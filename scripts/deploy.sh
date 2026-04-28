#!/usr/bin/env bash
set -e

PROJECT_DIR="/opt/agriculture"
RELEASE_DIR="$PROJECT_DIR/release"
BACKEND_DIR="$PROJECT_DIR/backend"
BACKUP_DIR="$PROJECT_DIR/backup"
WEB_ROOT="/www/wwwroot/agriculture"

# Change this to the real Java container name shown in 1Panel.
CONTAINER_NAME="agriculture-backend"

PACKAGE_FILE="$RELEASE_DIR/package.tar.gz"
DEPLOY_TMP="$RELEASE_DIR/deploy_tmp"
TIMESTAMP="$(date +%Y%m%d%H%M%S)"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

log "Starting agriculture platform deployment"

[ -n "$WEB_ROOT" ] || fail "WEB_ROOT is empty"
[ "$WEB_ROOT" != "/" ] || fail "WEB_ROOT must not be /"
case "$WEB_ROOT" in
  /www/wwwroot/*) ;;
  *) fail "WEB_ROOT must start with /www/wwwroot/" ;;
esac

[ -f "$PACKAGE_FILE" ] || fail "Package not found: $PACKAGE_FILE"

mkdir -p "$RELEASE_DIR" "$BACKEND_DIR" "$BACKUP_DIR" "$WEB_ROOT"
rm -rf "$DEPLOY_TMP"
mkdir -p "$DEPLOY_TMP"

log "Extracting package"
tar -xzf "$PACKAGE_FILE" -C "$DEPLOY_TMP"

NEW_JAR="$DEPLOY_TMP/release/app.jar"
NEW_DIST="$DEPLOY_TMP/release/dist.tar.gz"

[ -f "$NEW_JAR" ] || fail "New app.jar not found in package"
[ -f "$NEW_DIST" ] || fail "New dist.tar.gz not found in package"

if [ -f "$BACKEND_DIR/app.jar" ]; then
  log "Backing up existing backend jar"
  cp "$BACKEND_DIR/app.jar" "$BACKUP_DIR/app-$TIMESTAMP.jar"
fi

if [ -d "$WEB_ROOT" ] && [ "$(find "$WEB_ROOT" -mindepth 1 -maxdepth 1 | wc -l)" -gt 0 ]; then
  log "Backing up existing frontend files"
  tar --exclude='./upload' -czf "$BACKUP_DIR/web-$TIMESTAMP.tar.gz" -C "$WEB_ROOT" .
fi

log "Replacing backend jar"
cp "$NEW_JAR" "$BACKEND_DIR/app.jar"

log "Replacing frontend files while preserving $WEB_ROOT/upload"
find "$WEB_ROOT" -mindepth 1 -maxdepth 1 ! -name upload -exec rm -rf {} +
tar -xzf "$NEW_DIST" -C "$WEB_ROOT"

log "Restarting Docker container: $CONTAINER_NAME"
docker restart "$CONTAINER_NAME"

rm -rf "$DEPLOY_TMP"
log "Deployment completed successfully"
