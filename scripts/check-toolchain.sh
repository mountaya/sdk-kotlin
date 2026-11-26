#!/usr/bin/env bash
# USAGE-START
# Refuses to run the gate on a toolchain other than the one mise.toml names.
#
#   bash scripts/check-toolchain.sh <xcode version>
#
# mise installs the JDK it pins, and ./gradlew starts Gradle on a JDK of its own choosing:
# JAVA_HOME where it is set, else the `java` on PATH. So the JDK the wrapper would pick is held to
# the directory mise installed the `java` pin into, read from mise.toml; the JDK reports a version
# of its own build, which the pin's name does not spell. On macOS the iOS klibs are built with
# Xcode's tools, which mise cannot install, so the version `mise run toolchain:check` passes is
# asserted instead; off macOS no iOS target is built, and Xcode is not asked for. A gate that
# passed on another toolchain says nothing about this one.
# USAGE-END
set -euo pipefail

# shellcheck source=lib/common.sh
. "$(dirname "$0")/lib/common.sh"
answer_help "$@"
require_cmd mise "mise: sh .agents/setup installs it"

xcode_version="${1:-}"
[ -n "$xcode_version" ] || die "The toolchain check takes the Xcode version mise.toml names." "bash scripts/check-toolchain.sh --help"

cd "$(dirname "$0")/.."

java_pin="$(sed -n 's/^java = "\([^"]*\)".*/\1/p' mise.toml | head -n 1)"
[ -n "$java_pin" ] || die "mise.toml pins no java."

pinned="$(mise where java 2>/dev/null || true)"
if [ -z "$pinned" ] || [ ! -d "$pinned" ]; then
  die "mise.toml pins Java ${java_pin}, and mise has not installed it." "mise install"
fi
pinned="$(cd -P "$pinned" && pwd -P)"

# The JDK ./gradlew starts Gradle on, picked as the wrapper picks it.
if [ -n "${JAVA_HOME:-}" ]; then
  chosen="$JAVA_HOME"
  chosen_by="JAVA_HOME"
elif java_cmd="$(command -v java 2>/dev/null)"; then
  chosen="$(cd -P "$(dirname "$java_cmd")/.." 2>/dev/null && pwd -P || true)"
  chosen_by="the java on PATH"
else
  chosen=""
  chosen_by="nothing"
fi
[ -z "$chosen" ] || [ ! -d "$chosen" ] || chosen="$(cd -P "$chosen" && pwd -P)"
if [ "$chosen" != "$pinned" ]; then
  die "mise.toml pins Java ${java_pin}, installed at ${pinned}, and ./gradlew would start Gradle on ${chosen:-no JDK}, which ${chosen_by} names." \
    "Run the gate under mise, which sets JAVA_HOME to the pinned JDK: mise run ci"
fi

case "$(uname -s)" in
  Darwin)
    require_cmd xcodebuild "Xcode, installed by hand from the App Store or developer.apple.com"
    xcode="$(xcodebuild -version | sed -n 1p)"
    if [ "$xcode" != "Xcode ${xcode_version}" ]; then
      die "mise.toml names Xcode ${xcode_version}, and the selected one is ${xcode}." \
        "Install Xcode ${xcode_version}, then select it: sudo xcode-select -s /Applications/Xcode.app"
    fi
    ok "The toolchain is the pinned one: Java ${java_pin}, Xcode ${xcode_version}."
    ;;
  *)
    ok "The toolchain is the pinned one: Java ${java_pin}; no Xcode off macOS."
    ;;
esac
