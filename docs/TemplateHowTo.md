# 🗂️ Spring Boot 프로젝트 템플릿

Spring Boot 기반 백엔드 프로젝트를 빠르게 시작할 수 있는 팀 공용 템플릿입니다.
이슈/PR 템플릿, 코드 포맷팅 자동화, REST Docs 배포가 사전 구성되어 있습니다.

---

## 🚀 템플릿 사용법

### 1. 레포지토리 생성

1. 이 레포지토리 상단의 **Use this template** 버튼 클릭
2. 새 레포지토리 이름 및 공개 여부 설정 후 생성

### 2. 브랜치 구성

기본 워크플로우는 `main`, `dev` 브랜치를 기준으로 동작합니다.

```bash
git checkout -b dev
git push origin dev
```

### 3. GitHub Pages 설정 (REST Docs 배포용)

1. 레포지토리 **Settings → Pages** 로 이동
2. Source를 **GitHub Actions** 로 설정

`dev` 또는 `main` 브랜치에 푸시하면 REST Docs가 자동으로 GitHub Pages에 배포됩니다.

---

## 📦 포함된 구성 요소

| 구성 요소                      | 설명                                                         |
|------------------------------|--------------------------------------------------------------|
| Issue 템플릿                  | 작업 설명, 목적, 세부 항목, 관련 이슈를 포함한 이슈 양식       |
| PR 템플릿                     | 관련 이슈, 작업 내용, 변경 이유, 리뷰 포인트를 포함한 PR 양식  |
| Spotless (Google Java Format) | PR 생성 시 코드 포맷 자동 검사 (main, dev 브랜치 대상)        |

---

## 🧹 코드 포맷팅 (Spotless)

이 프로젝트는 일관된 Java 코드 스타일을 유지하기 위해 **Spotless(Google Java Format)** 를 사용합니다.
`main`, `dev` 브랜치로 PR을 올릴 때 GitHub Actions CI를 통해 자동으로 코드 포맷을 검사하며, 포맷이 어긋난 코드의 머지를 차단합니다.

```bash
# 포맷 자동 정렬 적용 (커밋 전 필수)
./gradlew spotlessApply

# 포맷 준수 여부 검사 (CI 검증 명령어)
./gradlew spotlessCheck
```

---

## 📄 REST Docs 자동 배포

`dev` 또는 `main` 브랜치에 푸시하면 GitHub Actions가 자동으로 다음을 수행합니다.

1. JDK 21 환경에서 `./gradlew asciidoctor` 빌드
2. 생성된 문서(`build/docs/asciidoc`)를 GitHub Pages에 배포

> **참고:** 빌드 시 Redis(7) 서비스가 필요합니다. 테스트에서 Redis를 사용하는 경우 별도 설정 없이 CI에서 자동으로 실행됩니다.