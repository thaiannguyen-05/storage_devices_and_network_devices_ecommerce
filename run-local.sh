#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

GLASSFISH_HOME_DEFAULT="/c/glassfish7"
GLASSFISH_HOME="${GLASSFISH_HOME:-$GLASSFISH_HOME_DEFAULT}"
ASADMIN="$GLASSFISH_HOME/bin/asadmin"

if [[ ! -x "$ASADMIN" ]]; then
  echo "[ERROR] Không tìm thấy asadmin tại: $ASADMIN"
  echo "Hãy set GLASSFISH_HOME đúng, ví dụ:"
  echo "  export GLASSFISH_HOME=/c/Program\\ Files/glassfish7"
  exit 1
fi

if ! command -v java >/dev/null 2>&1; then
  echo "[ERROR] Chưa có java trong PATH. Hãy set JAVA_HOME và PATH trước khi chạy."
  exit 1
fi

if ! command -v ant >/dev/null 2>&1; then
  echo "[ERROR] Chưa có ant trong PATH."
  exit 1
fi

echo "[1/5] Kiểm tra Java và Ant"
java -version

# Chuẩn hóa JAVA_HOME cho môi trường bash nếu đang là dạng Windows path
if [[ -n "${JAVA_HOME:-}" && "$JAVA_HOME" =~ ^[A-Za-z]:\\ ]]; then
  JAVA_HOME="$(cygpath "$JAVA_HOME")"
  export JAVA_HOME
fi

# Nếu JAVA_HOME chưa có hoặc sai, suy luận từ đường dẫn java đang chạy
if [[ -z "${JAVA_HOME:-}" || ! -x "$JAVA_HOME/bin/java" ]]; then
  JAVA_BIN="$(command -v java)"
  JAVA_HOME="$(cd "$(dirname "$JAVA_BIN")/.." && pwd)"
  export JAVA_HOME
fi

echo "Using JAVA_HOME=$JAVA_HOME"
ant -version

echo "[2/5] Build WAR"
ant -f build.xml clean
ant -f build.xml dist

echo "[3/5] Start GlassFish domain (nếu chưa chạy)"
"$ASADMIN" start-domain || true

echo "[4/5] Redeploy Ecommerce.war"
"$ASADMIN" undeploy Ecommerce >/dev/null 2>&1 || true
"$ASADMIN" deploy --force=true "$PROJECT_DIR/dist/Ecommerce.war"

echo "[5/5] Done"
APP_URL="http://localhost:8080/Ecommerce/product"
echo "Mở URL: $APP_URL"

if command -v cmd.exe >/dev/null 2>&1; then
  cmd.exe /c start "$APP_URL" >/dev/null 2>&1 || true
fi
