#!/bin/bash
# Использование: VPS_HOST=root@1.2.3.4 bash ktor-server/deploy.sh
set -e

VPS_HOST="${VPS_HOST:?Укажите VPS_HOST, например: root@1.2.3.4}"
VPS_DIR="/opt/enterprise-catalog"
JAR="ktor-server/build/libs/enterprise-catalog-server.jar"
SERVICE="ktor-server/enterprise-catalog.service"

echo "=== [1/4] Сборка fat-JAR ==="
./gradlew :ktor-server:shadowJar

echo "=== [2/4] Подготовка директории на VPS ==="
ssh "$VPS_HOST" "
  # Java 11 если не установлена
  if ! command -v java &>/dev/null; then
    apt-get update -qq && apt-get install -y openjdk-11-jre-headless
  fi
  mkdir -p $VPS_DIR
  ufw allow 8080/tcp 2>/dev/null || true
"

echo "=== [3/4] Копирование файлов ==="
scp "$JAR" "$VPS_HOST:$VPS_DIR/"
scp "$SERVICE" "$VPS_HOST:/tmp/enterprise-catalog.service"

echo "=== [4/4] Установка и перезапуск сервиса ==="
ssh "$VPS_HOST" "
  mv /tmp/enterprise-catalog.service /etc/systemd/system/
  systemctl daemon-reload
  systemctl enable enterprise-catalog
  systemctl restart enterprise-catalog
  sleep 2
  systemctl status enterprise-catalog --no-pager
"

echo ""
echo "✅ Готово! Сервер запущен на http://\$(echo $VPS_HOST | cut -d@ -f2):8080"
echo "   БД: Neon PostgreSQL (облако)"
