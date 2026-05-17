#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"
cd "$ROOT_DIR"

log() {
  printf '\n==> %s\n' "$1"
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'Missing required command: %s\n' "$1" >&2
    exit 1
  fi
}

resolve_java_home() {
  if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
    printf '%s\n' "$JAVA_HOME"
    return
  fi

  local javac_path
  javac_path="$(readlink -f "$(command -v javac)")"
  cd "$(dirname "$javac_path")/.." && pwd -P
}

resolve_glassfish_home() {
  if [[ -n "${GLASSFISH_HOME:-}" && -d "$GLASSFISH_HOME/modules" ]]; then
    printf '%s\n' "$GLASSFISH_HOME"
    return
  fi

  local candidate
  for candidate in "$HOME/glassfish7/glassfish" "/opt/glassfish7/glassfish" "/opt/glassfish"; do
    if [[ -d "$candidate/modules" ]]; then
      printf '%s\n' "$candidate"
      return
    fi
  done

  printf 'GLASSFISH_HOME is required and must point to a GlassFish installation with modules/.\n' >&2
  exit 1
}

classpath_from() {
  local root="$1"
  shift
  find "$root" "$@" -type f -name '*.jar' ! -name '*-sources.jar' -print | sort | paste -sd ':' -
}

run_main_test() {
  local class_name="$1"
  log "Run $class_name"
  "$JAVA_BIN" -cp "$TEST_CLASSES_DIR:$JAVAC_CLASSPATH" "$class_name"
}

require_command find
require_command javac
require_command java
require_command sort
require_command paste

JAVA_HOME="$(resolve_java_home)"
JAVA_BIN="$JAVA_HOME/bin/java"
JAVAC_BIN="$JAVA_HOME/bin/javac"
GLASSFISH_HOME="$(resolve_glassfish_home)"

LIB_CLASSPATH="$(classpath_from "$ROOT_DIR/lib")"
GLASSFISH_CLASSPATH="$(classpath_from "$GLASSFISH_HOME/modules")"
JAVAC_CLASSPATH="$LIB_CLASSPATH:$GLASSFISH_CLASSPATH"
TEST_CLASSES_DIR="$ROOT_DIR/build/ci-classes"

log "Environment"
printf 'JAVA_HOME=%s\n' "$JAVA_HOME"
printf 'GLASSFISH_HOME=%s\n' "$GLASSFISH_HOME"

log "Compile Java sources and tests"
rm -rf "$TEST_CLASSES_DIR"
mkdir -p "$TEST_CLASSES_DIR"
mapfile -t JAVA_SOURCES < <(find src/java test -type f -name '*.java' | sort)
"$JAVAC_BIN" \
  -encoding UTF-8 \
  -source 17 \
  -target 17 \
  -cp "$JAVAC_CLASSPATH" \
  -d "$TEST_CLASSES_DIR" \
  "${JAVA_SOURCES[@]}"

run_main_test "common.logger.audit.AuditSupportTest"
run_main_test "module.bussiness.admin.AdminServiceSupportTest"

ANT_CP="$ROOT_DIR/lib/jsp-compilation/ant-launcher.jar:$ROOT_DIR/lib/jsp-compilation/ant.jar"
ANT_MAIN="org.apache.tools.ant.Main"

log "Build WAR"
"$JAVA_BIN" -cp "$ANT_CP" "$ANT_MAIN" \
  -Dj2ee.server.home="$GLASSFISH_HOME" \
  -Dplatforms.JDK_17.home="$JAVA_HOME" \
  -f build.xml \
  clean dist

log "Compile JSPs"
"$JAVA_BIN" -cp "$ANT_CP" "$ANT_MAIN" \
  -Dcompile.jsps=true \
  -Djavac.source=1.8 \
  -Djavac.target=1.8 \
  -Dj2ee.server.home="$GLASSFISH_HOME" \
  -Dplatforms.JDK_17.home="$JAVA_HOME" \
  -f build.xml \
  compile-jsps

log "CI checks completed"
ls -lh dist/*.war
