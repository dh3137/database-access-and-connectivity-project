#!/usr/bin/env bash
# reset_db.sh — Wipe cars table and reload sample data
# Usage: bash tools/reset_db.sh '<mysql_root_password>'
# See workflows/reset_database.md for full context

set -euo pipefail

MYSQL="/usr/local/mysql/bin/mysql"
PASSWORD="${1:?Usage: reset_db.sh '<mysql_root_password>'}"
DB="car_dealership"
SEED="database/sample-data.sql"

echo "→ Deleting all rows from cars..."
"$MYSQL" -u root -p"$PASSWORD" "$DB" -e "DELETE FROM cars;"

echo "→ Reseeding from $SEED..."
"$MYSQL" -u root -p"$PASSWORD" "$DB" < "$SEED"

echo "→ Done. Row count:"
"$MYSQL" -u root -p"$PASSWORD" "$DB" -e "SELECT COUNT(*) AS cars_total FROM cars;"
