# Delivery 도메인 로컬 테스트 가이드

이 문서는 **Delivery 도메인을 검증하는 QA/개발자** 관점에서,
`delivery-server/src/test` 스크립트를 기준으로 테스트를 수행하는 절차입니다.

## 테스트 목적 정리 (도메인 관점)

| 스크립트 | 목적 | 왜 돌리나 | 필수 여부 |
|---|---|---|---|
| `src/test/test-e2e.sh` | 주문→배송 생성→구간 상태 전이→취소/삭제 회귀 | Delivery 사용자 시나리오가 끝까지 동작하는지 검증 | 필수 |
| `src/test/test-feign.sh` | Delivery ↔ Hub/User/Order Feign 통신 + fallback | 내부 연동 계약/라우팅/예외 처리 검증 | 필수 |

---

## 전제 조건

| 항목 | 기준 |
|---|---|
| Java | 17 이상 |
| Docker Desktop | 실행 중 |
| `jq` | 설치 필요 (`which jq`) |
| `psql` | 설치 필요 (setup 스크립트에서 사용) |

---

## 1) Docker 기동 (PostgreSQL + Keycloak)

```bash
cd ~/Desktop/projects
docker compose -f docker-compose.yml up -d
docker compose -f docker-compose.yml ps
```

---

## 2) Keycloak 초기 설정 (최초 1회)

1. `http://localhost:8090` 접속 (`admin` / `admin`)
2. Realm 생성: `followme`
3. Client 생성: `followme-client` (Client authentication ON)
4. Client secret 확인: `AFUcuO5HZsWcJP2Y7pfPhJYqKTL5L0gA` (테스트 스크립트에서만 로컬용)
5. Direct access grants ON

---

## 3) 서버 기동

`src/test/start-all.sh` 기준으로 서비스가 순차 기동됩니다.

```bash
cd ~/Desktop/projects/delivery-server
chmod +x src/test/start-all.sh
./src/test/start-all.sh
```

프로파일 변경이 필요하면 환경변수로 실행합니다.

```bash
# 예시: delivery-server만 feign-test로 기동
DELIVERY_PROFILE=feign-test ./src/test/start-all.sh
```

기본값은 `local`이며, 현재 스크립트는 아래 기본 프로파일을 사용합니다.

- `hub-server`: `local`
- `order-server`: `local`
- `delivery-server`: `local` (필요 시 `feign-test`로 변경)
- `vendor-server`: `local`

기동 후 `http://localhost:8761`에서 주요 서비스(`hub-server`, `user-server`, `order-server`, `delivery-server`, `gateway-server`)가 `UP`인지 확인하세요.

`start-all.sh`가 아래와 같이 멈추면:

```text
Waiting for hub-server port: 127.0.0.1:10001
[FAIL] hub-server port check failed (127.0.0.1:10001)
```

다음 순서로 즉시 복구하세요.

```bash
# 1) hub-server 단독 기동으로 실제 에러 로그 확인
cd ~/Desktop/projects/hub-server
./gradlew bootRun --args='--spring.profiles.active=local'
```

```bash
# 2) 별도 터미널에서 hub 포트 확인(200/503 상관없이 응답 오면 기동은 된 상태)
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:10001/actuator/health
```

```bash
# 3) 응답코드가 000이면 hub 미기동 상태이므로 로그 원인 먼저 해결 후 재시도
#    응답코드가 200 또는 503이면 start-all 재실행 가능
cd ~/Desktop/projects/delivery-server
./src/test/start-all.sh
```

---

## 4) 테스트 데이터 시드 적재 (주문 500 예방, 필수)

최근 로컬 실패의 대부분은 **허브 재고/벤더 데이터 미적재**로 발생했습니다.  
아래 2개 SQL을 먼저 넣어야 `test-e2e.sh`의 주문 생성이 정상 동작합니다.

```bash
PGPASSWORD=followme psql -h 127.0.0.1 -p 5432 -U followme -d followme -f ~/Desktop/projects/hub-server/src/main/resources/mock-data.sql
PGPASSWORD=followme psql -h 127.0.0.1 -p 5432 -U followme -d followme -f ~/Desktop/projects/vendor-server/data.sql
```

검증(둘 다 0보다 커야 정상):

```bash
PGPASSWORD=followme psql -h 127.0.0.1 -p 5432 -U followme -d followme -c "select (select count(*) from hub.p_hub) hub_count, (select count(*) from hub.p_hub_route) route_count, (select count(*) from vendor.p_vendor) vendor_count, (select count(*) from vendor.p_product) product_count, (select count(*) from hub.p_hub_stock) stock_count;"
```

---

## 5) Feign 테스트용 사용자/역할 데이터 생성 (권장)

`test-feign.sh`는 실서버 Feign 호출을 전제로 하므로, 시작 시 사용자 풀을 먼저 준비하는 것을 권장합니다.

- 허브 배송 담당자: 기본 `10`명
- 허브당 업체 사용자: 기본 `10`명
- 허브당 업체 배송 담당자: 기본 `10`명

```bash
cd ~/Desktop/projects/delivery-server
chmod +x src/test/setup-users.sh
HUB_DELIVERY_COUNT=10 VENDORS_PER_HUB=10 VENDOR_DELIVERY_PER_HUB=10 ./src/test/setup-users.sh
```

필요 시 규모 조절 예시:

```bash
HUB_DELIVERY_COUNT=20 VENDORS_PER_HUB=5 VENDOR_DELIVERY_PER_HUB=5 ./src/test/setup-users.sh
```

---

## 6) `test-e2e.sh` (도메인 시나리오 회귀)

`test-e2e.sh`의 핵심 목적은 **배송 도메인 워크플로 회귀 검증**입니다.

- 로그인
- 주문 생성
- 배송 자동 생성
- 배송자 권한 검증
  - 배송자 외 사용자 상태 변경 거부
  - 담당 배송자 상태 변경 허용
- 배송 구간 상태 전이(`SHIPPED/IN_TRANSIT/ARRIVED/COMPLETED`)
- 취소/삭제 및 오류 케이스

즉, “서비스 간 상세 연동”보다 “사용자 시나리오가 끝까지 동작하는지”를 보는 테스트입니다.

실행:

```bash
cd ~/Desktop/projects/delivery-server
chmod +x src/test/test-e2e.sh
./src/test/test-e2e.sh
```

---

## 7) `test-feign.sh` (연동 검증, 반환값 기준 강화)

`test-feign.sh`는 **Eureka 기반 실통신**을 검증합니다.

- Eureka 등록 확인
- `HubClient`, `UserClient`, `OrderClient` 실제 호출 확인
- fallback 동작 확인(허브/유저 미존재 케이스)
- 생성 검증 로직
  - **1차:** 응답 반환값(`orderId`, `deliveryId`) 기준으로 생성 확인
  - **2차:** 반환값이 없으면 이전/현재 개수 비교로 보조 확인
  - **3차:** 그래도 불명확하면 목록 조회로 재확인

실행 전 주의:

- `test-feign.sh`는 `delivery-server`가 **`feign-test` 프로파일**로 떠 있어야 정확합니다.
- 기존 `local`로 떠 있는 `delivery-server`가 있으면 종료 후 재실행하세요.
- 포트 충돌/프로세스 혼선을 피하려면 **새 터미널 2개** 사용을 권장합니다.

권장 실행 순서:

```bash
# [터미널 1] 기존 delivery-server 종료 후 feign-test로 재기동
cd ~/Desktop/projects/delivery-server
pkill -f "DeliveryServerApplication" 2>/dev/null || true
sleep 2
./gradlew bootRun --args='--spring.profiles.active=feign-test'
```

```bash
# [터미널 2] feign 통합 테스트 실행
cd ~/Desktop/projects/delivery-server
chmod +x src/test/test-feign.sh
./src/test/test-feign.sh
```

---

## 권장 실행 순서 (실무형)

1. `start-all.sh` (서비스/인프라 올리기)
2. `hub-server mock-data.sql` + `vendor-server data.sql` 시드 적재 (필수)
3. `setup-users.sh` (Feign용 사용자 대량 생성, 권장)
4. `test-e2e.sh` (도메인 회귀)
5. `test-feign.sh` (Eureka 실통신/fallback)

---

## 생성된 배송 GET 확인 방법

도메인 테스트 중 생성된 배송이 실제로 저장됐는지 API로 바로 확인할 수 있습니다.

```bash
# 목록(최신 배송 확인)
curl -s "http://localhost:10005/api/v1/deliveries?size=10" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -H "X-Role: MASTER"

# 단건
curl -s "http://localhost:10005/api/v1/deliveries/{deliveryId}" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -H "X-Role: MASTER"

# 구간(노드) 확인
curl -s "http://localhost:10005/api/v1/deliveries/{deliveryId}/shipments" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -H "X-Role: MASTER"
```

---

## 자주 발생하는 실패 포인트

| 증상 | 원인 | 확인 포인트 |
|---|---|---|
| `hub-server Eureka 미등록` | hub-server 미기동/등록 지연 | Eureka 대시보드에서 `UP` 확인 |
| `hub-server port check failed (10001)` | hub-server 부팅 실패 또는 초기화 지연 | `~/Desktop/projects/hub-server`에서 단독 `bootRun` 후 에러 로그 확인 |
| `deliveryManagerName` 비어 있음 | user-server 담당자 데이터 부족 | `setup-users.sh` 선실행 여부 확인 |
| 주문 생성 500 | 허브 재고/벤더 데이터 미적재 | `hub.p_hub_stock`, `vendor.p_vendor`, `vendor.p_product` 건수 확인 |
| 배송 생성 500 | Hub 라우트 조회 실패/경로 불일치 | hub-server `/api/hubs/route` 응답 확인 |
| user internal 403/404 | 내부 API 경로 버전 불일치 | user-server `/internal/v1/users/**` 경로 확인 |
| 배송 상태 변경이 모두 성공/모두 실패 | 권한 헤더/사용자 ID 설정 문제 | `X-User-Id`, `X-Role`, 담당자 ID 일치 여부 확인 |
| E2E는 성공, Feign만 실패 | 스텁 의존/실통신 환경 차이 | Eureka 등록 + 내부 API 계약 확인 |

---

## 포트 요약

| 서비스 | 포트 |
|---|---|
| PostgreSQL | 5432 |
| Keycloak | 8090 |
| Eureka | 8761 |
| Config Server | 13100 |
| Gateway | 8000 |
| User Server | 8080 |
| Order Server | 8082 |
| Hub Server | 10001 |
| Delivery Server | 10005 |
| Vendor Server | 10003 |
