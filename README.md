# coope-back
Coope 프로젝트 Spring boot 마이그레이션

### 자세한 사항은 프론트 Readme 참고
프론트: https://github.com/JinHaeryeong/coope-front

## 프로젝트 구조
```
📦 
.gitattributes
.github
│  └─ workflows
│     └─ deploy.yml
.gitignore
README.md
build.gradle
gradle
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
   │  │           ├─ domain
   │  │           │  ├─ ai
   │  │           │  │  ├─ controller
   │  │           │  │  │  └─ AiController.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  └─ VoiceProcessResponse.java
   │  │           │  │  └─ service
   │  │           │  │     └─ AiService.java
   │  │           │  ├─ aichat
   │  │           │  │  ├─ controller
   │  │           │  │  │  └─ AIChatController.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  ├─ AIChatMessage.java
   │  │           │  │  │  ├─ AIChatRequest.java
   │  │           │  │  │  └─ AIChatStreamRequest.java
   │  │           │  │  └─ service
   │  │           │  │     └─ AIChatService.java
   │  │           │  ├─ auth
   │  │           │  │  ├─ controller
   │  │           │  │  │  └─ AuthController.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  ├─ LoginRequest.java
   │  │           │  │  │  └─ LoginResponse.java
   │  │           │  │  ├─ handler
   │  │           │  │  │  └─ OAuth2AuthenticationSuccessHandler.java
   │  │           │  │  ├─ oauth
   │  │           │  │  │  ├─ GoogleUserInfo.java
   │  │           │  │  │  └─ OAuth2UserInfo.java
   │  │           │  │  └─ service
   │  │           │  │     ├─ AuthService.java
   │  │           │  │     ├─ CustomOAuth2UserService.java
   │  │           │  │     └─ CustomUserDetailsService.java
   │  │           │  ├─ chat
   │  │           │  │  ├─ controller
   │  │           │  │  │  ├─ ChatController.java
   │  │           │  │  │  └─ ChatStompController.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  ├─ ChatListResponse.java
   │  │           │  │  │  ├─ ChatRoomResponse.java
   │  │           │  │  │  ├─ ChatUploadResponse.java
   │  │           │  │  │  ├─ CreateGroupRequest.java
   │  │           │  │  │  ├─ MessageRequest.java
   │  │           │  │  │  └─ MessageResponse.java
   │  │           │  │  ├─ entity
   │  │           │  │  │  ├─ ChatParticipant.java
   │  │           │  │  │  ├─ ChatRoom.java
   │  │           │  │  │  ├─ Message.java
   │  │           │  │  │  └─ RoomType.java
   │  │           │  │  ├─ repository
   │  │           │  │  │  ├─ ChatParticipantRepository.java
   │  │           │  │  │  ├─ ChatRoomRepository.java
   │  │           │  │  │  └─ MessageRepository.java
   │  │           │  │  └─ service
   │  │           │  │     └─ ChatService.java
   │  │           │  ├─ comment
   │  │           │  │  ├─ controller
   │  │           │  │  │  └─ CommentController.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  ├─ CommentRequest.java
   │  │           │  │  │  └─ CommentResponse.java
   │  │           │  │  ├─ entity
   │  │           │  │  │  └─ Comment.java
   │  │           │  │  ├─ repository
   │  │           │  │  │  └─ CommentRepository.java
   │  │           │  │  └─ service
   │  │           │  │     └─ CommentService.java
   │  │           │  ├─ common
   │  │           │  │  └─ entity
   │  │           │  │     └─ BaseTimeEntity.java
   │  │           │  ├─ document
   │  │           │  │  ├─ controller
   │  │           │  │  │  └─ DocumentController.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  ├─ DocumentCreateRequest.java
   │  │           │  │  │  └─ DocumentResponse.java
   │  │           │  │  ├─ entity
   │  │           │  │  │  └─ Document.java
   │  │           │  │  ├─ repository
   │  │           │  │  │  └─ DocumentRepository.java
   │  │           │  │  └─ service
   │  │           │  │     └─ DocumentService.java
   │  │           │  ├─ friend
   │  │           │  │  ├─ controller
   │  │           │  │  │  └─ FriendController.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  └─ FriendResponse.java
   │  │           │  │  ├─ entity
   │  │           │  │  │  ├─ Friend.java
   │  │           │  │  │  └─ FriendStatus.java
   │  │           │  │  ├─ repository
   │  │           │  │  │  └─ FriendRepository.java
   │  │           │  │  └─ service
   │  │           │  │     └─ FriendService.java
   │  │           │  ├─ notice
   │  │           │  │  ├─ controller
   │  │           │  │  │  └─ NoticeController.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  ├─ NoticeDetailResponse.java
   │  │           │  │  │  ├─ NoticeResponse.java
   │  │           │  │  │  └─ NoticeWriteRequest.java
   │  │           │  │  ├─ entity
   │  │           │  │  │  └─ Notice.java
   │  │           │  │  ├─ repository
   │  │           │  │  │  └─ NoticeRepository.java
   │  │           │  │  └─ service
   │  │           │  │     └─ NoticeService.java
   │  │           │  ├─ user
   │  │           │  │  ├─ controller
   │  │           │  │  │  └─ UserController.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  ├─ SignupRequest.java
   │  │           │  │  │  ├─ SignupResponse.java
   │  │           │  │  │  ├─ UserResponse.java
   │  │           │  │  │  └─ UserSearchResponse.java
   │  │           │  │  ├─ entity
   │  │           │  │  │  └─ User.java
   │  │           │  │  ├─ enums
   │  │           │  │  │  ├─ Provider.java
   │  │           │  │  │  └─ Role.java
   │  │           │  │  ├─ repository
   │  │           │  │  │  └─ UserRepository.java
   │  │           │  │  └─ service
   │  │           │  │     └─ UserService.java
   │  │           │  └─ workspace
   │  │           │     ├─ controller
   │  │           │     │  └─ WorkspaceController.java
   │  │           │     ├─ dto
   │  │           │     │  ├─ WorkspaceResponse.java
   │  │           │     │  └─ WorkspaceWriteRequest.java
   │  │           │     ├─ entity
   │  │           │     │  ├─ Workspace.java
   │  │           │     │  └─ WorkspaceMember.java
   │  │           │     ├─ enums
   │  │           │     │  └─ WorkspaceRole.java
   │  │           │     ├─ repository
   │  │           │     │  ├─ WorkspaceMemberRepository.java
   │  │           │     │  └─ WorkspaceRepository.java
   │  │           │     └─ service
   │  │           │        └─ WorkspaceService.java
   │  │           └─ global
   │  │              ├─ config
   │  │              │  ├─ JwtProperties.java
   │  │              │  ├─ RedisConfig.java
   │  │              │  ├─ SecurityConfig.java
   │  │              │  ├─ WebConfig.java
   │  │              │  └─ WebSocketConfig.java
   │  │              ├─ error
   │  │              │  ├─ GlobalExceptionHandler.java
   │  │              │  ├─ dto
   │  │              │  │  └─ ErrorResponse.java
   │  │              │  └─ exception
   │  │              │     ├─ AccessDeniedException.java
   │  │              │     ├─ AiServiceException.java
   │  │              │     ├─ AuthenticationException.java
   │  │              │     ├─ BadRequestException.java
   │  │              │     ├─ CommentNotFoundException.java
   │  │              │     ├─ DocumentNotFoundException.java
   │  │              │     ├─ FileStorageException.java
   │  │              │     ├─ FriendException.java
   │  │              │     ├─ InvalidTokenException.java
   │  │              │     ├─ NoticeNotFoundException.java
   │  │              │     ├─ UserNotFoundException.java
   │  │              │     └─ WorkspaceNotFoundException.java
   │  │              ├─ infra
   │  │              │  ├─ FileService.java
   │  │              │  ├─ ImageCategory.java
   │  │              │  ├─ LocalFileService.java
   │  │              │  └─ S3FileService.java
   │  │              └─ security
   │  │                 ├─ CustomAuthenticationEntryPoint.java
   │  │                 ├─ FilterChannelInterceptor.java
   │  │                 ├─ JwtAuthenticationFilter.java
   │  │                 ├─ JwtProvider.java
   │  │                 └─ UserDetailsImpl.java
   │  └─ resources
   │     └─ application.yml
   └─ test
      └─ java
         └─ com
            └─ coope
               └─ server
                  ├─ ServerApplicationTests.java
                  └─ notice
                     └─ NoticeBulkInsertTest.java
```
©generated by [Project Tree Generator](https://woochanleee.github.io/project-tree-generator)
