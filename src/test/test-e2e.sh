#!/bin/bash
# =============================================================
# E2E 회귀 테스트
# - 목적: 사용자 관점의 배송 워크플로(주문→배송→구간상태→취소/삭제) 검증
# - 범위: 도메인 플로우 정상/오류 케이스 확인 (세부 Feign 경로 진단은 test-feign.sh 담당)
# - 사전 조건: Gateway, Order, Delivery, Keycloak 서버 기동 완료
# 실행: ./test-e2e.sh
# =============================================================

set -uo pipefail

# ── 설정 ──────────────────────────────────────────────────────
GATEWAY="http://localhost:8000"
DELIVERY="http://localhost:10005"
KEYCLOAK="http://localhost:8090"
REALM="followme"
CLIENT_ID="followme-client"
CLIENT_SECRET="AFUcuO5HZsWcJP2Y7pfPhJYqKTL5L0gA"

TEST_USERNAME="testUser"
TEST_PASSWORD="Test1234!"

SOURCE_HUB_ID="11111111-1111-1111-1111-111111111111"
VENDOR_ID="90000000-0000-0000-0000-000000000004"
PRODUCT_ID="aaaaaaaa-0000-0000-0000-000000000001"
USER_ID="00000000-0000-0000-0000-000000000001"

# ── 출력 헬퍼 ─────────────────────────────────────────────────
GREEN="\033[0;32m"; RED="\033[0;31m"; YELLOW="\033[1;33m"
GRAY="\033[0;90m"; NC="\033[0m"
PASS=0; FAIL=0

pass()  { echo -e "${GREEN}[PASS]${NC} $1"; PASS=$((PASS+1)); }
fail()  { echo -e "${RED}[FAIL]${NC} $1"; FAIL=$((FAIL+1)); }
step()  { echo -e "\n${YELLOW}▶ $1${NC}"; }

assert_success() {
  local label="$1" body="$2"
  local ok
  ok=$(echo "$body" | jq -r '.success // false' 2>/dev/null)
  echo -e "  ${GRAY}$(echo "$body" | jq -c '.' 2>/dev/null || echo "$body")${NC}"
  if [[ "$ok" == "true" ]]; then pass "$label"
  else fail "$label → $(echo "$body" | jq -r '.error.message // .error // .' 2>/dev/null)"; fi
}

assert_fail() {
  local label="$1" body="$2"
  local ok
  ok=$(echo "$body" | jq -r '.success // false' 2>/dev/null)
  echo -e "  ${GRAY}$(echo "$body" | jq -c '.' 2>/dev/null || echo "$body")${NC}"
  if [[ "$ok" == "false" ]]; then pass "$label (예상된 실패)"
  else fail "$label → 실패해야 하는데 성공함"; fi
}

assert_field() {
  local label="$1" value="$2"
  echo -e "  ${GRAY}$label: ${value:-"(없음)"}${NC}"
  if [[ -n "$value" && "$value" != "null" ]]; then pass "$label 존재"
  else fail "$label 없음"; fi
}

assert_eq() {
  local label="$1" actual="$2" expected="$3"
  echo -e "  ${GRAY}$label: 예상=$expected / 실제=${actual:-"(없음)"}${NC}"
  if [[ "$actual" == "$expected" ]]; then pass "$label"
  else fail "$label → 예상: $expected / 실제: $actual"; fi
}

# ── 0. 헬스 체크 ──────────────────────────────────────────────
step "0. 서비스 헬스 체크"

check_health() {
  local name="$1" url="$2"
  local status
  status=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")
  if [[ "$status" != "000" ]]; then pass "$name (HTTP $status)"
  else fail "$name 응답 없음 ($url)"; fi
}

check_health "Gateway   :8000" "$GATEWAY"
check_health "Order     :8082" "http://localhost:8082/actuator/health"
check_health "Delivery  :10005" "$DELIVERY/actuator/health"
check_health "Keycloak  :8090" "$KEYCLOAK/realms/$REALM"

# ── 1. 로그인 ──────────────────────────────────────────────────
step "1. 로그인 ($TEST_USERNAME)"

TOKEN_BODY=$(curl -s -X POST "$KEYCLOAK/realms/$REALM/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&username=$TEST_USERNAME&password=$TEST_PASSWORD" \
  || echo '{}')

ACCESS_TOKEN=$(echo "$TOKEN_BODY" | jq -r '.access_token // empty')
assert_field "액세스 토큰" "$ACCESS_TOKEN"

if [[ -z "$ACCESS_TOKEN" ]]; then
  echo -e "${RED}토큰 발급 실패 → 테스트 중단${NC}"
  echo -e "결과: ${GREEN}PASS $PASS${NC} / ${RED}FAIL $FAIL${NC}"
  exit 1
fi

# ── 2. 주문 생성 ───────────────────────────────────────────────
step "2. 주문 생성"

ORDER_CREATE=$(curl -s -w "\n__HTTP_STATUS__%{http_code}" \
  -X POST "$GATEWAY/api/v1/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{
    \"productId\": \"$PRODUCT_ID\",
    \"productName\": \"테스트상품\",
    \"requestVendorId\": \"90000000-0000-0000-0000-000000000001\",
    \"requestVendorName\": \"서울구매업체\",
    \"requestVendorHubId\": \"$SOURCE_HUB_ID\",
    \"requestVendorHubName\": \"서울허브\",
    \"receiverVendorId\": \"$VENDOR_ID\",
    \"receiverVendorName\": \"부산도착업체\",
    \"receiverVendorHubId\": \"44444444-4444-4444-4444-444444444444\",
    \"receiverVendorHubName\": \"부산허브\",
    \"quantity\": 1,
    \"requestNote\": \"e2e 테스트\"
  }" || echo "__HTTP_STATUS__000")

HTTP_STATUS=$(echo "$ORDER_CREATE" | sed -n 's/^__HTTP_STATUS__//p')
ORDER_CREATE_BODY=$(echo "$ORDER_CREATE" | sed '/^__HTTP_STATUS__/d')

if [[ "$HTTP_STATUS" == "200" || "$HTTP_STATUS" == "201" ]]; then
  pass "주문 생성 (HTTP $HTTP_STATUS)"
else
  fail "주문 생성 (HTTP $HTTP_STATUS)"
  echo -e "  ${GRAY}주문 생성 응답: $(echo "$ORDER_CREATE_BODY" | jq -c '.' 2>/dev/null || echo "$ORDER_CREATE_BODY")${NC}"
  echo -e "${RED}주문 생성 실패 → 테스트 중단${NC}"
  echo -e "결과: ${GREEN}PASS $PASS${NC} / ${RED}FAIL $FAIL${NC}"
  exit 1
fi

sleep 1
ORDER_ID=$(echo "$ORDER_CREATE_BODY" | jq -r '.data.orderId // empty' 2>/dev/null || echo "")
if [[ -z "$ORDER_ID" ]]; then
  ORDER_LIST=$(curl -s "$GATEWAY/api/v1/orders?size=1" \
    -H "Authorization: Bearer $ACCESS_TOKEN" || echo '{"success":false}')
  ORDER_ID=$(echo "$ORDER_LIST" | jq -r '.data.content[0].orderId // .data[0].orderId // empty' 2>/dev/null || echo "")
fi
assert_field "orderId" "$ORDER_ID"

# ── 3. 배송 자동 생성 확인 ────────────────────────────────────
step "3. 배송 자동 생성 확인"
sleep 2

DELIVERY_BY_ORDER=$(curl -s "$DELIVERY/api/v1/deliveries/order/$ORDER_ID" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false}')

DELIVERY_OK=$(echo "$DELIVERY_BY_ORDER" | jq -r '.success // false' 2>/dev/null || echo "false")
if [[ "$DELIVERY_OK" == "true" ]]; then
  pass "주문 연동 배송 조회"
  DELIVERY_ID=$(echo "$DELIVERY_BY_ORDER" | jq -r '.data.id // empty')
else
  echo -e "  ${YELLOW}[WARN]${NC} 주문 연동 배송 조회 실패 (목록 기반 보정 시도)"
  echo -e "  ${GRAY}$(echo "$DELIVERY_BY_ORDER" | jq -c '.' 2>/dev/null || echo "$DELIVERY_BY_ORDER")${NC}"
  # 일부 환경에서 /order/{orderId} 조회가 실패해도 실제 배송은 생성되므로 목록 기반으로 보정 조회
  DELIVERY_LIST_BY_ORDER=$(curl -s "$DELIVERY/api/v1/deliveries?size=50" \
    -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false}')
  DELIVERY_ID=$(echo "$DELIVERY_LIST_BY_ORDER" | jq -r --arg oid "$ORDER_ID" '.data.content[]? | select(.orderId == $oid) | .id' | head -n 1)
fi
assert_field "deliveryId" "$DELIVERY_ID"
echo -e "  ${GRAY}배송 상태: $(echo "$DELIVERY_BY_ORDER" | jq -r '.data.status // empty')${NC}"

# ── 4. 구간 목록 조회 ─────────────────────────────────────────
step "4. 배송 구간 목록 조회"

SHIPMENTS=$(curl -s "$DELIVERY/api/v1/deliveries/$DELIVERY_ID/shipments" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false,"data":[]}')

assert_success "구간 목록 조회" "$SHIPMENTS"
SHIPMENT_COUNT=$(echo "$SHIPMENTS" | jq '.data | length // 0')
echo -e "  ${GRAY}총 구간 수: $SHIPMENT_COUNT${NC}"

if [[ "$SHIPMENT_COUNT" -eq 0 ]]; then
  fail "구간이 없습니다"
fi

# ── 5. 배송자 권한 검증 ─────────────────────────────────────────
step "5. 배송자 권한 검증 (본인 성공 / 타인 실패)"

FIRST_SID=$(echo "$SHIPMENTS" | jq -r '.data[0].id // empty')
FIRST_MANAGER_ID=$(echo "$SHIPMENTS" | jq -r '.data[0].deliveryManager.id // empty')
OUTSIDER_ID="$(python3 -c "import uuid; print(uuid.uuid4())")"

assert_field "첫 구간 shipmentId" "$FIRST_SID"
assert_field "첫 구간 deliveryManagerId" "$FIRST_MANAGER_ID"

if [[ -n "$FIRST_SID" && -n "$FIRST_MANAGER_ID" ]]; then
  # 배송자 외 사용자(DELIVERY role) 상태 변경 시도 → 실패해야 정상
  OUTSIDER_CHANGE=$(curl -s -X PATCH "$DELIVERY/api/v1/shipments/$FIRST_SID/status" \
    -H "Content-Type: application/json" \
    -H "X-User-Id: $OUTSIDER_ID" -H "X-Role: DELIVERY" \
    -d '{"status":"SHIPPED"}' || echo '{"success":false}')
  assert_fail "배송자 외 상태 변경 거부" "$OUTSIDER_CHANGE"

  # 담당 배송자 본인 상태 변경 시도 → 성공해야 정상
  MANAGER_CHANGE=$(curl -s -X PATCH "$DELIVERY/api/v1/shipments/$FIRST_SID/status" \
    -H "Content-Type: application/json" \
    -H "X-User-Id: $FIRST_MANAGER_ID" -H "X-Role: DELIVERY" \
    -d '{"status":"SHIPPED"}' || echo '{"success":false}')
  assert_success "담당 배송자 상태 변경 허용" "$MANAGER_CHANGE"
else
  fail "배송자 권한 검증 준비 실패 (shipment/manager 정보 부족)"
fi

# ── 6. 구간 상태 전환 ─────────────────────────────────────────
step "6. 구간 상태 전환 (SHIPPED → IN_TRANSIT → ARRIVED → COMPLETED)"

change_status() {
  local sid="$1" to_status="$2"
  local body
  body=$(curl -s -X PATCH "$DELIVERY/api/v1/shipments/$sid/status" \
    -H "Content-Type: application/json" \
    -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" \
    -d "{\"status\":\"$to_status\"}" || echo '{"success":false}')
  assert_success "구간 → $to_status" "$body"
}

while IFS= read -r sid; do
  echo -e "  ${GRAY}구간 $sid 전환 중...${NC}"
  if [[ "$sid" != "$FIRST_SID" ]]; then
    change_status "$sid" "SHIPPED"
  fi
  change_status "$sid" "IN_TRANSIT"
  change_status "$sid" "ARRIVED"
  change_status "$sid" "COMPLETED"
  sleep 1
done < <(echo "$SHIPMENTS" | jq -r '.data[].id')

# ── 7. 최종 배송 상태 확인 ────────────────────────────────────
step "7. 최종 배송 상태 확인"

# internal API는 ApiResponse 래퍼 없이 raw 응답 반환
FINAL=$(curl -s "$DELIVERY/internal/v1/deliveries/$DELIVERY_ID/status" || echo '{}')
FINAL_STATUS=$(echo "$FINAL" | jq -r '.deliveryStatus // empty')
assert_eq "최종 배송 상태 = COMPLETED" "$FINAL_STATUS" "COMPLETED"

# ── 8. 배송 목록/단건 조회 ───────────────────────────────────
step "8. 배송 조회"

LIST=$(curl -s "$DELIVERY/api/v1/deliveries" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false}')
assert_success "배송 목록 조회" "$LIST"
echo -e "  ${GRAY}총 배송 수: $(echo "$LIST" | jq -r '.data.totalElements // (.data | length) // 0')${NC}"

DETAIL=$(curl -s "$DELIVERY/api/v1/deliveries/$DELIVERY_ID" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false}')
assert_success "배송 단건 조회" "$DETAIL"
assert_eq "단건 조회 상태 = COMPLETED" \
  "$(echo "$DETAIL" | jq -r '.data.status // empty')" "COMPLETED"

# ── 9. 잘못된 상태 전환 거부 ──────────────────────────────────
step "9. 잘못된 상태 전환 거부 (COMPLETED → SHIPPED)"

INVALID=$(curl -s -X PATCH "$DELIVERY/api/v1/shipments/$FIRST_SID/status" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" \
  -d '{"status":"SHIPPED"}' || echo '{"success":false}')
assert_fail "COMPLETED → SHIPPED 거부" "$INVALID"

# ── 10. 배송 취소/삭제 워크플로 ───────────────────────────────
step "10. 새 배송 생성 (취소/삭제 테스트용)"

# internal API는 raw 응답 (ApiResponse 래퍼 없음)
NEW_DELIVERY=$(curl -s -X POST "$DELIVERY/internal/v1/deliveries" \
  -H "Content-Type: application/json" \
  -d "{\"orderId\":\"$(python3 -c "import uuid; print(uuid.uuid4())")\",\"sourceHubId\":\"$SOURCE_HUB_ID\",\"vendorId\":\"$VENDOR_ID\"}" \
  || echo '{}')

CANCEL_DELIVERY_ID=$(echo "$NEW_DELIVERY" | jq -r '.deliveryId // empty')
assert_field "취소용 deliveryId" "$CANCEL_DELIVERY_ID"

step "11. 배송 취소"

CANCEL_RESULT=$(curl -s -X PATCH "$DELIVERY/api/v1/deliveries/$CANCEL_DELIVERY_ID/cancel" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false}')
assert_success "배송 취소" "$CANCEL_RESULT"

AFTER_CANCEL=$(curl -s "$DELIVERY/api/v1/deliveries/$CANCEL_DELIVERY_ID" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false}')
assert_eq "취소 후 상태 = CANCELLED" \
  "$(echo "$AFTER_CANCEL" | jq -r '.data.status // empty')" "CANCELLED"

step "12. 취소된 배송 삭제"

DELETE_RESULT=$(curl -s -X DELETE "$DELIVERY/api/v1/deliveries/$CANCEL_DELIVERY_ID" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false}')
assert_success "취소된 배송 삭제" "$DELETE_RESULT"

DELETED_HTTP=$(curl -s -o /dev/null -w "%{http_code}" \
  "$DELIVERY/api/v1/deliveries/$CANCEL_DELIVERY_ID" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER")
if [[ "$DELETED_HTTP" == "404" ]]; then pass "삭제 후 404 확인"
else fail "삭제 후 조회 가능 (HTTP $DELETED_HTTP)"; fi

# ── 13. 오류 케이스 ────────────────────────────────────────────
step "13. 오류 케이스"

# COMPLETED 배송 취소 거부
assert_fail "COMPLETED 배송 취소 거부" \
  "$(curl -s -X PATCH "$DELIVERY/api/v1/deliveries/$DELIVERY_ID/cancel" \
    -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false}')"

# 이미 삭제된 배송 재취소 거부
assert_fail "삭제된 배송 재취소 거부" \
  "$(curl -s -X PATCH "$DELIVERY/api/v1/deliveries/$CANCEL_DELIVERY_ID/cancel" \
    -H "X-User-Id: $USER_ID" -H "X-Role: MASTER" || echo '{"success":false}')"

# 없는 리소스 404
FAKE_HTTP=$(curl -s -o /dev/null -w "%{http_code}" \
  "$DELIVERY/api/v1/deliveries/99999999-9999-9999-9999-999999999999" \
  -H "X-User-Id: $USER_ID" -H "X-Role: MASTER")
if [[ "$FAKE_HTTP" == "404" ]]; then pass "없는 배송 조회 → 404"
else fail "없는 배송 조회 → 예상 404, 실제 $FAKE_HTTP"; fi

# ── 결과 ──────────────────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "  결과: ${GREEN}PASS $PASS${NC} / ${RED}FAIL $FAIL${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
[[ $FAIL -eq 0 ]]
