#!/usr/bin/env bash
# USAGE-START
# Lists, and with --yes deletes, the toolchains this machine's Mountaya checkouts no longer pin,
# so the disk every build here shares does not fill. Everything it touches lives outside this
# repository and may be read by any checkout or any other project on the machine, so it is yours
# to run: never an agent's, and never the gate's, whose `mise run clean` deletes inside the
# repository alone.
#
#   bash scripts/clean-toolchains.sh [--yes]
#
# Without --yes it lists what it would delete and the room that would free, and deletes nothing.
# It keeps what any Mountaya checkout beside this one names, this one's included: each
# `../*/mise.toml`, each Gradle wrapper below them and each version catalog's Kotlin, and the
# version `mise where` reports in use. Beyond those it deletes:
#   - a Gradle version's distribution, caches and daemon logs in GRADLE_USER_HOME (~/.gradle), once
#     no Mountaya checkout has a wrapper naming it and no daemon of it has run for seven days:
#     Gradle keeps them a month on its own;
#   - a Kotlin/Native toolchain in KONAN_DATA_DIR (~/.konan) for a Kotlin no Mountaya checkout pins,
#     and the dependencies no kept toolchain names;
#   - in the Android SDK (ANDROID_HOME, which mise sets, and which every Mountaya checkout on this
#     machine shares): a system image, a platform and an NDK that no `../*/mise.toml` names,
#     keeping the newest NDK, the one the Android Gradle plugin downloads;
#   - mise's installs of Gradle and of the Android SDK at a version no checkout pins and none uses:
#     an Android SDK holds its system image, emulator and NDK inside its version's directory, so a
#     pin that moves leaves gigabytes behind;
#   - an emulator device named `mountaya-*` that no `../*/mise.toml` creates.
# A path a running process holds open is never deleted: where lsof is installed, it is asked
# first, and the path is kept and named. A Gradle version that only a project outside Mountaya
# uses is downloaded again by that project's next build.
# USAGE-END
set -euo pipefail

# shellcheck source=lib/common.sh
. "$(dirname "$0")/lib/common.sh"
answer_help "$@"

yes=0
for argument in "$@"; do
  case "$argument" in
    --yes) yes=1 ;;
    --) ;;
    *) die "Unknown argument: $argument" "bash scripts/clean-toolchains.sh --help" ;;
  esac
done

root="$(cd "$(dirname "$0")/.." && pwd -P)"
cd "$root"
freed_kb=0
found=0
kept_open=0

if has_cmd lsof; then
  can_see_open=1
else
  can_see_open=0
  warn "lsof is not on PATH, so whether a running process holds a path open cannot be asked first."
fi

# human <kilobytes>: a size the way df and du print it to a person.
human() {
  awk -v k="$1" 'BEGIN { if (k >= 1048576) printf "%.1f GB", k / 1048576; else printf "%.0f MB", k / 1024 }'
}

# held_open <path>: whether a running process holds the file, or a file below the directory, open.
held_open() {
  [ "$can_see_open" = 1 ] || return 1
  if [ -d "$1" ] && [ ! -L "$1" ]; then
    lsof +D "$1" >/dev/null 2>&1
  else
    lsof -- "$1" >/dev/null 2>&1
  fi
}

# remove <path> <why>: lists one path with its size, or under --yes deletes it, unless a running
# process holds it open.
remove() {
  local target="$1" why="$2" kb
  [ -e "$target" ] || [ -L "$target" ] || return 0
  kb="$(du -sk "$target" 2>/dev/null | awk '{ print $1 }')"
  found=$((found + 1))
  if [ "$yes" = 0 ]; then
    freed_kb=$((freed_kb + ${kb:-0}))
    info "would delete ${target} ($(human "${kb:-0}")): ${why}"
    return 0
  fi
  if held_open "$target"; then
    kept_open=$((kept_open + 1))
    warn "kept ${target}: a running process holds it open (lsof +D ${target} names it)"
    return 0
  fi
  rm -rf "$target"
  freed_kb=$((freed_kb + ${kb:-0}))
  info "deleted ${target} ($(human "${kb:-0}")): ${why}"
}

# version_of_wrapper <gradle-wrapper.properties>: the Gradle version its distribution names.
version_of_wrapper() {
  sed -n 's|^distributionUrl=.*gradle-\([0-9][0-9.]*\)-[a-z]*\.zip.*|\1|p' "$1" | head -n 1
}

gradle_pin="$(version_of_wrapper gradle/wrapper/gradle-wrapper.properties)"
kotlin_pin="$(sed -n 's/^kotlin = "\([^"]*\)".*/\1/p' gradle/libs.versions.toml | head -n 1)"
image_pin="$(grep -o 'system-images/[^@" ]*' mise.toml | head -n 1)"
platform_pin="$(grep -o 'platforms/android-[^@" ]*' mise.toml | head -n 1)"
[ -n "$gradle_pin" ] || die "No Gradle version in gradle/wrapper/gradle-wrapper.properties" "mise run wrapper"
[ -n "$kotlin_pin" ] || die "No kotlin version in gradle/libs.versions.toml"

# What every Mountaya checkout beside this one pins, this one's included.
checkout_pins="$(cat ../*/mise.toml 2>/dev/null || true)"
# named <text>: whether any Mountaya checkout's mise.toml names it as written.
named() {
  case "$checkout_pins" in *"$1"*) return 0 ;; *) return 1 ;; esac
}

# Gradle: the versions every Mountaya checkout's wrapper names, this one's included.
live_gradle=" ${gradle_pin} "
while read -r properties; do
  live_gradle="${live_gradle}$(version_of_wrapper "$properties") "
done < <(find .. -maxdepth 6 \( -name build -o -name .build -o -name node_modules -o -name .git -o -name .gradle \) -prune \
  -o -path '*/gradle/wrapper/gradle-wrapper.properties' -print 2>/dev/null)

gradle_home="${GRADLE_USER_HOME:-$HOME/.gradle}"
installed_gradle="$(
  {
    for entry in "$gradle_home"/wrapper/dists/gradle-*; do
      if [ -d "$entry" ]; then basename "$entry" | sed -n 's/^gradle-\([0-9][0-9.]*\)-[a-z]*$/\1/p'; fi
    done
    for entry in "$gradle_home"/caches/* "$gradle_home"/daemon/*; do
      if [ -d "$entry" ]; then basename "$entry" | grep -E '^[0-9]+\.[0-9]+(\.[0-9]+)?$' || true; fi
    done
  } | sort -u
)"
for version in $installed_gradle; do
  case "$live_gradle" in
    *" ${version} "*) continue ;;
  esac
  if [ -d "$gradle_home/daemon/$version" ] && [ -n "$(find "$gradle_home/daemon/$version" -name '*.log' -mtime -7 2>/dev/null | head -n 1)" ]; then
    continue
  fi
  why="no Mountaya wrapper names Gradle ${version} and no daemon of it ran this week"
  for entry in "$gradle_home"/wrapper/dists/gradle-"$version"-*; do
    remove "$entry" "$why"
  done
  remove "$gradle_home/caches/$version" "$why"
  remove "$gradle_home/daemon/$version" "$why"
done

# Kotlin/Native: the toolchain of every Kotlin a Mountaya checkout's version catalog pins, and the
# dependencies those toolchains' properties name.
live_kotlin=" ${kotlin_pin} "
for catalog in ../*/gradle/libs.versions.toml; do
  [ -f "$catalog" ] || continue
  live_kotlin="${live_kotlin}$(sed -n 's/^kotlin = "\([^"]*\)".*/\1/p' "$catalog" | head -n 1) "
done
konan_home="${KONAN_DATA_DIR:-$HOME/.konan}"
kept_properties=()
for toolchain in "$konan_home"/kotlin-native-prebuilt-*; do
  [ -d "$toolchain" ] || continue
  version="${toolchain##*-}"
  case "$live_kotlin" in
    *" ${version} "*)
      if [ -f "$toolchain/konan/konan.properties" ]; then kept_properties+=("$toolchain/konan/konan.properties"); fi
      ;;
    *) remove "$toolchain" "no Mountaya checkout pins Kotlin ${version}; this one pins ${kotlin_pin}" ;;
  esac
done
# With no kept toolchain to ask, no dependency is judged.
if [ "${#kept_properties[@]}" -gt 0 ]; then
  for dependency in "$konan_home"/dependencies/*; do
    [ -d "$dependency" ] || continue
    name="$(basename "$dependency")"
    [ "$name" = cache ] && continue
    grep -qF "$name" "${kept_properties[@]}" || remove "$dependency" "no kept Kotlin/Native toolchain names it"
  done
fi

# The Android SDK mise installs. It is one directory every Mountaya checkout on this machine
# installs into (sdk-dart's plugins compile against another platform and NDK than this build), so
# a package any checkout's mise.toml names is kept, and only what no checkout names goes.
if [ -n "${ANDROID_HOME:-}" ] && [ -d "$ANDROID_HOME" ]; then
  for image in "$ANDROID_HOME"/system-images/*/*/*; do
    [ -d "$image" ] || continue
    relative="${image#"$ANDROID_HOME"/}"
    [ "$relative" = "$image_pin" ] || named "$relative" ||
      remove "$image" "no Mountaya checkout names ${relative}; the device tests run on ${image_pin}"
  done
  for platform in "$ANDROID_HOME"/platforms/*; do
    [ -d "$platform" ] || continue
    relative="${platform#"$ANDROID_HOME"/}"
    [ "$relative" = "$platform_pin" ] || named "${relative}@" || named "${relative}\"" || named "${relative} " ||
      remove "$platform" "no Mountaya checkout names ${relative}; this build compiles against ${platform_pin}"
  done
  newest_ndk="$(ls "$ANDROID_HOME"/ndk 2>/dev/null | sort -V | tail -n 1 || true)"
  for ndk in "$ANDROID_HOME"/ndk/*; do
    [ -d "$ndk" ] || continue
    [ "$(basename "$ndk")" = "$newest_ndk" ] || named "ndk/$(basename "$ndk")" ||
      remove "$ndk" "no Mountaya checkout names it; the Android Gradle plugin uses the newest NDK, ${newest_ndk}"
  done
else
  info "ANDROID_HOME is not set, so the Android SDK is not looked at: run this under mise"
fi

# mise's own installs of the two tools this repository pins beyond the JDK, at a version no
# checkout pins and none is using.
if has_cmd mise; then
  for tool in gradle android-sdk; do
    pin="$(sed -n "s/^${tool} = \"\([^\"]*\)\".*/\1/p" mise.toml | head -n 1)"
    current="$(mise where "$tool" 2>/dev/null || true)"
    [ -n "$pin" ] && [ -n "$current" ] || continue
    in_use="$(basename "$current")"
    installs="$(dirname "$current")"
    for version_dir in "$installs"/*; do
      if [ ! -d "$version_dir" ] || [ -L "$version_dir" ]; then continue; fi
      version="$(basename "$version_dir")"
      [ "$version" = "$pin" ] && continue
      [ "$version" = "$in_use" ] && continue
      named "${tool} = \"${version}\"" && continue
      remove "$version_dir" "no Mountaya checkout pins ${tool} ${version}, and mise uses ${in_use}"
    done
    if [ "$yes" = 1 ]; then
      for link in "$installs"/*; do
        if [ -L "$link" ] && [ ! -e "$link" ]; then rm -f "$link"; fi
      done
    fi
  done
else
  info "mise is not on PATH, so its installs are not looked at"
fi

# The emulator's devices: a `mountaya-*` device no checkout's mise.toml creates.
avd_home="${ANDROID_AVD_HOME:-${ANDROID_USER_HOME:-$HOME/.android}/avd}"
for device in "$avd_home"/mountaya-*.avd; do
  [ -d "$device" ] || continue
  name="$(basename "$device" .avd)"
  named "--name ${name} " && continue
  remove "$device" "no Mountaya checkout's android:emulator creates ${name}"
  remove "$avd_home/$name.ini" "no Mountaya checkout's android:emulator creates ${name}"
done

if [ "$yes" = 0 ]; then
  if [ "$found" = 0 ]; then
    ok "Nothing to delete: every toolchain here is one a Mountaya checkout pins or uses"
  else
    ok "$(human "$freed_kb") would be freed. Deleting is yours: mise run clean:toolchains -- --yes"
  fi
else
  ok "$(human "$freed_kb") freed"
  [ "$kept_open" = 0 ] || warn "${kept_open} path(s) kept because a running process holds them open; run this again once it stops."
fi
