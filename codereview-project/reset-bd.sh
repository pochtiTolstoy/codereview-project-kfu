#!/bin/bash
set -e

DB_NAME="codereview-db"
DB_OWNER="reviewuser"
DB_PASS="${DB_PASS:-reviewpass}"

SCRIPT_PATH="$(readlink -f "$0")"
SCRIPT_DIR="$(dirname "$SCRIPT_PATH")"
INIT_SQL="$SCRIPT_DIR/init.sql"
SEED_SQL="$SCRIPT_DIR/seed.sql"

cd /tmp

if [ ! -f "$INIT_SQL" ]; then
  echo "Не найден init.sql по пути: $INIT_SQL"
  exit 1
fi

export PGPASSWORD="$DB_PASS"

echo "Создание/обновление пользователя $DB_OWNER..."
sudo -u postgres psql -v ON_ERROR_STOP=1 -c "
DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$DB_OWNER') THEN
      CREATE ROLE $DB_OWNER LOGIN PASSWORD '$DB_PASS';
   ELSE
      ALTER ROLE $DB_OWNER WITH LOGIN PASSWORD '$DB_PASS';
   END IF;
END
\$\$;
"

echo "Остановка активных сессий для базы $DB_NAME..."
sudo -u postgres psql -v ON_ERROR_STOP=1 -c "
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = '$DB_NAME';
" || true

echo "Дроп базы $DB_NAME (если существует)..."
sudo -u postgres psql -v ON_ERROR_STOP=1 -c "DROP DATABASE IF EXISTS \"$DB_NAME\";"

echo "Создание базы $DB_NAME с владельцем $DB_OWNER..."
sudo -u postgres psql -v ON_ERROR_STOP=1 -c "CREATE DATABASE \"$DB_NAME\" OWNER $DB_OWNER;"

echo "Применение init.sql..."
psql -h localhost -U "$DB_OWNER" -d "$DB_NAME" -f "$INIT_SQL"

if [ -f "$SEED_SQL" ]; then
  echo "Применение seed.sql..."
  psql -h localhost -U "$DB_OWNER" -d "$DB_NAME" -f "$SEED_SQL"
fi

echo "База $DB_NAME пересобрана из init.sql (+ seed.sql если был)"
