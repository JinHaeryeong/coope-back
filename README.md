# Coope: WebRTC등을 이용한 실시간 통화가 가능한 협업 사이트

<p align="center">
  <img width="554" height="117"  alt="logo" src="https://github.com/user-attachments/assets/ee9196c1-a378-453f-a262-ed885714b888" />
</p>

> "React와 Mediasoup SFU 아키텍처를 결합한 화상 회의 및 실시간 툴"
---
## 마이그레이션 진행중
---
### 배포주소

<p align="center">
  <a href="https://www.coope1.me">Coope</a>
</p>

* 기술스택 변경 전 프로젝트: https://github.com/JinHaeryeong/Coope-project
* React 주소: https://github.com/JinHaeryeong/coope-front
* Spring 주소: https://github.com/JinHaeryeong/coope-back
* Node.js 주소: https://github.com/JinHaeryeong/coope-webrtc-sfu

## 프로젝트 개요

### 목표
* 프로젝트 기술 스택 변경: 초기 개발 단계에서는 개발 편의성을 위해 직접 구현할 부분이 적은 프레임워크를 사용했으나, 성능 최적화 등을 위해 직접 구현 비중이 높은 스택으로 전환을 결정
* SFU 기반 미디어 서버 구축: Mesh 방식의 한계를 넘어 Mediasoup을 활용한 다자간 통신 구현
* 실시간 데이터 동기화: 양방향 통신과 더불어, 동시 편집 및 상태 공유를 위한 로직을 설계
* 인프라 아키텍처 실습: Vercel(Front)과 AWS EC2(Back)를 분리 배포하여 실제 서비스와 유사한 인프라 환경 구축


## 사용 기술
<img width="1232" height="478" alt="coope" src="https://github.com/user-attachments/assets/a7bef5d6-f900-4b53-91da-610643aa36a9" />




## 환경
### Frontend
* Framework: React
* Deployment: Vercel
* Communication: Axios (REST API), Socket.io-client (Signaling/Chat)
* Real-time: WebRTC (Mediasoup-client), BlockNote, LiveBlocks

### Backend
* Core: Spring Boot 3.x (Java 21+)
* Media Server: Node.js (Mediasoup / SFU Architecture)
* Database: AWS RDS (MySQL 8.0)
* In-Memory DB: Redis (JWT Blacklist, Cache)
* Storage: AWS S3

---
```
📦 
├─ .gitattributes
├─ .github
│  └─ workflows
│     └─ deploy.yml
├─ .gitignore
├─ README.md
├─ build.gradle
├─ gradle
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradlew
├─ gradlew.bat
├─ notice-list-test.js
├─ settings.gradle
└─ src
   ├─ main
   │  ├─ java
   │  │  └─ com
   │  │     └─ coope
   │  │        └─ server
   │  │           ├─ ServerApplication.java
   │  │           ├─ ai
   │  │           │  ├─ application
   │  │           │  │  └─ AiService.java
   │  │           │  ├─ infrastructure
   │  │           │  │  └─ OpenAICompletionResponse.java
   │  │           │  └─ presentation
   │  │           │     ├─ AiController.java
   │  │           │     └─ dto
   │  │           │        └─ VoiceProcessResponse.java
   │  │           ├─ aichat
   │  │           │  ├─ application
   │  │           │  │  └─ AIChatService.java
   │  │           │  └─ presentation
   │  │           │     ├─ AIChatController.java
   │  │           │     └─ dto
   │  │           │        ├─ AIChatMessage.java
   │  │           │        ├─ AIChatRequest.java
   │  │           │        └─ AIChatStreamRequest.java
   │  │           ├─ auth
   │  │           │  ├─ application
   │  │           │  │  ├─ AccountRecoveryService.java
   │  │           │  │  ├─ AuthService.java
   │  │           │  │  ├─ CustomOAuth2UserService.java
   │  │           │  │  ├─ CustomUserDetailsService.java
   │  │           │  │  ├─ EmailAuthService.java
   │  │           │  │  ├─ LoginAttemptService.java
   │  │           │  │  ├─ MailService.java
   │  │           │  │  └─ TokenService.java
   │  │           │  ├─ domain
   │  │           │  │  ├─ GoogleUserInfo.java
   │  │           │  │  └─ OAuth2UserInfo.java
   │  │           │  ├─ infrastructure
   │  │           │  │  └─ OAuth2AuthenticationSuccessHandler.java
   │  │           │  └─ presentation
   │  │           │     ├─ AuthController.java
   │  │           │     └─ dto
   │  │           │        ├─ EmailRequest.java
   │  │           │        ├─ EmailVerifyRequest.java
   │  │           │        ├─ FindEmailRequest.java
   │  │           │        ├─ FindEmailResponse.java
   │  │           │        ├─ FindPasswordRequest.java
   │  │           │        ├─ LoginRequest.java
   │  │           │        ├─ LoginResponse.java
   │  │           │        └─ ResetPasswordRequest.java
   │  │           ├─ chat
   │  │           │  ├─ application
   │  │           │  │  └─ ChatService.java
   │  │           │  ├─ domain
   │  │           │  │  ├─ ChatParticipant.java
   │  │           │  │  ├─ ChatParticipantRepository.java
   │  │           │  │  ├─ ChatRoom.java
   │  │           │  │  ├─ ChatRoomRepository.java
   │  │           │  │  ├─ Message.java
   │  │           │  │  ├─ MessageRepository.java
   │  │           │  │  ├─ MessageType.java
   │  │           │  │  └─ RoomType.java
   │  │           │  ├─ infrastructure
   │  │           │  │  ├─ ChatParticipantJpaRepository.java
   │  │           │  │  ├─ ChatParticipantRepositoryImpl.java
   │  │           │  │  ├─ ChatRoomJpaRepository.java
   │  │           │  │  ├─ ChatRoomRepositoryImpl.java
   │  │           │  │  ├─ MessageJpaRepository.java
   │  │           │  │  └─ MessageRepositoryImpl.java
   │  │           │  └─ presentation
   │  │           │     ├─ ChatController.java
   │  │           │     ├─ ChatStompController.java
   │  │           │     └─ dto
   │  │           │        ├─ ChatListResponse.java
   │  │           │        ├─ ChatRoomResponse.java
   │  │           │        ├─ ChatUploadResponse.java
   │  │           │        ├─ CreateGroupRequest.java
   │  │           │        ├─ MessageRequest.java
   │  │           │        └─ MessageResponse.java
   │  │           ├─ comment
   │  │           │  ├─ application
   │  │           │  │  └─ CommentService.java
   │  │           │  ├─ domain
   │  │           │  │  ├─ Comment.java
   │  │           │  │  └─ CommentRepository.java
   │  │           │  ├─ infrastructure
   │  │           │  │  ├─ CommentJpaRepository.java
   │  │           │  │  └─ CommentRepositoryImpl.java
   │  │           │  └─ presentation
   │  │           │     ├─ CommentController.java
   │  │           │     └─ dto
   │  │           │        ├─ CommentRequest.java
   │  │           │        └─ CommentResponse.java
   │  │           ├─ document
   │  │           │  ├─ application
   │  │           │  │  └─ DocumentService.java
   │  │           │  ├─ domain
   │  │           │  │  ├─ Document.java
   │  │           │  │  └─ DocumentRepository.java
   │  │           │  ├─ infrastructure
   │  │           │  │  ├─ DocumentEvent.java
   │  │           │  │  ├─ DocumentJpaRepository.java
   │  │           │  │  ├─ DocumentRepositoryImpl.java
   │  │           │  │  └─ DocumentSyncScheduler.java
   │  │           │  ├─ liveblocks
   │  │           │  │  ├─ LiveblocksAuthController.java
   │  │           │  │  └─ LiveblocksAuthService.java
   │  │           │  └─ presentation
   │  │           │     ├─ DocumentController.java
   │  │           │     └─ dto
   │  │           │        ├─ DocumentCreateRequest.java
   │  │           │        ├─ DocumentResponse.java
   │  │           │        └─ DocumentUpdateRequest.java
   │  │           ├─ friend
   │  │           │  ├─ application
   │  │           │  │  └─ FriendService.java
   │  │           │  ├─ domain
   │  │           │  │  ├─ Friend.java
   │  │           │  │  ├─ FriendRepository.java
   │  │           │  │  └─ FriendStatus.java
   │  │           │  ├─ infrastructure
   │  │           │  │  ├─ FriendJpaRepository.java
   │  │           │  │  └─ FriendRepositoryImpl.java
   │  │           │  └─ presentation
   │  │           │     ├─ FriendController.java
   │  │           │     └─ dto
   │  │           │        └─ FriendResponse.java
   │  │           ├─ inquiry
   │  │           │  ├─ application
   │  │           │  │  └─ InquiryService.java
   │  │           │  ├─ domain
   │  │           │  │  ├─ Inquiry.java
   │  │           │  │  ├─ InquiryAnswer.java
   │  │           │  │  ├─ InquiryFile.java
   │  │           │  │  ├─ InquiryRepository.java
   │  │           │  │  └─ enums
   │  │           │  │     ├─ InquiryCategory.java
   │  │           │  │     └─ InquiryStatus.java
   │  │           │  ├─ infrastructure
   │  │           │  │  ├─ InquiryCleanupProcessor.java
   │  │           │  │  ├─ InquiryCleanupScheduler.java
   │  │           │  │  ├─ InquiryJpaRepository.java
   │  │           │  │  └─ InquiryRepositoryImpl.java
   │  │           │  └─ presentation
   │  │           │     ├─ InquiryController.java
   │  │           │     └─ dto
   │  │           │        ├─ InquiryAnswerRequest.java
   │  │           │        ├─ InquiryCreateRequest.java
   │  │           │        └─ InquiryResponse.java
   │  │           ├─ notice
   │  │           │  ├─ application
   │  │           │  │  └─ NoticeService.java
   │  │           │  ├─ domain
   │  │           │  │  ├─ Notice.java
   │  │           │  │  └─ NoticeRepository.java
   │  │           │  ├─ infrastructure
   │  │           │  │  ├─ NoticeJpaRepository.java
   │  │           │  │  ├─ NoticeRepositoryImpl.java
   │  │           │  │  └─ ViewCountScheduler.java
   │  │           │  └─ presentation
   │  │           │     ├─ NoticeController.java
   │  │           │     └─ dto
   │  │           │        ├─ NoticeDetailResponse.java
   │  │           │        ├─ NoticeResponse.java
   │  │           │        └─ NoticeWriteRequest.java
   │  │           ├─ shared
   │  │           │  ├─ ai
   │  │           │  │  ├─ AiLimit.java
   │  │           │  │  ├─ AiUsageAspect.java
   │  │           │  │  ├─ AiUsageController.java
   │  │           │  │  └─ AiUsageService.java
   │  │           │  ├─ config
   │  │           │  │  ├─ JwtProperties.java
   │  │           │  │  ├─ RedisConfig.java
   │  │           │  │  ├─ RestTemplateConfig.java
   │  │           │  │  ├─ SecurityConfig.java
   │  │           │  │  ├─ SwaggerConfig.java
   │  │           │  │  ├─ WebClientConfig.java
   │  │           │  │  ├─ WebConfig.java
   │  │           │  │  └─ WebSocketConfig.java
   │  │           │  ├─ domain
   │  │           │  │  └─ BaseTimeEntity.java
   │  │           │  ├─ error
   │  │           │  │  ├─ GlobalExceptionHandler.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  └─ ErrorResponse.java
   │  │           │  │  └─ exception
   │  │           │  │     ├─ AccessDeniedException.java
   │  │           │  │     ├─ AccountLockedException.java
   │  │           │  │     ├─ AiServiceException.java
   │  │           │  │     ├─ AuthenticationException.java
   │  │           │  │     ├─ BadRequestException.java
   │  │           │  │     ├─ CommentNotFoundException.java
   │  │           │  │     ├─ ConflictException.java
   │  │           │  │     ├─ DocumentNotFoundException.java
   │  │           │  │     ├─ FileStorageException.java
   │  │           │  │     ├─ FriendException.java
   │  │           │  │     ├─ InvalidTokenException.java
   │  │           │  │     ├─ NoticeNotFoundException.java
   │  │           │  │     ├─ UserNotFoundException.java
   │  │           │  │     └─ WorkspaceNotFoundException.java
   │  │           │  ├─ file
   │  │           │  │  ├─ FileController.java
   │  │           │  │  ├─ FileDeleteEvent.java
   │  │           │  │  ├─ FileEventListener.java
   │  │           │  │  ├─ FileService.java
   │  │           │  │  ├─ ImageCategory.java
   │  │           │  │  ├─ LocalFileService.java
   │  │           │  │  └─ S3FileService.java
   │  │           │  ├─ security
   │  │           │  │  ├─ CustomAuthenticationEntryPoint.java
   │  │           │  │  ├─ FilterChannelInterceptor.java
   │  │           │  │  ├─ JwtAuthenticationFilter.java
   │  │           │  │  ├─ JwtProvider.java
   │  │           │  │  └─ UserDetailsImpl.java
   │  │           │  └─ util
   │  │           │     └─ SecurityUtil.java
   │  │           ├─ user
   │  │           │  ├─ application
   │  │           │  │  └─ UserService.java
   │  │           │  ├─ domain
   │  │           │  │  ├─ User.java
   │  │           │  │  ├─ UserRepository.java
   │  │           │  │  └─ enums
   │  │           │  │     ├─ Provider.java
   │  │           │  │     └─ Role.java
   │  │           │  ├─ infrastructure
   │  │           │  │  ├─ UserJpaRepository.java
   │  │           │  │  └─ UserRepositoryImpl.java
   │  │           │  └─ presentation
   │  │           │     ├─ UserController.java
   │  │           │     └─ dto
   │  │           │        ├─ PasswordCheckRequest.java
   │  │           │        ├─ ProfileUpdateFullRequest.java
   │  │           │        ├─ SignupRequest.java
   │  │           │        ├─ SignupResponse.java
   │  │           │        ├─ UserResponse.java
   │  │           │        └─ UserSearchResponse.java
   │  │           └─ workspace
   │  │              ├─ application
   │  │              │  ├─ WorkspaceRoleService.java
   │  │              │  └─ WorkspaceService.java
   │  │              ├─ domain
   │  │              │  ├─ Workspace.java
   │  │              │  ├─ WorkspaceMember.java
   │  │              │  ├─ WorkspaceMemberRepository.java
   │  │              │  ├─ WorkspaceRepository.java
   │  │              │  └─ enums
   │  │              │     └─ WorkspaceRole.java
   │  │              ├─ infrastructure
   │  │              │  ├─ WorkspaceJpaRepository.java
   │  │              │  ├─ WorkspaceMemberJpaRepository.java
   │  │              │  ├─ WorkspaceMemberRepositoryImpl.java
   │  │              │  └─ WorkspaceRepositoryImpl.java
   │  │              └─ presentation
   │  │                 ├─ WorkspaceController.java
   │  │                 └─ dto
   │  │                    ├─ MemberRoleUpdateRequest.java
   │  │                    ├─ WorkspaceJoinRequest.java
   │  │                    ├─ WorkspaceMemberResponse.java
   │  │                    ├─ WorkspaceResponse.java
   │  │                    └─ WorkspaceWriteRequest.java
   │  └─ resources
   │     └─ application.yml
   └─ test
      └─ java
         └─ com
            └─ coope
               └─ server
                  ├─ ServerApplicationTests.java
                  ├─ auth
                  │  └─ application
                  │     ├─ AccountRecoveryServiceTest.java
                  │     ├─ AuthServiceLoginTest.java
                  │     └─ LoginAttemptServiceTest.java
                  ├─ domain
                  │  ├─ document
                  │  │  └─ scheduler
                  │  │     └─ DocumentSyncSchedulerTest.java
                  │  ├─ inquiry
                  │  │  ├─ scheduler
                  │  │  │  └─ InquiryCleanupProcessorTest.java
                  │  │  └─ service
                  │  │     └─ InquiryServiceTest.java
                  │  └─ user
                  │     └─ service
                  │        └─ UserServiceTest.java
                  └─ support
                     └─ AbstractContainerTest.java
```
©generated by [Project Tree Generator](https://woochanleee.github.io/project-tree-generator)


## 🧪예시 결과
### 아래는 Next 시절 결과로 마이그레이션 완료사항은 완성 표시

https://github.com/user-attachments/assets/2b8858a5-de5b-4666-a360-e46cec4e104b

|  이름  | 결과  |
|---|---|
|  메인페이지(완성) |   ![main](https://github.com/user-attachments/assets/aa734e14-7b86-43a8-afed-5d0013b96ddd)|
| 소셜로그인(구글, 완성)  | ![google](https://github.com/user-attachments/assets/c82b8012-ebc8-4ba3-b1de-f1768f607dab) |
| 공지사항(완성)  | ![notice](https://github.com/user-attachments/assets/15a0ef0a-3154-4cd4-b1f4-6564399baafd) |
| 회사소개(완성)  | ![introduce](https://github.com/user-attachments/assets/08f211cb-88a1-438a-aa7b-e7c1a5e738fb) |
| 고객지원(미완성)  |   ![inquiries](https://github.com/user-attachments/assets/cae4b53d-93be-409c-a42c-43fc413b6213)|
|  AI 채팅(완성)  |  ![aiChat](https://github.com/user-attachments/assets/603824d5-5154-473d-b218-49e262a89662) |
| 문서 동시 편집(완성)  |  (데스크탑, 유저1) ![share](https://github.com/user-attachments/assets/1ba15e7c-2ab7-42e7-b244-36898db06c46)(모바일, 유저2)<br/> ![share_mobile](https://github.com/user-attachments/assets/b6d74402-7acf-48e8-be6b-7987735e4f42)|
|  채팅(완성)  | (데스크탑, 유저1) ![chat](https://github.com/user-attachments/assets/e41f96a4-5eab-4bdf-a0ea-6a1a3354a010)(모바일, 유저2)<br/> ![chat_mobile](https://github.com/user-attachments/assets/7665b4bb-9295-401e-9fb8-9598cb9918dd)|
|  통화(완성)  |  (데스크탑, 유저1) ![webrtc](https://github.com/user-attachments/assets/9cb4fa65-4da6-483e-8a14-31a3e1fefe10)(모바일, 유저2)<br/> ![webrtc_mobile](https://github.com/user-attachments/assets/9781c9e9-0803-4c4a-b80a-e0569b60d4a2) <br />데스크탑버전이 모바일버전보다 먼저 들어가서 공유를 시작 => 몇 초이후 모바일이 들어옴이라서 둘 사이에 딜레이가 있어보이는 점 양해부탁드립니다|
|  STT(완성)   |![stt](https://github.com/user-attachments/assets/a8857a28-16ca-4fef-89b5-ef5ba8363ca3) 읽은 문장은 친절한 SQL 튜닝 21p를 읽었습니다. 길게 읽어서 GIF가 길고, 중간에 기록 중지가 빨간색으로 바뀔 때가 있는데 덜 읽었을 때라서 기다려주세요 => 현재 문서 페이지가 완성되지않아 STT가 완료된 결과를 보실 수 없습니다.. DB에는 들어오는데 빠른 시일내에 볼 수 있도록 하겠습니다|

---

## 핵심 기능
### SFU 방식의 화상 회의 (WebRTC, Mediasoup)
- 서버에서 미디어 스트림을 중계하는 SFU 방식을 채택하여 다수 참여 시에도 클라이언트 부하 최적화
- 전체화면 모드 제공

### AI 회의 기록 및 요약 (OpenAI, STT)
- 실시간 마이크 스트림을 캡처하여 Whisper 모델로 텍스트 변환
- 회의 내용을 분석하여 AI 요약본을 자동으로 생성하고 문서로 저장

### 실시간 워크스페이스 (미완성)
- 노션 스타일의 블록 기반 문서 편집기 및 실시간 초대/권한 관리.
- 친구 요청, 실시간 온라인 상태 확인

---

## 🛠️ 개발 중 겪은 문제 & 해결 방법

### GPT API 대화 맥락 유지 및 클라이언트 사이드 캐싱
*  문제: AI 채팅 이용 중 페이지를 새로고침하거나 브라우저를 재시작하면 기존 대화 내역이 유실되어 대화의 맥락이 끊기는 불편함 발생
* 원인: 채팅 데이터가 React State(메모리)에만 저장되어 있어, 브라우저 세션이 종료되면 데이터가 초기화됨
* 해결:
  - Context API + LocalStorage: React Context를 사용하여 전역 상태를 관리하되, useEffect를 통해 상태가 변경될 때마다 브라우저의 LocalStorage에 동기화하여 데이터 지속성 확보
  - 데이터 최적화 전략:
    1. 용량 제한: 무한히 늘어날 수 있는 대화 내역을 방지하기 위해 최대 저장 개수를 100개로 제한
    2. 만료 정책(TTL): 사용자 프라이버시 및 최신성 유지를 위해 7일이 지난 오래된 데이터는 필터링하여 자동 삭제하는 로직 구현

### WebRTC 미디어 동기화 Race Condition 해결
* 문제: 사용자가 회의 입장 시, 기존 참여자의 스트림이 즉시 로드되지 않는 현상 발생
* 원인: 미디어 서버의 프로듀서 목록 송신 시점과 클라이언트 Mediasoup Device 로딩 완료 시점 간의 비동기 타이밍 문제 (Device가 준비되기 전 데이터를 수신하여 처리 실패)
* 해결: 클라이언트가 장치 준비를 마치고 서버에 목록을 요청하는 Pull 방식(getExistingProducers)으로 리팩토링하여 동기화 보장

### 투명 레이어 클릭 간섭(Z-Index) 이슈
* 문제: 상단 네비게이션 바가 투명함에도 불구하고 특정 영역의 버튼 클릭이 작동하지 않음
* 원인: absolute 포지셔닝된 상단 바가 w-full로 설정되어 보이지 않게 클릭 이벤트를 가로챔
* 해결: pointer-events: none을 부모 레이어에 설정하고, 실제 인터랙션이 필요한 아이콘에만 pointer-events: auto를 부여하여 해결

### AWS EC2 배포 환경의 보안 컨텍스트(HTTPS) 이슈
* 문제: 로컬 환경과 달리 배포 환경에서 카메라, 마이크, 화면 공유 등 미디어 스트림이 작동하지 않는 현상 발생
* 원인: WebRTC 보안 정책상 미디어 디바이스 접근(getUserMedia)은 보안 컨텍스트(HTTPS) 내에서만 허용 => Vercel로 배포된 프론트엔드(HTTPS)에서 SSL이 적용되지 않은 EC2 서버(HTTP)로 요청을 보낼 때 Mixed Content 오류 및 보안 거부 발생
* 해결: 도메인 확보 후 A 레코드를 설정하여 EC2 인스턴스와 연결하고, Nginx를 리버스 프록시로 구성한 뒤 Certbot(Let's Encrypt)을 통해 SSL 인증서를 발급받아 HTTPS 환경을 구축하여 해결

### AWS RDS 비용 문제
* 문제: AWS 비용이 설정해둔 예산을 초과함
* 원인: RDS 데이터들을 확인하기 위해 퍼블릭 액세스(집 IP만)를 허용해 두어 1시간마다 0.005$의 추가 비용이 발생
* 해결: SSH 터널링을 통해 DBeaver에서 데이터를 확인할 수 있도록 하고, 퍼블릭 액세스를 차단함 

### 단일 서버 내 다중 서비스 문제
* 문제: 단일 EC2 인스턴스 내에 Spring Boot, Node.js, Redis 등 다중 서비스를 개별 배포하고 관리하는 데 어려움 발생
* 원인: 서비스별로 상이한 런타임 환경(Java, Node) 및 종속성 관리의 복잡성과 로컬-서버 간 인프라 환경 불일치
* 해결: Docker Compose를 도입하여 전체 서비스를 컨테이너화하고, 단일 가상 네트워크로 묶어 서비스 간 통신 및 배포 효율성 확보

---
## 추후 보완점
### 1. Mediasoup 오케스트레이션 및 상태 동기화 (Redis 활용)
- 사용자가 여러 미디어 서버 노드에 분산되어 접속하더라도, 특정 화상 통화 방의 정보를 모든 서버가 실시간으로 공유할 수 있도록 Redis Pub/Sub 및 캐시 시스템을 도입
- 서버 장애 발생 시 통화 세션 정보를 Redis에서 즉시 복구하여 연결 끊김을 최소화하는 고가용성 아키텍처를 목표로 하기..

### 2. 오토스케일링 및 인프라 안정화
- 현재 상태: 단일 EC2 인스턴스 환경에서 운영
- 개선 방향: 트래픽 증가에 따라 서버 자원을 유동적으로 조절할 수 있도록 AWS Auto Scaling을 적용하고, 로드 밸런서를 통해 부하 분산 처리를 자동화하여 서비스 안정성을 높일 예정

### 3. 멀티 디바이스 세션 관리 및 중복 접속 제어
- 현재 상태: 동일한 계정으로 여러 기기(PC, 모바일 등)에서 동시 접속할 경우, 개별 소켓 연결은 가능하나 미디어 서버(Mediasoup) 세션 충돌이나 상태 동기화가 미비함
- 개선 방향:
  * Kick-out 로직 도입: 새로운 기기에서 통화 진입 시, 기존 연결된 기기에 안내 메시지를 송출하고 세션을 강제 종료하는 중복 접속 제어 로직 구현
  * 세션 상태 동기화: Redis를 연동하여 유저의 실시간 통화 참여 상태를 관리하고, 다자간 통화 환경에서 유저가 어느 방에 참여 중인지 정확하게 추적
  * 사용자 경험 강화: 중복 접속 시 사용자에게 선택권(기존 연결 유지 vs 새 연결 전환)을 부여하는 모달 시스템 구축

### 4. AI 채팅 내역 저장
- 현재 상태: AI 채팅 내역을 브라우저 로컬 스토리지에만 저장하여 기기 간 데이터 동기화가 불가능하며 데이터 소실 위험이 있음
- 개선 방향: 채팅 데이터를 서버측 Redis 또는 RDS에 저장하되, TTL(Time To Live) 설정 또는 Batch 작업을 통해 최근 7일간의 대화만 유지하고 자동 삭제되는 시스템 구축 예정
---
## 🔗 참고 자료
* MediasoupDiscourseGroup- [https://mediasoup.discourse.group/t/mediasoup-doesnt-support-in-chrome-version-140/6857](https://mediasoup.discourse.group/t/urgent-upgrade-to-mediasoup-client-3-15-0-asap/6821)
* [WebRTC] mediasoup로 webRTC SFU 구현하기 - https://olive-su.tistory.com/390
* [WebRTC] EC2, Nginx로 WebSocket 서버 배포하기 + HTTPS/WSS - https://93960028.tistory.com/106
* Fullstack Notion Clone - https://youtu.be/0OaDyjB9Ib8?si=lKqW-kyjQPIpD_um

