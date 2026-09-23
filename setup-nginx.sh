#!/bin/bash
# ================================================================
# Nginx + SSL Setup for bekal.morpkhai.web.id
# Usage: sudo bash setup-nginx.sh
# ================================================================

set -e  # Exit on any error

DOMAIN="bekal.morpkhai.web.id"
EMAIL="admin@morpkhai.web.id"  # Change this to your email for Let's Encrypt
NGINX_CONF="/etc/nginx/sites-available/$DOMAIN"
CERTBOT_WEBROOT="/var/www/certbot"

echo "========================================================"
echo " Starting Nginx + SSL setup for $DOMAIN"
echo "========================================================"

# ─── 1. Install Nginx & Certbot ──────────────────────────────
echo ""
echo "[1/6] Installing Nginx and Certbot..."
apt-get update -q
apt-get install -y nginx certbot python3-certbot-nginx

# ─── 2. Create Certbot Webroot ───────────────────────────────
echo ""
echo "[2/6] Creating certbot webroot directory..."
mkdir -p $CERTBOT_WEBROOT
chown -R www-data:www-data $CERTBOT_WEBROOT

# ─── 3. Write Nginx Config (HTTP only first) ─────────────────
echo ""
echo "[3/6] Writing Nginx HTTP config..."
cat > $NGINX_CONF << 'EOF'
server {
    listen 80;
    listen [::]:80;
    server_name bekal.morpkhai.web.id;

    client_max_body_size 20M;

    location ^~ /.well-known/acme-challenge/ {
        root /var/www/certbot;
        default_type text/plain;
        try_files $uri =404;
    }

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
EOF

# ─── 4. Enable Site & Test Config ────────────────────────────
echo ""
echo "[4/6] Enabling site and testing Nginx config..."

# Enable site
ln -sf $NGINX_CONF /etc/nginx/sites-enabled/$DOMAIN

# Remove default site if exists
if [ -f /etc/nginx/sites-enabled/default ]; then
    rm /etc/nginx/sites-enabled/default
    echo "Removed default Nginx site."
fi

# Test config
nginx -t

# Reload Nginx
systemctl reload nginx
echo "Nginx reloaded with HTTP config."

# ─── 5. Obtain SSL Certificate ───────────────────────────────
echo ""
echo "[5/6] Obtaining SSL certificate from Let's Encrypt..."
certbot certonly \
    --webroot \
    --webroot-path=$CERTBOT_WEBROOT \
    --domain $DOMAIN \
    --email $EMAIL \
    --agree-tos \
    --non-interactive \
    --keep-until-expiring

# ─── 6. Write Full Nginx Config (HTTP + HTTPS) ───────────────
echo ""
echo "[6/6] Writing full Nginx config with SSL..."
cat > $NGINX_CONF << 'EOF'
server {
    listen 80;
    listen [::]:80;
    server_name bekal.morpkhai.web.id;

    client_max_body_size 20M;

    location ^~ /.well-known/acme-challenge/ {
        root /var/www/certbot;
        default_type text/plain;
        try_files $uri =404;
    }

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}

server {
    listen 443 ssl;
    listen [::]:443 ssl;

    server_name bekal.morpkhai.web.id;

    client_max_body_size 20M;

    ssl_certificate /etc/letsencrypt/live/bekal.morpkhai.web.id/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/bekal.morpkhai.web.id/privkey.pem;

    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;

        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
EOF

# Test and reload with full config
nginx -t
systemctl reload nginx

# ─── Auto-renewal Cron ───────────────────────────────────────
echo ""
echo "Setting up auto-renewal cron job..."
(crontab -l 2>/dev/null; echo "0 3 * * * certbot renew --quiet --post-hook 'systemctl reload nginx'") | crontab -

echo ""
echo "========================================================"
echo " ✅ Setup complete!"
echo "========================================================"
echo " Domain   : https://$DOMAIN"
echo " SSL Cert : /etc/letsencrypt/live/$DOMAIN/"
echo " Nginx    : /etc/nginx/sites-available/$DOMAIN"
echo " Auto-renew: daily at 3AM via cron"
echo "========================================================"
echo ""
echo "Test your site: curl -I https://$DOMAIN"