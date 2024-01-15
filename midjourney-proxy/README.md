# midjourney-proxy

MidJourney의 디스코드 채널을 대리하여 API 형식으로 AI 그리기 호출을 구현

[![GitHub release](https://img.shields.io/static/v1?label=release&message=v2.5.5&color=blue)](https://www.github.com/novicezk/midjourney-proxy)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

## 주요 기능
- [x] Imagine 명령과 관련 동작 지원
- [x] Imagine 시 base64 이미지 추가 지원, 배경 이미지로 사용
- [x] Blend(이미지 혼합), Describe(이미지 기반 텍스트 생성) 명령 지원
- [x] 작업의 실시간 진행 상태 지원
- [x] 중문 prompt 번역 지원, 바이두 번역이나 GPT 설정 필요
- [x] prompt 민감 단어 사전 검사, 조정 가능
- [x] 사용자 토큰을 통한 wss 연결, 오류 정보 및 완전한 기능 접근 가능
- [x] 다중 계정 설정 지원, 각 계정별 작업 큐 설정 가능

**🚀 더 많은 기능은 [midjourney-proxy-plus](https://github.com/litter-coder/midjourney-proxy-plus)에서 확인하세요**
> - [x] 오픈 소스 버전의 모든 기능 지원
> - [x] Shorten(prompt 분석) 명령 지원
> - [x] 포커스 이동: Pan ⬅️ ➡️ ⬆️ ⬇️
> - [x] 이미지 줌인: Zoom 🔍
> - [x] 부분 재그리기: Vary (Region) 🖌
> - [x] 거의 모든 연관 버튼 동작 및 🎛️ Remix 모드 지원
> - [x] 이미지 seed 값 획득 지원
> - [x] 계정 풀 지속 관리
> - [x] 계정 정보 및 설정 획득
> - [x] 계정 설정 지원
> - [x] niji bot, InsightFace 인공지능 얼굴 교체 로봇 지원
> - [x] 내장된 관리자 페이지

## 사용 전제 조건
1. MidJourney에 등록 및 구독, 자체 서버와 채널 생성: [시작 가이드](https://docs.midjourney.com/docs/quick-start)
2. 사용자 토큰, 서버 ID, 채널 ID 획득: [획득 방법](./docs/discord-params.md)

## 빠른 시작
1. `Railway`: Railway 플랫폼 기반, 자체 서버 필요 없음: [배포 방법](./docs/railway-start.md); Railway 사용 불가 시 Zeabur 사용 가능
2. `Zeabur`: Zeabur 플랫폼 기반, 자체 서버 필요 없음: [배포 방법](./docs/zeabur-start.md)
3. `Docker`: 서버나 로컬에서 Docker 사용하여 시작: [배포 방법](./docs/docker-start.md)

## 로컬 개발
- Java 17과 Maven 필요
- 설정 변경: src/main/application.yml 수정
- 프로젝트 실행: ProxyApplication의 main 함수 시작
- 코드 변경 후, 이미지 빌드: Dockerfile에서 VOLUME 주석 해제, `docker build . -t midjourney-proxy` 실행

## 설정 옵션
- mj.accounts: [계정 풀 설정](./docs/config.md#%E8%B4%A6%E5%8F%B7%E6%B1%A0%E9%85%8D%E7%BD%AE%E5%8F%82%E8%80%83) 참조
- mj.task-store.type: 작업 저장 방식, 기본 in_memory(메모리\재시작 후 손실), redis 선택 가능
- mj.task-store.timeout: 작업 저장 만료 시간, 만료 후 삭제, 기본 30일
- mj.api-secret: API 비밀키, 비어 있으면 인증 비활성화; API 호출 시 요청 헤더에 mj-api-secret 필요
- mj.translate-way: 중문 prompt를 영문으로 번역하는 방식, 선택적 null(기본), baidu, gpt
- 추가 설정: [설정 항목](./docs/config.md) 확인

## 관련 문서
1. [API 인터페이스 설명](./docs/api.md)
2. [버전 업데이트 기록](https://github.com/novicezk/midjourney-proxy/wiki/%E6%9B%B4%E6%96%B0%E8%AE%B0%E5%BD%95)

## 주의 사항
1. 자주 작업하는 행위는 MidJourney 계정 경고를 유발할 수 있으니 주의하세요.
2. 자주 묻는 질문과 해결 방법은 [Wiki / FAQ](https://github.com/novicezk/midjourney-proxy/wiki/FAQ) 참조
3. 관심 있는 친구들은 교류 그룹에 가입하여 토론에 참여할 수 있습니다. QR 코드 스캔으로 그룹 참여 가능, 자리가 차면 관리자 위챗으로 초대, 비고: mj그룹 가입

 <img src="https://raw.githubusercontent.com/novicezk/midjourney-proxy/main/docs/manager-qrcode.png" width="220" alt="위챗 QR 코드"/>

## 응용 프로젝트
이 프로젝트에 의존하고 오픈 소스인 경우, 저자에게 연락하여 여기에 표시를 요청할 수 있습니다.
- [wechat-midjourney](https://github.com/novicezk/wechat-midjourney) : WeChat 클라이언트 대리, MidJourney 접근, 예시 응용 시나리오, 업데이트 중단
- [chatgpt-web-midjourney-proxy](https://github.com/Dooy/chatgpt-web-midjourney-proxy) : chatgpt web, midjourney, gpts, tts, whisper 모두 하나의 UI로 완성
- [stable-diffusion-mobileui](https://github.com/yuanyuekeji/stable-diffusion-mobileui) : SDUI, 이 인터페이스와 SD를 기반으로 H5 및 소프트웨어 패키지를 한 번에 생성
- [MidJourney-Web](https://github.com/ConnectAI-E/MidJourney-Web) : 🍎 웹 UI에서 MidJourney를 위한 Supercharged 경험

## 오픈 API
비공식 MJ/SD 오픈 API 제공, 관리자 위챗으로 문의, 비고: api

## 기타
이 프로젝트가 도움이 되었다면, 별표를 클릭하여 지원해 주세요.

[![Star History Chart](https://api.star-history.com/svg?repos=novicezk/midjourney-proxy&type=Date)](https://star-history.com/#novicezk/midjourney-proxy&Date)
