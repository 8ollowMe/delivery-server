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

open_terminal() {
  local dir="$1"
  local profile="$2"
  if [[ -n "$profile" ]]; then
    osascript -e "tell app \"Terminal\" to do script \"cd $dir && SPRING_PROFILES_ACTIVE=$profile ./gradlew bootRun\""
  else
    osascript -e "tell app \"Terminal\" to do script \"cd $dir && ./gradlew bootRun\""
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

wait_for_delivery_port() {
  local max_try="${1:-60}"
  echo "Waiting for delivery-server port: 127.0.0.1:{10005|8081}"
  for ((i=1; i<=max_try; i++)); do
    if nc -z 127.0.0.1 10005 >/dev/null 2>&1; then
      echo "  [OK] delivery-server port is open (10005)"
      return 0
    fi
    if nc -z 127.0.0.1 8081 >/dev/null 2>&1; then
      echo "  [OK] delivery-server port is open (8081)"
      return 0
    fi
    sleep 2
  done
  echo "  [FAIL] delivery-server port check failed (10005/8081)"
  echo "  힌트: delivery-server 터미널에서 APPLICATION FAILED TO START / Caused by 로그를 확인하세요."
  return 1
}

wait_for_http_status() {
  local name="$1"
  local url="$2"
  local expected="${3:-200}"
  local max_try="${4:-30}"

  echo "Waiting for $name: $url (expect $expected)"
  for ((i=1; i<=max_try; i++)); do
    local code
    code="$(curl -s -o /dev/null -w "%{http_code}" "$url" || true)"
    if [[ "$code" == "$expected" ]]; then
      echo "  [OK] $name HTTP $code"
      return 0
    fi
    sleep 2
  done

  echo "  [FAIL] $name check failed: expected $expected"
  return 1
}

wait_for_eureka_registration() {
  local name="$1"
  local max_try="${2:-40}"
  local upper_name
  upper_name=$(echo "$name" | tr '[:lower:]' '[:upper:]')

  echo "Waiting for $name Eureka registration..."
  for ((i=1; i<=max_try; i++)); do
    local code
    code=$(curl -s -o /dev/null -w "%{http_code}" \
      "http://localhost:8761/eureka/apps/${upper_name}" \
      -H "Accept: application/json" || true)
    if [[ "$code" == "200" ]]; then
      echo "  [OK] $name registered in Eureka"
      return 0
    fi
    sleep 2
  done

  echo "  [FAIL] $name Eureka registration timeout ($name)"
  return 1
}

check_and_start_redis() {
  echo "=== Checking Redis ==="
  if docker exec followme-redis redis-cli ping >/dev/null 2>&1; then
    echo "  [OK] Redis is already running"
    return 0
  fi
  echo "  Redis not running. Starting via Docker..."
  docker compose -f "$PROJECTS_DIR/docker-compose.yml" up -d redis
  for ((i=1; i<=15; i++)); do
    if docker exec followme-redis redis-cli ping >/dev/null 2>&1; then
      echo "  [OK] Redis started"
      return 0
    fi
    sleep 2
  done
  echo "  [FAIL] Redis failed to start. Check: docker compose -f $PROJECTS_DIR/docker-compose.yml up -d redis"
  exit 1
}

# =====================================================
# 기존 프로세스 종료
# =====================================================
echo "=== Stopping existing processes ==="
pkill -f "bootRun" 2>/dev/null || true
pkill -f "spring-boot" 2>/dev/null || true
sleep 3

check_and_start_redis

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
open_terminal "$EUREKA_DIR" ""
wait_for_port "eureka-server" "127.0.0.1" "8761" 60

echo "[2/8] Starting Config Server (13100)..."
open_terminal "$PROJECTS_DIR/config-server" ""
wait_for_port "config-server" "127.0.0.1" "13100" 60

echo "[3/8] Starting Hub Server (10001)..."
open_terminal "$PROJECTS_DIR/hub-server" "$HUB_PROFILE"
wait_for_port "hub-server" "127.0.0.1" "10001" 60
wait_for_eureka_registration "hub-server" 40

echo "[4/8] Starting User Server (8080)..."
open_terminal "$PROJECTS_DIR/user-server" ""
wait_for_port "user-server" "127.0.0.1" "8080" 60
wait_for_eureka_registration "user-server" 40

echo "[5/8] Starting Order Server (8082)..."
open_terminal "$PROJECTS_DIR/order-server" "$ORDER_PROFILE"
wait_for_port "order-server" "127.0.0.1" "8082" 60
wait_for_eureka_registration "order-server" 40

echo "[6/8] Starting Delivery Server (10005)..."
open_terminal "$PROJECTS_DIR/delivery-server" "$DELIVERY_PROFILE"
wait_for_delivery_port 60
wait_for_eureka_registration "delivery-server" 40

echo "[7/8] Starting Vendor Server (10003)..."
open_terminal "$PROJECTS_DIR/vendor-server" "$VENDOR_PROFILE"
wait_for_port "vendor-server" "127.0.0.1" "10003" 60
wait_for_eureka_registration "vendor-server" 40

echo "[8/8] Starting Gateway Server (8000)..."
open_terminal "$PROJECTS_DIR/gateway-server" ""

echo "=== Re-loading seed data after service startup ==="
PGPASSWORD="${PGPASSWORD:-followme}" psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$PROJECTS_DIR/hub-server/src/main/resources/mock-data.sql"
PGPASSWORD="${PGPASSWORD:-followme}" psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$PROJECTS_DIR/vendor-server/data.sql"

hub_stock_count="$(PGPASSWORD="${PGPASSWORD:-followme}" psql -t -A -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "select count(*) from hub.p_hub_stock;")"
vendor_product_count="$(PGPASSWORD="${PGPASSWORD:-followme}" psql -t -A -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "select count(*) from vendor.p_product;")"
hub_count="$(PGPASSWORD="${PGPASSWORD:-followme}" psql -t -A -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "select count(*) from hub.p_hub;")"
hub_route_count="$(PGPASSWORD="${PGPASSWORD:-followme}" psql -t -A -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "select count(*) from hub.p_hub_route;")"
echo "Seed check: hub_stock=$hub_stock_count, vendor_product=$vendor_product_count"
echo "Seed check: hub=$hub_count, hub_route=$hub_route_count"
if [[ "$hub_stock_count" == "0" || "$vendor_product_count" == "0" || "$hub_count" == "0" || "$hub_route_count" == "0" ]]; then
  echo "[FAIL] seed data is empty after startup. check each server ddl-auto/config profile."
  exit 1
fi

echo "=== Verifying hub route API ==="
# vendor-server가 Eureka에 등록됐어도 hub-server의 Eureka 캐시에 반영되기까지
# 약 30초가 필요합니다. 이 시간 전에 hub→vendor Feign 호출이 실패하면
# Resilience4j 서킷브레이커가 OPEN 되어 이후 요청도 차단됩니다.
echo "  Waiting 30s for Eureka cache propagation (hub-server ← vendor-server)..."
sleep 30

wait_for_http_status \
  "vendor internal API" \
  "http://localhost:10003/internal/v1/vendors/90000000-0000-0000-0000-000000000004" \
  "200" \
  "45" || { echo "[FAIL] vendor API not ready"; exit 1; }

wait_for_http_status \
  "hub route API" \
  "http://localhost:10001/api/hubs/route?sourceHubId=11111111-1111-1111-1111-111111111111&vendorId=90000000-0000-0000-0000-000000000004" \
  "200" \
  "45" || {
    echo "[WARN] hub route API check failed after retries."
    echo "       start-all은 계속 진행합니다. test-feign.sh에서 재검증하세요."
  }

echo ""
echo "=== All services started ==="
echo "Eureka Dashboard : http://localhost:8761"
echo "Config Server    : http://localhost:13100"
echo "Gateway          : http://localhost:8000"