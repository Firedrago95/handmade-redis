# Handmade Redis (Build Your Own Redis)

이 프로젝트는 학습 목적으로 Redis 서버를 밑바닥부터 구현하는 프로젝트입니다. 
**CodeCrafters의 "Build Your Own Redis" 챌린지 커리큘럼**을 로컬 환경에서 TDD 방식으로 진행합니다.

## 🎯 프로젝트 목표
- TCP 소켓 프로그래밍 및 네트워크 통신 원리 이해
- RESP (REdis Serialization Protocol) 디코딩 및 인코딩 파서 구현
- 멀티스레딩 또는 이벤트 루프(NIO)를 활용한 동시성 처리
- 인메모리(In-Memory) Key-Value 저장소와 지연 평가(Lazy Eviction)/TTL 처리 로직 구현

## 💡 학습 방식 및 협업 룰
1. **직접 구현 (No Auto-code)**: 에이전트(AI)는 절대 프로덕션 코드(`src/main`)를 대신 짜주지 않습니다. 구현과 아키텍처 고민은 전적으로 개발자 본인의 몫입니다.
2. **TDD 에이전트 보조**: 사용자가 자연어로 구현할 스펙(예: "포트 6379에 접속해서 PING을 쏘는 테스트 짜줘")을 요구하면, 에이전트가 그에 맞는 테스트 코드를 작성해 줍니다. 
3. **코드 리뷰 교차 검증**:
   - 각 Stage(단계) 구현이 끝날 때마다 PR을 생성하고 **CodeRabbit**을 통해 전반적인 코드 품질 리뷰를 받습니다.
   - 에이전트에게 명시적으로 리뷰를 요청하여, 놓친 엣지 케이스, 소켓/스트림 누수, 동시성 이슈 등 아키텍처 관점의 깊이 있는 피드백을 받습니다.
4. **CodeCrafters 정석 가이드 준수**:
   - 에이전트(AI)는 앞으로 모든 아키텍처 및 파서 구현 가이드 시, 반드시 검색 및 CodeCrafters 공식 레퍼런스 솔루션 정석(`InputStream` 기반 스트림 파서, 비트/바이트 읽기, 명확한 스펙 준수)을 확인한 후 이를 100% 우선 적용하여 방향을 안내합니다.

## 🚀 커리큘럼 진행 상황 (Stages)

> CodeCrafters "Build Your Own Redis" 챌린지의 공개 스테이지 구성을 참고해 재구성한 커리큘럼입니다.
> 각 Stage는 Red(에이전트가 테스트 작성) → Green(직접 구현) → Refactor & Review 사이클로 진행합니다.

### Phase 1: TCP 서버 기초
- [x] Stage 1-1. 6379 포트 TCP 서버 바인딩
- [x] Stage 1-2. 단일 클라이언트 연결 수락 및 응답
- [x] Stage 1-3. 동일 연결에서 다중 명령 순차 처리
- [x] Stage 1-4. 다중 클라이언트 동시 처리 (Thread-per-connection / Virtual Thread)

### Phase 2: RESP 프로토콜
- [x] Stage 2-1. RESP 파서: Simple String (`+OK\r\n`)
- [x] Stage 2-2. RESP 파서: Bulk String (`$3\r\nfoo\r\n`)
- [x] Stage 2-3. RESP 파서: Array (`*2\r\n...`)
- [x] Stage 2-4. RESP 파서: Integer, Error 타입
- [x] Stage 2-5. 커맨드 디스패처 (파싱된 명령어 → 핸들러 라우팅)

### Phase 3: 기본 명령어
- [ ] Stage 3-1. PING / PING \<message\>
- [ ] Stage 3-2. ECHO
- [ ] Stage 3-3. SET / GET
- [ ] Stage 3-4. SET with EX/PX (TTL 옵션)
- [ ] Stage 3-5. DEL, EXISTS

### Phase 4: 데이터 만료 (TTL)
- [ ] Stage 4-1. Passive Expiration (조회 시 만료 체크, lazy)
- [ ] Stage 4-2. Active Expiration (백그라운드 주기적 스캔)
- [ ] Stage 4-3. EXPIRE / TTL / PERSIST 명령어

### Phase 5: 확장 자료구조 (선택)
- [ ] Stage 5-1. LPUSH / RPUSH / LRANGE (List)
- [ ] Stage 5-2. HSET / HGET (Hash)
- [ ] Stage 5-3. Type 명령어 (RESP 타입 검사)

### Phase 6: 영속성 (RDB)
- [ ] Stage 6-1. RDB 파일 포맷 읽기 (기본 구조)
- [ ] Stage 6-2. --dbfilename / --dir 옵션 지원
- [ ] Stage 6-3. 저장된 키/만료시각 로드

### Phase 7: 복제 (선택, 심화)
- [ ] Stage 7-1. REPLICAOF 기본 핸드셰이크
- [ ] Stage 7-2. Master → Replica 커맨드 전파

### Phase 8: 2차 리팩토링 (선택, 심화)
- [ ] Stage 8-1. Java NIO (Selector) 기반 싱글 스레드 이벤트 루프로 네트워크 계층 교체
- [ ] Stage 8-2. 논블로킹 I/O 버퍼 파싱 및 기존 통합 테스트 회귀 검증

**진행률**: 9 / 27

