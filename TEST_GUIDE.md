# 전체 서비스 로컬 테스트 가이드

---

## 전제 조건

| 항목 | 버전 |
|------|------|
| Java | 17 이상 |
| Docker Desktop | 실행 중 |
| `jq` | `which jq` 로 확인, 없으면 `brew install jq` |

---

## 1단계: Docker 기동 (PostgreSQL + Keycloak)

```bash
cd ~/Desktop/projects
docker compose up -d
docker compose ps
```

| 컨테이너 | 포트 | 역할 |
|----------|------|------|
| `followme-postgres` | 5432 | 전 서비스 공용 DB |
| `followme-keycloak` | 8090 | 인증 서버 |

---

## 2단계: Keycloak 초기 설정

> **최초 1회만 진행합니다.**

1. http://localhost:8090 → ID: `admin` / PW: `admin`
2. **Create Realm** → name: `followme`
3. **Clients → Create client** → ID: `followme-client` → Client authentication ON → Save
4. **Credentials** 탭 → Client secret 확인 (`AFUcuO5HZsWcJP2Y7pfPhJYqKTL5L0gA`)
5. **Settings** 탭 → **Direct access grants** ON → Save

---

## 3단계: 전체 서버 기동

```bash
cd ~/Desktop/projects
./start-all.sh
```

| 순서 | 서비스 | 포트 | 프로파일 |
|------|--------|------|---------|
| 1 | Eureka Server | 8761 | — |
| 2 | Config Server | 13100 | — |
| 3 | Hub Server | 10001 | `local` |
| 4 | User Server | 8080 | — |
| 5 | Order Server | 8082 | `local` |
| 6 | Delivery Server | 10005 | `local` 또는 `local-intellij` |
| 7 | Vendor Server | 10003 | `local` |
| 8 | Gateway | 8000 | — |

Eureka 대시보드: http://localhost:8761 — 모든 서비스 `UP` 확인

---

## 4단계: E2E 자동화 테스트

```bash
cd ~/Desktop/projects
./test-e2e.sh
```

> `delivery-server`가 `local` 또는 `local-intellij` 프로파일로 기동되어야 합니다.  
> `HubClientLocalStub` / `UserClientLocalStub`이 활성화되지 않으면 배송 생성이 실패합니다.

---

## Feign 서비스 간 통신 테스트

`test-e2e.sh`는 스텁 기반이라 Hub/User/Order 서버 없이도 동작합니다.  
이 테스트는 **스텁을 끄고** 실제 Feign 호출이 성공하는지 확인합니다.

### 검증 대상

| Feign 클라이언트 | 엔드포인트 | 호출 시점 |
|----------------|-----------|---------|
| `HubClient` | `GET hub-server:/internal/hubs/route` | 배송 생성 |
| `UserClient.getDeliveryManagers` | `GET user-server:/internal/users/delivery-managers` | 담당자 배정 |
| `UserClient.updateDeliverySequence` | `GET user-server:/internal/users/{id}/sequence/last` | 배정 직후 |
| `OrderClient.deliveryManagerAssigned` | `PATCH order-server:/internal/v1/orders/{id}/delivery` | 배송 생성 완료 후 |
| `UserClient.getUserInfo` | `GET user-server:/internal/users/{id}` | 구간 담당자 재배정 |

### 프로파일 관련 추가 설명

`local` 프로파일이 활성화되면 스텁 클래스들이 실제 Feign 클라이언트를 덮어씁니다.

```
HubClientLocalStub    → hub-server 호출 없이 더미 노드 반환
UserClientLocalStub   → user-server 호출 없이 랜덤 담당자 반환
OrderClientLocalStub  → order-server 호출을 로그만 남기고 무시
```
`local-intellij` 프로파일에는 `spring.config.import: ""`이 있어 Config Server 덮어쓰기를 막습니다.  
프로파일을 완전히 비우면 Config Server가 Kafka `bootstrap-servers`를 `localhost:9092`로 덮어써서 오류가 발생합니다.

```
ERROR KafkaAdmin : Could not configure topics
Connection to node -1 (localhost/127.0.0.1:9092) could not be established.
```

→ **`feign-test` 전용 프로파일**을 사용합니다. Config Server 덮어쓰기를 막으면서 Eureka는 활성화하고, `local` 이름이 아니므로 스텁도 자동 비활성화됩니다.

### delivery-server를 `feign-test` 프로파일로 기동

**IntelliJ** — Run Configurations → Active profiles: `local-intellij` → `feign-test` 로 변경 후 재시작

**터미널**
```bash
cd ~/Desktop/projects/delivery-server
./gradlew bootRun --args='--spring.profiles.active=feign-test'
```

> 테스트 후 `local-intellij`로 되돌려야 `test-e2e.sh`가 정상 동작합니다.

### 실행

```bash
cd ~/Desktop/projects
./test-feign.sh
```

### 문제 해결

| 증상 | 원인 | 해결 |
|------|------|------|
| `[FAIL] hub-server Eureka 미등록` | hub-server 미기동 | hub-server 확인, 30초 대기 후 재시도 |
| 배송 생성 500 에러 | HubClient fallback 발동 | hub-server 로그 → `/internal/hubs/route` 확인 |
| `deliveryManagerName` null | UserClient fallback 발동 | user-server 로그 → `/internal/users/delivery-managers` 확인 |
| E2E 테스트 실패 (Feign 테스트 후) | `feign-test` 프로파일 유지 중 | delivery-server를 `local-intellij`로 재기동 |

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
