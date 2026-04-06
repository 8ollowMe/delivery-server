# 전체 서비스 로컬 테스트 가이드

Keycloak 기동 → 회원가입 → 로그인 → 주문 생성 → 배송 생성 → 상태 확인까지의 전체 흐름을 설명합니다.

---

## 전제 조건

| 항목 | 버전 |
|------|------|
| Java | 17 이상 |
| Docker Desktop | 실행 중 |
| `jq` | `which jq` 로 확인, 없으면 `brew install jq` |
| PostgreSQL 클라이언트 (`psql`) | 선택 (DB 초기화 재실행 시) |

---

## 1단계: Docker 기동 (PostgreSQL + Keycloak)

```bash
cd /Users/annie/Desktop/projects
docker compose up -d
```

컨테이너 상태 확인:
```bash
docker compose ps
```

| 컨테이너 | 포트 | 역할 |
|----------|------|------|
| `followme-postgres` | 5432 | 전 서비스 공용 DB |
| `followme-keycloak` | 8090 | 인증 서버 |

> PostgreSQL이 healthy 상태가 될 때까지 기다린 후 다음 단계로 진행하세요.

---

## 2단계: Keycloak 초기 설정

> **최초 1회만 진행합니다.** 이미 설정된 경우 3단계로 넘어가세요.

### 2-1. 관리자 콘솔 접속

브라우저에서 http://localhost:8090 열기  
- ID: `admin`  
- PW: `admin`

### 2-2. Realm 생성

1. 좌측 상단 드롭다운 → **Create Realm**
2. Realm name: `followme` → **Create**

### 2-3. Client 생성

1. 좌측 메뉴 **Clients** → **Create client**
2. Client ID: `followme-client` → **Next**
3. **Client authentication** ON → **Next** → **Save**
4. **Credentials** 탭 → Client secret 복사 (아래 API 호출에서 사용)

> 현재 auth-api.http에 저장된 secret: `AFUcuO5HZsWcJP2Y7pfPhJYqKTL5L0gA`  
> 새로 생성했다면 이 값을 교체해야 합니다.

### 2-4. Direct Access Grants 활성화

**Clients → followme-client → Settings** 탭  
→ **Direct access grants** ON → **Save**

---

## 3단계: 전체 서버 기동

```bash
cd /Users/annie/Desktop/projects
chmod +x start-all.sh
./start-all.sh
```

기동 순서 및 포트:

| 순서 | 서비스 | 포트 | 비고 |
|------|--------|------|------|
| 1 | Eureka Server | 8761 | 서비스 디스커버리 |
| 2 | Config Server | 13100 | 중앙 설정 서버 |
| 3 | Hub Server | 10001 | local 프로파일 |
| 4 | User Server | 8080 | |
| 5 | Order Server | 8082 | local 프로파일 |
| 6 | Delivery Server | 10005 | local 프로파일 |
| 7 | Vendor Server | 10003 | local 프로파일 |
| 8 | Gateway | 8000 | |

### 서비스 기동 확인

Eureka 대시보드: http://localhost:8761  
모든 서비스가 `UP` 상태인지 확인합니다.

---

## 4단계: E2E 자동화 테스트

서버가 모두 뜬 후 아래 스크립트로 전체 흐름을 한 번에 검증합니다.

```bash
cd /Users/annie/Desktop/projects
./test-e2e.sh
```

### 출력 예시

```
▶ 0. 서비스 헬스 체크
[PASS] Gateway      (8000) 응답 확인 (HTTP 404)
[PASS] User Server  (8080) 응답 확인 (HTTP 401)
[PASS] Order Server (8082) 응답 확인 (HTTP 200)
[PASS] Delivery     (10005) 응답 확인 (HTTP 200)
[PASS] Keycloak     (8090) 응답 확인 (HTTP 200)

▶ 1. 회원가입 (username: u12345)
[PASS] 회원가입

▶ 2. 로그인
[PASS] 액세스 토큰 발급

▶ 3. 주문 생성
[PASS] 주문 생성
[PASS] orderId 확인

...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  결과: PASS 18 / FAIL 0
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

실패 시 `exit 1`을 반환하므로 CI(GitHub Actions 등)에도 그대로 연결할 수 있습니다.

### 주의 사항

- **주문/배송/구간 API는 직접 포트로 호출합니다** (Gateway 경유 시 Keycloak realm에 역할 설정이 없으면 `X-User-Role` 주입 실패)
- 스크립트는 매 실행마다 고유한 username을 생성하므로 중복 오류가 발생하지 않습니다
- `local` 프로파일의 `HubClientLocalStub` / `UserClientLocalStub`이 활성화된 상태에서만 정상 동작합니다

---

## 수동 테스트 (단계별)

스크립트 없이 직접 API를 호출하며 테스트하려면 아래 단계를 따라가세요.

---

## 5단계: 회원가입

```http
POST http://localhost:8000/api/v1/users/register
Content-Type: application/json

{
  "username": "testuser1",
  "password": "Test1234!",
  "name": "홍길동",
  "slackId": "slack_testuser1",
  "role": "MASTER"
}
```

> **비밀번호 규칙**: 대문자+소문자+숫자+특수문자(`@$!%*?&`) 포함, 8~15자  
> **필수 필드**: `username`(소문자+숫자 4~10자), `password`, `name`, `slackId`, `role`

---

## 6단계: 로그인 (토큰 발급)

```http
POST http://localhost:8090/realms/followme/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&client_id=followme-client
&client_secret=AFUcuO5HZsWcJP2Y7pfPhJYqKTL5L0gA
&username=testuser1
&password=Test1234!
```

응답에서 `access_token` 값을 복사해 두세요.

```json
{
  "access_token": "eyJhbGci...",
  "expires_in": 300,
  "token_type": "Bearer"
}
```

---

## 7단계: 주문 생성

> Gateway `X-User-Role` 주입 이슈로 **Order Server 직접 포트(8082)**로 호출합니다.

```http
POST http://localhost:8082/api/v1/orders
Content-Type: application/json
X-User-Id: 00000000-0000-0000-0000-000000000001
X-User-Role: MASTER

{
  "productId": "aaaaaaaa-0000-0000-0000-000000000001",
  "productName": "축구공",
  "requestVendorId": "bbbbbbbb-0000-0000-0000-000000000001",
  "requestVendorName": "요청업체A",
  "requestVendorHubId": "dddddddd-0000-0000-0000-000000000001",
  "receiverVendorId": "dddddddd-0000-0000-0000-000000000003",
  "receiverVendorName": "수령업체B",
  "receiverVendorHubId": "dddddddd-0000-0000-0000-000000000002",
  "quantity": 10,
  "requestNote": "빠른 배송 부탁드립니다"
}
```

응답에서 `orderId`를 복사해 두세요.

---

## 8단계: 배송 생성 (Order Server → Delivery Server 연동)

주문 생성 시 Order Server가 내부적으로 Delivery Server에 배송을 자동 생성합니다.  
수동으로 생성할 경우 아래 internal API를 사용합니다:

```http
POST http://localhost:10005/internal/v1/deliveries
Content-Type: application/json

{
  "orderId": "<위에서 받은 orderId>",
  "sourceHubId": "dddddddd-0000-0000-0000-000000000001",
  "vendorId": "dddddddd-0000-0000-0000-000000000003"
}
```

응답 예시:
```json
{
  "success": true,
  "data": {
    "deliveryId": "...",
    "deliveryManagerId": "...",
    "deliveryManagerName": "김배달"
  }
}
```

> `local` 프로파일에서는 `HubClientLocalStub` + `UserClientLocalStub`이 활성화되어  
> 실제 Hub/User 서버 없이도 배송 매니저가 자동 배정됩니다.

---

## 9단계: 배송 조회

### 배송 단건 조회
```http
GET http://localhost:10005/api/v1/deliveries/<deliveryId>
X-User-Id: 00000000-0000-0000-0000-000000000001
X-User-Role: MASTER
```

### 주문 ID로 배송 조회
```http
GET http://localhost:10005/api/v1/deliveries/order/<orderId>
X-User-Id: 00000000-0000-0000-0000-000000000001
X-User-Role: MASTER
```

### 배송 상태 조회 (internal)
```http
GET http://localhost:10005/internal/v1/deliveries/<deliveryId>/status
```

---

## 10단계: 배송 구간 상태 변경

### 구간 목록 조회
```http
GET http://localhost:10005/api/v1/deliveries/<deliveryId>/shipments
X-User-Id: 00000000-0000-0000-0000-000000000001
X-User-Role: MASTER
```

### 상태 전환 순서: PENDING → SHIPPED → IN_TRANSIT → ARRIVED → COMPLETED

```http
PATCH http://localhost:10005/api/v1/shipments/<shipmentId>/status
Content-Type: application/json
X-User-Id: 00000000-0000-0000-0000-000000000001
X-User-Role: MASTER

{"status": "SHIPPED"}
```

마지막 구간(HUB_TO_VENDOR)이 `COMPLETED`가 되면 Delivery 전체 상태도 `COMPLETED`로 전환됩니다.

---

## 11단계: 배송 담당자 재배정 (선택)

```http
PATCH http://localhost:10005/api/v1/shipments/<shipmentId>/reassign
Content-Type: application/json
X-User-Id: 00000000-0000-0000-0000-000000000001
X-User-Role: MASTER

{"deliveryManagerId": "<새 담당자 UUID>"}
```

> 구간 상태가 `PENDING` 또는 `FAILED`일 때만 재배정 가능합니다.

---

## 문제 해결

| 증상 | 원인 | 해결 |
|------|------|------|
| Keycloak 토큰 발급 실패 | Realm/Client 미설정 | 2단계 다시 확인 |
| 주문/배송 API `X-User-Role` 오류 | Gateway가 Keycloak realm 역할 미설정으로 `USER` 주입 | 직접 포트(8082, 10005)로 호출하고 헤더 수동 설정 |
| 회원가입 validation 오류 | 비밀번호 규칙 또는 필수 필드 누락 | 대문자+소문자+숫자+특수문자, `slackId`·`role` 포함 |
| 서비스 시작 안됨 | 이전 프로세스 충돌 | `pkill -f bootRun` 후 재시작 |
| 배송 매니저 미배정 | UserClientLocalStub 미활성화 | `local` 프로파일 확인 |
| DB 연결 실패 | Docker 미기동 | `docker compose up -d` |
| Config Server 연결 실패 | 부팅 순서 문제 | Config Server 뜬 후 다른 서비스 재시작 |
| Eureka 등록 안됨 | 서비스 기동 시간 부족 | 30초 대기 후 대시보드 확인 |

---

## 포트 요약

| 서비스 | 포트 |
|--------|------|
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
