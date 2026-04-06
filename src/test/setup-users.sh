#!/bin/bash

# =====================================================
# 통합 테스트용 유저 세팅 스크립트
# - Eureka/Feign 실통신 테스트 전 대량 사용자 데이터 생성
# - 실행 전 Gateway, Keycloak, DB가 기동되어 있어야 합니다.
# =====================================================

set -euo pipefail

on_error() {
  local exit_code=$?
  echo "  [ERROR] line $1: '$2' (exit=$exit_code)"
}
trap 'on_error "$LINENO" "$BASH_COMMAND"' ERR

GATEWAY="${GATEWAY:-http://localhost:8000}"
KEYCLOAK="${KEYCLOAK:-http://localhost:8090}"
REALM="${REALM:-followme}"
CLIENT_ID="${CLIENT_ID:-followme-client}"
CLIENT_SECRET="${CLIENT_SECRET:-AFUcuO5HZsWcJP2Y7pfPhJYqKTL5L0gA}"
KC_ADMIN="${KC_ADMIN:-admin}"
KC_ADMIN_PASS="${KC_ADMIN_PASS:-admin}"
DB_USER="${DB_USER:-followme}"
DB_NAME="${DB_NAME:-followme}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-5432}"
DB_USER_TABLE="${DB_USER_TABLE:-users.p_user}"

MASTER_USERNAME="${MASTER_USERNAME:-master01}"
MASTER_PASSWORD="${MASTER_PASSWORD:-Master1234!}"

# 데이터 규모 조절 파라미터
HUB_DELIVERY_COUNT="${HUB_DELIVERY_COUNT:-10}"
VENDORS_PER_HUB="${VENDORS_PER_HUB:-10}"
VENDOR_DELIVERY_PER_HUB="${VENDOR_DELIVERY_PER_HUB:-10}"
RUN_TAG="${RUN_TAG:-$(date +%m%d%H%M)}"

HUB_IDS=(
  "11111111-1111-1111-1111-111111111111"
  "22222222-2222-2222-2222-222222222222"
  "33333333-3333-3333-3333-333333333333"
  "44444444-4444-4444-4444-444444444444"
  "55555555-5555-5555-5555-555555555555"
  "66666666-6666-6666-6666-666666666666"
)

echo ""
echo "======================================================"
echo "  통합 테스트 유저 세팅 시작"
echo "  RUN_TAG=$RUN_TAG"
echo "  HUB_DELIVERY_COUNT=$HUB_DELIVERY_COUNT"
echo "  VENDORS_PER_HUB=$VENDORS_PER_HUB"
echo "  VENDOR_DELIVERY_PER_HUB=$VENDOR_DELIVERY_PER_HUB"
echo "======================================================"

require_cmd() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "  [ERROR] '$cmd' 명령이 필요합니다. 설치 후 다시 실행하세요."
    exit 1
  fi
}

preflight_check() {
  local gw kc
  gw=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY/actuator/health" 2>/dev/null || echo "000")
  kc=$(curl -s -o /dev/null -w "%{http_code}" "$KEYCLOAK/realms/$REALM" 2>/dev/null || echo "000")

  if [[ "$gw" == "000" ]]; then
    echo "  [ERROR] Gateway 연결 실패: $GATEWAY"
    exit 1
  fi
  if [[ "$kc" == "000" ]]; then
    echo "  [ERROR] Keycloak 연결 실패: $KEYCLOAK/realms/$REALM"
    exit 1
  fi
}

register_master_if_needed() {
  local status
  status=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY/api/v1/users/register" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"$MASTER_USERNAME\",
      \"password\": \"$MASTER_PASSWORD\",
      \"name\": \"Master Admin\",
      \"slackId\": \"master-$RUN_TAG\",
      \"role\": \"MASTER\"
    }" 2>/dev/null || echo "000")
  if [[ "$status" == "200" || "$status" == "201" || "$status" == "409" ]]; then
    echo "  [OK/$status] master 사용자 준비"
  elif [[ "$status" == "000" ]]; then
    echo "  [ERROR/000] master 사용자 등록 요청 실패 (Gateway 연결 확인: $GATEWAY)"
    exit 1
  else
    echo "  [ERROR/$status] master 사용자 등록 실패"
    exit 1
  fi
}

build_master_and_token() {
  local kc_admin_token master_kc_id master_role_id

  kc_admin_token=$(curl -s -X POST "$KEYCLOAK/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password&client_id=admin-cli&username=$KC_ADMIN&password=$KC_ADMIN_PASS" \
    | jq -r '.access_token')

  if [[ -z "$kc_admin_token" || "$kc_admin_token" == "null" ]]; then
    echo "  [ERROR] Keycloak admin 토큰 발급 실패"
    exit 1
  fi

  master_kc_id=$(curl -s "$KEYCLOAK/admin/realms/$REALM/users?username=$MASTER_USERNAME" \
    -H "Authorization: Bearer $kc_admin_token" \
    | jq -r '.[0].id // empty')

  if [[ -z "$master_kc_id" ]]; then
    echo "  [ERROR] Keycloak에서 master 유저를 찾을 수 없습니다"
    exit 1
  fi

  curl -s -o /dev/null -X PUT "$KEYCLOAK/admin/realms/$REALM/users/$master_kc_id" \
    -H "Authorization: Bearer $kc_admin_token" \
    -H "Content-Type: application/json" \
    -d '{"enabled":true}'

  master_role_id=$(curl -s "$KEYCLOAK/admin/realms/$REALM/roles/MASTER" \
    -H "Authorization: Bearer $kc_admin_token" \
    | jq -r '.id // empty')

  if [[ -n "$master_role_id" ]]; then
    curl -s -o /dev/null -X POST "$KEYCLOAK/admin/realms/$REALM/users/$master_kc_id/role-mappings/realm" \
      -H "Authorization: Bearer $kc_admin_token" \
      -H "Content-Type: application/json" \
      -d "[{\"id\":\"$master_role_id\",\"name\":\"MASTER\"}]"
  fi

  psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c \
    "UPDATE $DB_USER_TABLE SET status='APPROVED' WHERE username='$MASTER_USERNAME';" >/dev/null

  MASTER_TOKEN=$(curl -s -X POST "$KEYCLOAK/realms/$REALM/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&username=$MASTER_USERNAME&password=$MASTER_PASSWORD" \
    | jq -r '.access_token')

  if [[ -z "$MASTER_TOKEN" || "$MASTER_TOKEN" == "null" ]]; then
    echo "  [ERROR] master 토큰 발급 실패"
    exit 1
  fi
}

register_user() {
  local payload="$1"
  local label="$2"
  local status

  status=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY/api/v1/users/register" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $MASTER_TOKEN" \
    -d "$payload")

  if [[ "$status" == "200" || "$status" == "201" || "$status" == "409" ]]; then
    echo "  [OK/$status] $label"
  else
    echo "  [WARN/$status] $label"
  fi
}

vendor_id_for() {
  local hub_index="$1"
  local seq="$2"
  local head tail
  head=$(printf "%08x" $((0x90000000 + hub_index * 0x100 + seq)))
  tail=$(printf "%012x" $((hub_index * 1000 + seq)))
  echo "$head-0000-0000-0000-$tail"
}

approve_pending_users() {
  local page=0
  while true; do
    local pending_ids
    pending_ids=$(curl -s "$GATEWAY/api/v1/users?size=200&page=$page" \
      -H "Authorization: Bearer $MASTER_TOKEN" \
      | jq -r '.data.content[]? | select(.status == "PENDING") | .userId')

    if [[ -z "$pending_ids" ]]; then
      break
    fi

    for user_id in $pending_ids; do
      curl -s -o /dev/null -X PATCH "$GATEWAY/api/v1/users/$user_id/status" \
        -H "Authorization: Bearer $MASTER_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"status":"APPROVED"}'
      echo "  [APPROVED] $user_id"
    done

    page=$((page + 1))
  done
}

echo ""
require_cmd curl
require_cmd jq
require_cmd psql
preflight_check

echo ""
echo "[1/4] master 사용자 준비"
register_master_if_needed

echo ""
echo "[2/4] master 권한/토큰 준비"
build_master_and_token

echo ""
echo "[3/4] 테스트용 사용자 생성"

# 허브 관리자 1명씩
for idx in "${!HUB_IDS[@]}"; do
  hub_no=$((idx + 1))
  hub_id="${HUB_IDS[$idx]}"
  register_user "{\"username\":\"hub${hub_no}_${RUN_TAG}\",\"password\":\"Hub1234!\",\"name\":\"HubManager${hub_no}\",\"slackId\":\"hub-${hub_no}-${RUN_TAG}\",\"role\":\"HUB\",\"hubId\":\"$hub_id\"}" \
    "허브 관리자 hub=$hub_no"
done

# 허브 소속 배송 담당자 10명
for i in $(seq 1 "$HUB_DELIVERY_COUNT"); do
  hub_index=$(( (i - 1) % ${#HUB_IDS[@]} ))
  hub_id="${HUB_IDS[$hub_index]}"
  register_user "{\"username\":\"hubdeliver_${i}_${RUN_TAG}\",\"password\":\"Deliver1!\",\"name\":\"HubDeliver${i}\",\"slackId\":\"hub-deliver-${i}-${RUN_TAG}\",\"role\":\"DELIVERY\",\"hubId\":\"$hub_id\"}" \
    "허브 배송 담당자 #$i"
done

# 허브별 업체 10명 + 업체별 배송 담당자 10명
for idx in "${!HUB_IDS[@]}"; do
  hub_no=$((idx + 1))
  hub_id="${HUB_IDS[$idx]}"

  for i in $(seq 1 "$VENDORS_PER_HUB"); do
    vendor_id=$(vendor_id_for "$hub_no" "$i")
    register_user "{\"username\":\"vendor${hub_no}_${i}_${RUN_TAG}\",\"password\":\"Vendor12!\",\"name\":\"Vendor${hub_no}_${i}\",\"slackId\":\"vendor-${hub_no}-${i}-${RUN_TAG}\",\"role\":\"VENDOR\",\"hubId\":\"$hub_id\",\"vendorId\":\"$vendor_id\"}" \
      "허브$hub_no 업체 #$i"
  done

  for i in $(seq 1 "$VENDOR_DELIVERY_PER_HUB"); do
    vendor_id=$(vendor_id_for "$hub_no" "$i")
    register_user "{\"username\":\"vdeliver${hub_no}_${i}_${RUN_TAG}\",\"password\":\"Deliver1!\",\"name\":\"VendorDeliver${hub_no}_${i}\",\"slackId\":\"v-deliver-${hub_no}-${i}-${RUN_TAG}\",\"role\":\"DELIVERY\",\"hubId\":\"$hub_id\",\"vendorId\":\"$vendor_id\"}" \
      "허브$hub_no 업체 배송 담당자 #$i"
  done
done

echo ""
echo "[4/4] PENDING 사용자 일괄 승인"
approve_pending_users

echo ""
echo "======================================================"
echo "  유저 세팅 완료"
echo "  master 계정: $MASTER_USERNAME / $MASTER_PASSWORD"
echo "======================================================"
