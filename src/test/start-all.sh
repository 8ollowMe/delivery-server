#!/bin/bash
set -euo pipefail

PROJECTS_DIR="/Users/annie/Desktop/projects"
EUREKA_DIR="/Users/annie/eureka-server"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-5432}"
DB_USER="${DB_USER:-followme}"
DB_NAME="${DB_NAME:-followme}"

# 프로파일 설정
# - 기본값: local
# - 예시: DELIVERY_PROFILE=feign-test ./src/test/start-all.sh
HUB_PROFILE="${HUB_PROFILE:-local}"
ORDER_PROFILE="${ORDER_PROFILE:-local}"
DELIVERY_PROFILE="${DELIVERY_PROFILE:-local}"
VENDOR_PROFILE="${VENDOR_PROFILE:-local}"

profile_args() {
  local profile="$1"
  if [[ -n "$profile" ]]; then
    echo "--args='--spring.profiles.active=$profile'"
  else
    echo ""
  fi
}

wait_for_health() {
  local name="$1"
  local url="$2"
  local max_try="${3:-20}"

  echo "Waiting for $name health: $url"
  for ((i=1; i<=max_try; i++)); do
    local code
    code="$(curl -s -o /dev/null -w "%{http_code}" "$url" || true)"
    if [[ "$code" == "200" ]]; then
      echo "  [OK] $name is healthy"
      return 0
    fi
    sleep 2
  done

  echo "  [FAIL] $name health check failed ($url)"
  return 1
}

wait_for_port() {
  local name="$1"
  local host="$2"
  local port="$3"
  local max_try="${4:-40}"

  echo "Waiting for $name port: $host:$port"
  for ((i=1; i<=max_try; i++)); do
    if nc -z "$host" "$port" >/dev/null 2>&1; then
      echo "  [OK] $name port is open"
      return 0
    fi
    sleep 2
  done

  echo "  [FAIL] $name port check failed ($host:$port)"
  return 1
}

# =====================================================
# 기존 프로세스 종료
# =====================================================
echo "=== Stopping existing processes ==="
pkill -f "bootRun" 2>/dev/null || true
pkill -f "spring-boot" 2>/dev/null || true
sleep 3

# =====================================================
# 실행 권한 부여
# =====================================================
echo "=== Granting execute permissions ==="
chmod +x $EUREKA_DIR/gradlew
chmod +x $PROJECTS_DIR/config-server/gradlew
chmod +x $PROJECTS_DIR/hub-server/gradlew
chmod +x $PROJECTS_DIR/user-server/gradlew
chmod +x $PROJECTS_DIR/order-server/gradlew
chmod +x $PROJECTS_DIR/delivery-server/gradlew
chmod +x $PROJECTS_DIR/vendor-server/gradlew
chmod +x $PROJECTS_DIR/gateway-server/gradlew

# =====================================================
# DB 테이블 초기화 (delivery 스키마)
# =====================================================
echo "=== Initializing DB tables ==="
PGPASSWORD="${PGPASSWORD:-followme}" psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$PROJECTS_DIR/docker/init.sql"
echo "DB init done."

echo "=== Loading hub mock data ==="
PGPASSWORD="${PGPASSWORD:-followme}" psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$PROJECTS_DIR/hub-server/src/main/resources/mock-data.sql"
echo "Hub mock data done."

echo "=== Loading vendor seed data ==="
PGPASSWORD="${PGPASSWORD:-followme}" psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$PROJECTS_DIR/vendor-server/data.sql"
echo "Vendor seed data done."

echo "=== Verifying critical seed data ==="
vendor_exists="$(PGPASSWORD="${PGPASSWORD:-followme}" psql -t -A -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "select count(*) from vendor.p_vendor where vendor_id='90000000-0000-0000-0000-000000000004';")"
if [[ "$vendor_exists" != "1" ]]; then
  echo "[FAIL] vendor seed missing: 90000000-0000-0000-0000-000000000004"
  exit 1
fi
echo "Seed verification done."

# =====================================================
# jOOQ 코드 생성 (delivery-server)
# =====================================================
echo "=== Generating jOOQ code ==="
cd $PROJECTS_DIR/delivery-server && ./gradlew generateJooq
cd $PROJECTS_DIR

# =====================================================
# 서비스 순차 시작
# =====================================================
echo "=== Starting all services ==="
echo "Profiles: hub=$HUB_PROFILE, order=$ORDER_PROFILE, delivery=$DELIVERY_PROFILE, vendor=$VENDOR_PROFILE"

echo "[1/8] Starting Eureka Server (8761)..."
osascript -e "tell app \"Terminal\" to do script \"cd $EUREKA_DIR && ./gradlew bootRun\""
wait_for_port "eureka-server" "127.0.0.1" "8761" 60

echo "[2/8] Starting Config Server (13100)..."
osascript -e "tell app \"Terminal\" to do script \"cd $PROJECTS_DIR/config-server && ./gradlew bootRun\""
wait_for_port "config-server" "127.0.0.1" "13100" 60

echo "[3/8] Starting Hub Server (10001)..."
osascript -e "tell app \"Terminal\" to do script \"cd $PROJECTS_DIR/hub-server && ./gradlew bootRun $(profile_args "$HUB_PROFILE")\""
wait_for_port "hub-server" "127.0.0.1" "10001" 60

echo "[4/8] Starting User Server (8080)..."
osascript -e "tell app \"Terminal\" to do script \"cd $PROJECTS_DIR/user-server && ./gradlew bootRun\""
sleep 8

echo "[5/8] Starting Order Server (8082)..."
osascript -e "tell app \"Terminal\" to do script \"cd $PROJECTS_DIR/order-server && ./gradlew bootRun $(profile_args "$ORDER_PROFILE")\""
wait_for_port "order-server" "127.0.0.1" "8082" 60

echo "[6/8] Starting Delivery Server (10005)..."
osascript -e "tell app \"Terminal\" to do script \"cd $PROJECTS_DIR/delivery-server && ./gradlew bootRun $(profile_args "$DELIVERY_PROFILE")\""
wait_for_port "delivery-server" "127.0.0.1" "10005" 60

echo "[7/8] Starting Vendor Server (10003)..."
osascript -e "tell app \"Terminal\" to do script \"cd $PROJECTS_DIR/vendor-server && ./gradlew bootRun $(profile_args "$VENDOR_PROFILE")\""
sleep 8

echo "[8/8] Starting Gateway Server (8000)..."
osascript -e "tell app \"Terminal\" to do script \"cd $PROJECTS_DIR/gateway-server && ./gradlew bootRun\""

echo ""
echo "=== All services started ==="
echo "Eureka Dashboard : http://localhost:8761"
echo "Config Server    : http://localhost:13100"
echo "Gateway          : http://localhost:8000"