#!/bin/sh

NC='\033[0m'
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
DEBUG=${DEBUG:-true}

[ ! -d migrations ] && echo "No migrations directory found" && exit 1
migrations=$(find migrations -type f -name '*.sql' | sort -n)
[ -z "$migrations" ] && echo "No migrations found" && exit 1

host=127.0.0.1
port=5432
user=root
password=root
database=paw

if [ "$DEBUG" = false ]; then
  production=webapp/src/main/resources/config/production.properties
  [ -f "$production" ] || { echo "No production properties file found" && exit 1; }

  while IFS='=' read -r key value; do
      case "$key" in
          "db.host") host=$value ;;
          "db.port") port=$value ;;
          "db.username") user=$value ;;
          "db.password") password=$value ;;
          "db.database") database=$value ;;
      esac
  done < "$production"
fi

log_file=migrations/migrations-$(date +%Y-%m-%d-%H-%M).log
log_file_tmp=migrations/migrations-tmp.log
[ -f $log_file ] && rm $log_file

migrations_query="SELECT * FROM migrations WHERE id="

export PGPASSWORD=$password

function check_migrations() {
    lines="$(psql -h "$host" -p "$port" -U "$user" -d "$database" -c "${migrations_query}$1" 2>/dev/null | wc -l)"
    [ $lines -gt 4 ] && return 0 || return 1
}

for migration in $migrations; do
    echo -n "Applying: $migration ... "

    id=$(echo "$migration" | grep -oE '[0-9]{2,}')
    check_migrations $id && echo -e "${YELLOW}SKIPPED${NC}" && continue

    printf "\\n\\nMigration $migration\\n" >> $log_file
    psql -h "$host" -p "$port" -U "$user" -d "$database" 2>&1 &>"$log_file_tmp" < "$migration"

    cat "$log_file_tmp" >> $log_file
    cat "$log_file_tmp" | grep -q "ERROR" && echo -e "${YELLOW}WARNINGS${NC}" && continue
    check_migrations $id && echo -e "${GREEN}OK${NC}" || echo -e "${RED}FAILED${NC}"
done

[ -f "$log_file_tmp" ] && rm "$log_file_tmp"
[ -f "$log_file" ] && echo -e "\\nMigration finished. Check $log_file for more details."
