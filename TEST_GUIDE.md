# Delivery 도메인 로컬 테스트 가이드

이 문서는 `delivery-server/src/test` 스크립트를 기준으로 로컬 테스트를 처음부터 끝까지 수행하는 절차입니다.

## 테스트 목적

| 스크립트 | 목적 |
|---|---|
| `src/test/test-e2e.sh` | 주문→배송 생성→구간 상태 전이→취소/삭제 회귀 |
| `src/test/test-feign.sh` | Delivery ↔ Hub/User/Order Feign 실통신 + fallback 검증 |

---

## 전제 조건

| 항목 | 확인 방법 |
|---|---|
| Java 17+ | `java -version` |
| Docker Desktop 실행 중 | Docker Desktop 앱 확인 |
| Redis | Docker로 자동 기동 (`start-all.sh`가 처리) |
| `jq` | `which jq` (없으면 `brew install jq`) |
| `psql` | `which psql` (없으면 `brew install libpq`) |

> `start-all.sh`가 실행 시 Redis를 자동으로 기동하지만, `redis-server` 바이너리가 없으면 실패합니다.

---

## Step 1 — Docker 기동 (PostgreSQL + Keycloak)

```bash
cd ~/Desktop/projects
docker compose -f docker-compose.yml up -d
docker compose -f docker-compose.yml ps
```

`postgres`, `keycloak` 컨테이너가 모두 `Up` 상태인지 확인합니다.

---

## Step 2 — Keycloak 초기 설정 (최초 1회만)

1. `http://localhost:8090` 접속 (`admin` / `admin`)
2. Realm 생성: `followme`
3. Client 생성: `followme-client`
   - Client authentication: ON
   - Direct access grants: ON
4. Client secret 확인: `AFUcuO5HZsWcJP2Y7pfPhJYqKTL5L0gA`

---

## Step 3 — 서버 전체 기동

`start-all.sh`는 아래를 자동으로 처리합니다.

- Redis 실행 여부 확인 및 자동 기동
- DB 스키마 초기화 + 시드 데이터 적재 (hub mock-data, vendor data)
- 서비스 순차 기동 (Eureka → Config → Hub → User → Order → Delivery → Vendor → Gateway)
- 각 서비스 **포트 오픈 + Eureka 등록 완료** 대기
- 시드 데이터 재적재 및 검증

### E2E 테스트용 (기본, local 프로파일)

```bash
cd ~/Desktop/projects/delivery-server
chmod +x src/test/start-all.sh
./src/test/start-all.sh
```

### Feign 테스트용 (delivery-server를 feign-test 프로파일로 기동)

```bash
cd ~/Desktop/projects/delivery-server
DELIVERY_PROFILE=feign-test ./src/test/start-all.sh
```

기동 완료 후 `http://localhost:8761`에서 아래 서비스가 모두 `UP`인지 확인합니다.

- `HUB-SERVER`, `USER-SERVER`, `ORDER-SERVER`, `DELIVERY-SERVER`, `VENDOR-SERVER`, `GATEWAY-SERVER`

---

## Step 4 — 테스트 사용자 생성 (Feign 테스트 필수, E2E 권장)

`test-feign.sh`의 담당자 배정 검증에 실제 user-server 데이터가 필요합니다.

```bash
cd ~/Desktop/projects/delivery-server
chmod +x src/test/setup-users.sh
HUB_DELIVERY_COUNT=10 VENDORS_PER_HUB=10 VENDOR_DELIVERY_PER_HUB=10 ./src/test/setup-users.sh
```

---

## Step 5 — 테스트 실행

### E2E 테스트 (도메인 회귀)

```bash
cd ~/Desktop/projects/delivery-server
chmod +x src/test/test-e2e.sh
./src/test/test-e2e.sh
```

검증 항목:
- 주문 생성 → 배송 자동 생성
- 배송자 권한 검증 (타인 상태 변경 거부 / 담당자 허용)
- 배송 구간 상태 전이 (`SHIPPED` → `IN_TRANSIT` → `ARRIVED` → `COMPLETED`)
- 취소/삭제 및 오류 케이스

### Feign 통신 테스트

> **주의:** `DELIVERY_PROFILE=feign-test ./src/test/start-all.sh`로 기동한 경우에만 정확합니다.
> local 프로파일로 이미 기동된 상태라면 아래와 같이 delivery-server만 재기동합니다.
>
> ```bash
> # delivery-server만 종료 후 feign-test 프로파일로 재기동
> pkill -f "delivery.*bootRun" 2>/dev/null || true
> sleep 3
> cd ~/Desktop/projects/delivery-server
> SPRING_PROFILES_ACTIVE=feign-test ./gradlew bootRun
> ```

```bash
cd ~/Desktop/projects/delivery-server
chmod +x src/test/test-feign.sh
./src/test/test-feign.sh
```

검증 항목:
- Eureka 등록 확인 (hub / user / order / delivery)
- `HubClient` 실통신 + fallback (없는 허브 ID)
- `UserClient.getDeliveryManagers` / `updateDeliverySequence` / `getUserInfo` 실통신 + fallback
- `OrderClient.deliveryManagerAssigned` 실통신

---

## 권장 실행 순서 요약

### E2E 테스트

```
Step 1 (Docker)
→ Step 2 (Keycloak, 최초 1회)
→ Step 3: ./src/test/start-all.sh
→ Step 4: setup-users.sh (권장)
→ Step 5: test-e2e.sh
```

### Feign 테스트

```
Step 1 (Docker)
→ Step 2 (Keycloak, 최초 1회)
→ Step 3: DELIVERY_PROFILE=feign-test ./src/test/start-all.sh
→ Step 4: setup-users.sh (필수)
→ Step 5: test-feign.sh
```

---

## 트러블슈팅

| 증상 | 원인 | 조치 |
|---|---|---|
| `Redis failed to start` | redis-server 미설치 | `brew install redis` 후 재실행 |
| `[FAIL] hub-server port check failed` | hub-server 부팅 실패 | hub-server 단독 `./gradlew bootRun` 후 에러 로그 확인 |
| `[FAIL] hub-server Eureka registration timeout` | Eureka 등록 지연 또는 실패 | `http://localhost:8761` 대시보드에서 등록 여부 확인 |
| `Load balancer does not contain an instance for delivery-server` | delivery-server Eureka 미등록 상태에서 요청 | start-all.sh 완료 후 Eureka 대시보드에서 UP 확인 |
| `deliveryManagerName` 비어 있음 | user-server 담당자 데이터 없음 | `setup-users.sh` 실행 여부 확인 |
| 주문 생성 500 | 허브 재고/벤더 데이터 없음 | 아래 쿼리로 시드 건수 확인 |
| 배송 생성 500 | Hub 라우트 조회 실패 | `GET http://localhost:10001/api/hubs/route?sourceHubId=...&vendorId=...` 직접 호출 확인 |
| E2E 성공, Feign만 실패 | delivery-server가 local(스텁) 프로파일로 기동됨 | `DELIVERY_PROFILE=feign-test`로 재기동 |

### 시드 데이터 건수 확인

```bash
PGPASSWORD=followme psql -h 127.0.0.1 -p 5432 -U followme -d followme -c \
  "select
     (select count(*) from hub.p_hub)         hub_count,
     (select count(*) from hub.p_hub_route)   route_count,
     (select count(*) from hub.p_hub_stock)   stock_count,
     (select count(*) from vendor.p_vendor)   vendor_count,
     (select count(*) from vendor.p_product)  product_count;"
```

모든 항목이 0보다 커야 정상입니다. 0이 있으면 start-all.sh를 재실행하세요.

---

## 포트 요약

| 서비스 | 포트 |
|---|---|
| PostgreSQL | 5432 |
| Keycloak | 8090 |
| Redis | 6379 |
| Eureka | 8761 |
| Config Server | 13100 |
| Gateway | 8000 |
| User Server | 8080 |
| Order Server | 8082 |
| Hub Server | 10001 |
| Vendor Server | 10003 |
| Delivery Server | 10005 (feign-test: 8081) |

---

## 배송 데이터 직접 확인

```bash
# 목록
curl -s "http://localhost:10005/api/v1/deliveries?size=10" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -H "X-Role: MASTER" | jq .

# 단건
curl -s "http://localhost:10005/api/v1/deliveries/{deliveryId}" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -H "X-Role: MASTER" | jq .

# 구간(노드)
curl -s "http://localhost:10005/api/v1/deliveries/{deliveryId}/shipments" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -H "X-Role: MASTER" | jq .
```