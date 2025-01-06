# 서비스 소개 
![HOME _ 웹용 가이드(푸터 영역 추가)](https://github.com/user-attachments/assets/f67995f3-4664-4338-b046-afaaf6efbf41)


운동을 좋아하는 모든 이들에게 매칭/커뮤니티/채팅 서비스를 제공합니다. 


# 서비스 주요 기능
<br>

1. 커뮤니티
2. 통합 검색 기능
3. 새싹과 고수 회원 매칭
4. 채팅
5. 알림

# 기술 스택 
- Tech Stack
  - Java, Spring Boot, MySQL, JPA, QueryDSL, Redis, JUnit
- Infra
  - AWS EC2, AWS RDS, AWS ElasticCache, AWS SQS, AWS SNS
- DevOps
  - Jenkins, Docker


# 구현 및 문제 해결 과정

## 1. 알림 시스템 구현

<img width="617" alt="스크린샷 2025-01-06 오후 9 25 02" src="https://github.com/user-attachments/assets/90bafe16-9779-4e4e-b935-d43709c5e5d4" />


### [Async와 EventListener 기반의 비동기 처리 알림 시스템을 AWS SNS와 SQS 기반의 PUB/SUB 구조로 리팩토링](https://velog.io/@dlrkdus/AWS-SNS%EC%99%80-SQS%EB%A1%9C-%EC%95%8C%EB%9E%8C-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EB%A6%AC%ED%8C%A9%ED%86%A0%EB%A7%81%ED%95%98%EA%B8%B0-1-%EC%84%A4%EA%B3%84)

**`문제`** <br>
기존의 Async와 EventListener 구조는 어플리케이션 의존성이 너무 커 **이벤트 내구성 부족, 트래픽 증가 시 스레드 풀이 포화 가능성, 비동기 처리 재시도 전략 미수립** 등의 문제점이 있음


**`해결`** <br>
AWS SQS는 큐에 메세지를 넣어놓고 Worker가 폴링해 쓰는 구조이기 때문에 발행 속도와 처리 속도 간의 차이를 완충 <br>
AWS SNS가 DB 저장 Worker와 Sse Worker에게 이벤트를 발행해 DB 병목이 일어나더라도 실시간 알림 작업은 영향을 받지 않음 <br>
DLQ를 생성해두고 누적 메세지가 임계값을 넘어가면 Cloudwatch로 알림 발송


## 2. 커뮤니티 CRUD 구현

### [주간 인기글 카테고리 구현](https://velog.io/@dlrkdus/Spring-%EC%9D%B8%EA%B8%B0%EA%B8%80-%EA%B8%B0%EB%8A%A5%EC%9D%84-%EA%B5%AC%ED%98%84%ED%95%98%EB%A9%B0-%ED%96%88%EB%8D%98-%EA%B8%B0%EC%88%A0%EC%A0%81-%EA%B3%A0%EB%AF%BC%EB%93%A4)

**`문제`**  <br>
자주 접근하는 특성을 가진 인기글 목록 특성상 접근과 정렬 비용 절감 필요

**`해결`** <br>
Redisson의 SortedSet을 활용하여 DB I/O 비용 감소, SortedSet 자료구조로 정렬 비용 감소, <br> TTL로 외부 스케줄러 없이 주간 인기글 자동 갱신

### [검색 성능 최적화](https://velog.io/@dlrkdus/MySQL-%EC%9D%B8%EB%8D%B1%EC%8A%A4-%EC%84%A4%EA%B3%84%EC%99%80-%EA%B2%80%EC%83%89-API%EC%97%90%EC%84%9C%EC%9D%98-%EC%A0%81%EC%9A%A9)

**`문제`** <br>
글 본문이 길어질 경우 DB 병목 현상

**`해결`** <br>
단일/Full Text 인덱스로 검색 성능 73% 개선 <br>
Full-Text Search 쿼리문이 Hibernate Dialect에 정의되어 있지 않은 문제는 MATCH AGAINST 쿼리문을 커스텀 쿼리로 등록함으로써 해결
SQL Explain으로 쿼리 분석해 효율 개선 확인

### [대댓글 조회 JPA N+1 문제 해결](https://velog.io/@dlrkdus/%EB%8C%80%EB%8C%93%EA%B8%80-%EA%B8%B0%EB%8A%A5%EC%97%90%EC%84%9C-N1-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0%ED%95%98%EA%B8%B0)

**`문제`** <br>
댓글 1개를 조회하는데 지연로딩으로 인한 대댓글 조회 N개 쿼리 추가 발생

**`해결`** <br>
배치 로딩 적용으로 쿼리 수 90% 감소, 조회 성능 약 60% 개선


