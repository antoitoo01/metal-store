#!/usr/bin/env bash
set -euo pipefail

# ─── Metal Store — Deploy Script ───────────────────────────────────────────
# Run this on the Oracle Cloud VM (Ubuntu 24.04).
# Usage:
#   scp scripts/deploy.sh metal-store-*.jar ubuntu@<ip>:~
#   ssh ubuntu@<ip> 'bash deploy.sh'
# ─────────────────────────────────────────────────────────────────────────────

# ─── Config ─────────────────────────────────────────────────────────────────
JAR_FILE="${1:-metal-store-0.0.1-SNAPSHOT.jar}"
APP_USER="${APP_USER:-metalstore}"
APP_DIR="/opt/metal-store"
LOG_DIR="/var/log/metal-store"
DUCKDNS_DOMAIN="${DUCKDNS_DOMAIN:-}"   # e.g. "taller"
DUCKDNS_TOKEN="${DUCKDNS_TOKEN:-}"     # token from duckdns.org
# ─────────────────────────────────────────────────────────────────────────────

echo "=== 1. System update & deps ==="
sudo apt-get update -y
sudo apt-get install -y openjdk-25-jre-headless certbot curl

echo "=== 2. Create user ==="
sudo id -u "$APP_USER" &>/dev/null || sudo useradd -m -s /bin/bash "$APP_USER"

echo "=== 3. Create directories ==="
sudo mkdir -p "$APP_DIR" "$LOG_DIR"
sudo chown "$APP_USER:$APP_USER" "$APP_DIR" "$LOG_DIR"

echo "=== 4. Copy JAR ==="
sudo cp "$JAR_FILE" "$APP_DIR/app.jar"
sudo chown "$APP_USER:$APP_USER" "$APP_DIR/app.jar"

echo "=== 5. Create .env ==="
sudo tee "$APP_DIR/.env" > /dev/null <<EOF
SUPABASE_URL=${SUPABASE_URL:-}
SUPABASE_PUBLISHABLE_KEY=${SUPABASE_PUBLISHABLE_KEY:-}
SUPABASE_SECRET_KEY=${SUPABASE_SECRET_KEY:-}
SUPABASE_DB_URL=${SUPABASE_DB_URL:-}
SUPABASE_DB_USER=${SUPABASE_DB_USER:-}
SUPABASE_DB_PASSWORD=${SUPABASE_DB_PASSWORD:-}
SPRING_PROFILES_ACTIVE=prod
CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS:-https://metalstore.miweb.com}
EOF
sudo chown "$APP_USER:$APP_USER" "$APP_DIR/.env"

echo "=== 6. Create systemd service ==="
sudo tee /etc/systemd/system/metal-store.service > /dev/null <<SERVICE
[Unit]
Description=Metal Store ERP
After=network.target

[Service]
Type=simple
User=$APP_USER
WorkingDirectory=$APP_DIR
EnvironmentFile=$APP_DIR/.env
ExecStart=/usr/bin/java -jar $APP_DIR/app.jar
Restart=always
RestartSec=10
StandardOutput=append:$LOG_DIR/app.log
StandardError=append:$LOG_DIR/error.log

[Install]
WantedBy=multi-user.target
SERVICE

sudo systemctl daemon-reload
sudo systemctl enable metal-store

echo "=== 7. DuckDNS (optional) ==="
if [ -n "$DUCKDNS_DOMAIN" ] && [ -n "$DUCKDNS_TOKEN" ]; then
    sudo tee /etc/systemd/system/duckdns.service > /dev/null <<DUCK
[Unit]
Description=DuckDNS dynamic DNS
After=network.target

[Service]
Type=oneshot
ExecStart=/usr/bin/curl -s "https://www.duckdns.org/update?domains=$DUCKDNS_DOMAIN&token=$DUCKDNS_TOKEN&ip="
DUCK

    sudo tee /etc/systemd/system/duckdns.timer > /dev/null <<DUCKT
[Unit]
Description=Update DuckDNS every 5 minutes
Requires=duckdns.service

[Timer]
OnCalendar=*:0/5
Persistent=true

[Install]
WantedBy=timers.target
DUCKT

    sudo systemctl daemon-reload
    sudo systemctl enable duckdns.timer
    sudo systemctl start duckdns.timer
    echo "  DuckDNS configurado para $DUCKDNS_DOMAIN.duckdns.org"
fi

echo "=== 8. Start app ==="
sudo systemctl start metal-store

echo ""
echo "─── Resumen ──────────────────────────────────────"
echo "App:     http://$(curl -4 -s ifconfig.me):8080"
echo "Service: sudo systemctl status metal-store"
echo "Logs:    tail -f $LOG_DIR/app.log"
if [ -n "$DUCKDNS_DOMAIN" ]; then
    echo "DNS:     https://$DUCKDNS_DOMAIN.duckdns.org"
fi
echo "──────────────────────────────────────────────────"
