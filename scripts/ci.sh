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
  if [[ ! -d "$root" ]]; then
    return 0
  fi

  find "$root" "$@" -type f -name '*.jar' ! -name '*-sources.jar' -print | sort | paste -sd ':' -
}

join_classpath() {
  local joined=""
  local part

  for part in "$@"; do
    [[ -n "$part" ]] || continue

    if [[ -n "$joined" ]]; then
      joined="$joined:$part"
    else
      joined="$part"
    fi
  done

  printf '%s\n' "$joined"
}

run_main_test() {
  local class_name="$1"
  log "Run $class_name"
  "$JAVA_BIN" -cp "$TEST_CLASSES_DIR:$JAVAC_CLASSPATH" "$class_name"
}

compile_jsps() {
  local web_dir="$ROOT_DIR/build/web"
  local web_classes="$web_dir/WEB-INF/classes"
  local jsp_src_dir="$ROOT_DIR/build/generated/jsp-src"
  local jsp_classes_dir="$ROOT_DIR/build/generated/jsp-classes"
  local web_lib_classpath
  local jsp_compile_classpath

  if [[ ! -d "$web_dir" ]]; then
    printf 'Expected %s to exist after WAR build.\n' "$web_dir" >&2
    exit 1
  fi

  web_lib_classpath="$(classpath_from "$web_dir/WEB-INF/lib")"
  jsp_compile_classpath="$(join_classpath "$web_classes" "$web_lib_classpath" "$JAVAC_CLASSPATH")"

  rm -rf "$jsp_src_dir" "$jsp_classes_dir"
  mkdir -p "$jsp_src_dir" "$jsp_classes_dir"

  "$JAVA_BIN" -cp "$JAVAC_CLASSPATH" org.apache.jasper.JspC \
    -uriroot "$web_dir" \
    -d "$jsp_src_dir" \
    -die1 \
    -compilerSourceVM 1.8 \
    -compilerTargetVM 1.8 \
    -classpath "$jsp_compile_classpath"

  mapfile -t JSP_SOURCES < <(find "$jsp_src_dir" -type f -name '*.java' | sort)
  if [[ ${#JSP_SOURCES[@]} -eq 0 ]]; then
    printf 'JSP compiler did not generate any Java sources under %s.\n' "$jsp_src_dir" >&2
    exit 1
  fi

  "$JAVAC_BIN" \
    -encoding UTF-8 \
    -source 1.8 \
    -target 1.8 \
    -proc:none \
    -cp "$jsp_compile_classpath" \
    -d "$jsp_classes_dir" \
    "${JSP_SOURCES[@]}"

  printf 'Compiled %d generated JSP servlet source files.\n' "${#JSP_SOURCES[@]}"
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
run_main_test "common.guard.RoleGuardSupportTest"
run_main_test "module.core.sql.PostgresSchemaSupportTest"

ANT_CP="$ROOT_DIR/lib/jsp-compilation/ant-launcher.jar:$ROOT_DIR/lib/jsp-compilation/ant.jar"
ANT_MAIN="org.apache.tools.ant.Main"

log "Build WAR"
"$JAVA_BIN" -cp "$ANT_CP" "$ANT_MAIN" \
  -Dj2ee.server.home="$GLASSFISH_HOME" \
  -Dplatforms.JDK_17.home="$JAVA_HOME" \
  -f build.xml \
  clean dist

log "Compile JSPs"
compile_jsps

log "CI checks completed"
ls -lh dist/*.war
