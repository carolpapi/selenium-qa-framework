#!/usr/bin/env bash
# Checks (and optionally installs) everything needed to work on this project on macOS.
#
#   bash check-setup.sh            -> only checks and shows what is missing
#   bash check-setup.sh --install  -> installs what is missing with Homebrew
#
# Safe to run as many times as you want.

INSTALL=false
[ "$1" = "--install" ] && INSTALL=true

GREEN="\033[32m"; RED="\033[31m"; YELLOW="\033[33m"; RESET="\033[0m"
ok()      { printf "${GREEN}✔ %-16s${RESET} %s\n" "$1" "$2"; }
missing() { printf "${RED}✘ %-16s${RESET} %s\n" "$1" "$2"; }
warn()    { printf "${YELLOW}! %-16s${RESET} %s\n" "$1" "$2"; }

MISSING=()

echo "=== macOS $(sw_vers -productVersion) on $(uname -m) ==="
echo

# 1) Xcode Command Line Tools (gives git, needed by Homebrew)
if xcode-select -p >/dev/null 2>&1; then
  ok "Command Line Tools" "$(xcode-select -p)"
else
  missing "Command Line Tools" "run: xcode-select --install"
  $INSTALL && xcode-select --install && echo "Finish the popup installer, then run this script again." && exit 0
fi

# 2) Homebrew
if ! command -v brew >/dev/null 2>&1; then
  for p in /opt/homebrew/bin/brew /usr/local/bin/brew; do
    [ -x "$p" ] && eval "$("$p" shellenv)"
  done
fi
if command -v brew >/dev/null 2>&1; then
  ok "Homebrew" "$(brew --version | head -1)"
else
  missing "Homebrew" 'run: /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"'
  if $INSTALL; then
    echo "Homebrew must be installed interactively (it asks for your Mac password)."
    echo "Run the command above, follow the 'Next steps' it prints, then run this script again."
    exit 1
  fi
fi

# 3) Java 17
if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
  ok "JDK 17" "$(/usr/libexec/java_home -v 17)"
else
  missing "JDK 17" "brew install --cask temurin@17"
  MISSING+=("cask:temurin@17")
fi

# JAVA_HOME should point to 17 so Maven uses the right Java
if [ -n "$JAVA_HOME" ] && "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"17'; then
  ok "JAVA_HOME" "$JAVA_HOME"
else
  warn "JAVA_HOME" "not pointing to Java 17 (fix in step 'JAVA_HOME' of the guide)"
fi

# 4) Maven
if command -v mvn >/dev/null 2>&1; then
  ok "Maven" "$(mvn -v 2>/dev/null | head -1)"
  mvn -v 2>/dev/null | grep -q "Java version: 17" \
    && ok "Maven uses Java" "17" \
    || warn "Maven uses Java" "$(mvn -v 2>/dev/null | grep 'Java version' | cut -d, -f1) (should be 17 - set JAVA_HOME)"
else
  missing "Maven" "brew install maven"
  MISSING+=("formula:maven")
fi

# 5) Git
if command -v git >/dev/null 2>&1; then
  ok "Git" "$(git --version)"
  [ -n "$(git config --global user.name)" ] \
    && ok "Git user" "$(git config --global user.name) <$(git config --global user.email)>" \
    || warn "Git user" 'not set: git config --global user.name "Your Name" && git config --global user.email "you@email.com"'
else
  missing "Git" "brew install git"
  MISSING+=("formula:git")
fi

# 6) GitHub CLI
if command -v gh >/dev/null 2>&1; then
  ok "GitHub CLI" "$(gh --version | head -1)"
  gh auth status >/dev/null 2>&1 && ok "GitHub login" "authenticated" || warn "GitHub login" "run: gh auth login"
else
  missing "GitHub CLI" "brew install gh"
  MISSING+=("formula:gh")
fi

# 7) Browsers
[ -d "/Applications/Google Chrome.app" ] && ok "Chrome" "installed" \
  || { missing "Chrome" "brew install --cask google-chrome"; MISSING+=("cask:google-chrome"); }
[ -d "/Applications/Firefox.app" ] && ok "Firefox" "installed (optional)" \
  || { warn "Firefox" "optional: brew install --cask firefox"; }

# 8) IDE
if [ -d "/Applications/IntelliJ IDEA CE.app" ] || [ -d "/Applications/IntelliJ IDEA.app" ] || [ -d "/Applications/IntelliJ IDEA Ultimate.app" ]; then
  ok "IntelliJ IDEA" "installed"
else
  missing "IntelliJ IDEA" "brew install --cask intellij-idea-ce"
  MISSING+=("cask:intellij-idea-ce")
fi

# 9) Optional tools
command -v allure >/dev/null 2>&1 && ok "Allure" "$(allure --version) (optional)" || warn "Allure" "optional: brew install allure"
command -v docker >/dev/null 2>&1 && ok "Docker" "$(docker --version) (optional)" || warn "Docker" "optional, for Selenium Grid: install Docker Desktop from docker.com"

echo
if [ ${#MISSING[@]} -eq 0 ]; then
  echo "Everything required is installed. Next: mvn test -Dsuite=api"
  exit 0
fi

if $INSTALL; then
  echo "Installing missing items..."
  for item in "${MISSING[@]}"; do
    kind="${item%%:*}"; name="${item#*:}"
    if [ "$kind" = "cask" ]; then brew install --cask "$name"; else brew install "$name"; fi
  done
  echo
  echo "Done. Open a NEW terminal window and run: bash check-setup.sh"
else
  echo "Missing: ${MISSING[*]//*:/}"
  echo "Run 'bash check-setup.sh --install' to install them with Homebrew."
fi
