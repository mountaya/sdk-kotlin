#!/usr/bin/env bash
# USAGE-START
# Deletes what this repository's builds left behind and no longer use, so the disk it builds on
# does not fill. `mise run ci` runs it before the gate; `mise run clean` runs it alone. It deletes
# paths inside this repository and nothing else: a toolchain every checkout on this machine shares
# is never its to touch, and `mise run clean:toolchains` (scripts/clean-toolchains.sh), yours to
# run and never an agent's, lists those.
#
#   bash scripts/clean.sh [--all] [--dry-run]
#
# By default it deletes only what no build of this repository uses any more:
#   - the per-version state Gradle keeps under each `.gradle/` of this repository, the root's,
#     build-logic's, the consumer's and Trail's, for a Gradle other than the one the wrapper names;
#   - the `build/`, `.gradle/` and `.kotlin/` of a directory that holds no Gradle build any more,
#     a module or a build removed and its output left behind.
# `--all` also deletes this repository's own build directories (`build/`, `.gradle/`, `.kotlin/`
# beside each Gradle build script), which the next gate builds again. `--dry-run` lists what it
# would delete and deletes nothing.
# USAGE-END
set -euo pipefail

# shellcheck source=lib/common.sh
. "$(dirname "$0")/lib/common.sh"
answer_help "$@"

all=0
dry_run=0
for argument in "$@"; do
  case "$argument" in
    --all) all=1 ;;
    --dry-run) dry_run=1 ;;
    --) ;;
    *) die "Unknown argument: $argument" "bash scripts/clean.sh --help" ;;
  esac
done

root="$(cd "$(dirname "$0")/.." && pwd -P)"
cd "$root"
freed_kb=0
removed=0

# human <kilobytes>: a size the way df and du print it to a person.
human() {
  awk -v k="$1" 'BEGIN { if (k >= 1048576) printf "%.1f GB", k / 1048576; else printf "%.0f MB", k / 1024 }'
}

# inside <path>: whether the path, its directory resolved, lies below this repository's root. A
# link is judged by where it sits, never by what it points at, since `rm -rf` removes the link.
inside() {
  local parent
  parent="$(cd "$(dirname "$1")" 2>/dev/null && pwd -P)" || return 1
  case "$parent/" in
    "$root"/*) return 0 ;;
    *) return 1 ;;
  esac
}

# remove <path> <why>: deletes one path, or under --dry-run says it would, with its size. A path
# outside the repository stops the script: nothing here may reach one.
remove() {
  local target="$1" why="$2" kb
  [ -e "$target" ] || [ -L "$target" ] || return 0
  inside "$target" || die "Refusing to delete ${target}: it is not inside ${root}, and this script deletes nothing else." \
    "A toolchain outside the repository is listed by: mise run clean:toolchains"
  kb="$(du -sk "$target" 2>/dev/null | awk '{ print $1 }')"
  freed_kb=$((freed_kb + ${kb:-0}))
  removed=$((removed + 1))
  if [ "$dry_run" = 1 ]; then
    info "would delete ${target} ($(human "${kb:-0}")): ${why}"
  else
    rm -rf "$target"
    info "deleted ${target} ($(human "${kb:-0}")): ${why}"
  fi
}

# version_of_wrapper <gradle-wrapper.properties>: the Gradle version its distribution names.
version_of_wrapper() {
  sed -n 's|^distributionUrl=.*gradle-\([0-9][0-9.]*\)-[a-z]*\.zip.*|\1|p' "$1" | head -n 1
}

# is_build <directory>: whether a Gradle build script stands in it.
is_build() {
  [ -f "$1/build.gradle.kts" ] || [ -f "$1/settings.gradle.kts" ] || [ -f "$1/build.gradle" ] || [ -f "$1/settings.gradle" ]
}

gradle_pin="$(version_of_wrapper gradle/wrapper/gradle-wrapper.properties)"
[ -n "$gradle_pin" ] || die "No Gradle version in gradle/wrapper/gradle-wrapper.properties" "mise run wrapper"

# Every `build/`, `.gradle/` and `.kotlin/` of this repository, looked for where a build writes
# them: a source tree is never entered, so a package directory named `build` is never taken for one.
outputs="$(find . \( -name src -o -name node_modules -o -name .git \) -prune -o \
  \( -name build -o -name .gradle -o -name .kotlin \) -type d -prune -print 2>/dev/null | sort)"

while IFS= read -r output; do
  [ -n "$output" ] || continue
  owner="$(dirname "$output")"
  if ! is_build "$owner"; then
    remove "$output" "${owner} holds no Gradle build any more"
    continue
  fi
  # Gradle's state for one version of itself, beside the build that ran it; --all deletes the
  # whole directory below.
  if [ "$all" = 0 ] && [ "$(basename "$output")" = .gradle ]; then
    for state in "$output"/*; do
      [ -d "$state" ] || continue
      version="$(basename "$state")"
      echo "$version" | grep -Eq '^[0-9]+\.[0-9]+(\.[0-9]+)?$' || continue
      [ "$version" = "$gradle_pin" ] || remove "$state" "the wrapper names Gradle ${gradle_pin}, not ${version}"
    done
  fi
done <<<"$outputs"

# --all: this repository's own build directories, beside every Gradle build script.
if [ "$all" = 1 ]; then
  while IFS= read -r output; do
    [ -n "$output" ] || continue
    is_build "$(dirname "$output")" || continue
    remove "$output" "every build directory, rebuilt by the next gate"
  done <<<"$outputs"
fi

if [ "$removed" = 0 ]; then
  ok "Nothing to delete: every build directory here is one a build of this repository uses"
elif [ "$dry_run" = 1 ]; then
  ok "$(human "$freed_kb") would be freed"
else
  ok "$(human "$freed_kb") freed"
fi
info "What every checkout on this machine shares is listed by mise run clean:toolchains, yours to run."
