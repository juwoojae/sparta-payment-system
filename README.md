# 💯 포인트 기반 결제 시스템 프로젝트

## 📌 프로젝트 소개

본 프로젝트는 **PortOne Payments API(v2)** 기반으로 구현된  
**실제 결제 → 검증 → 후처리 → 취소 → 환불 이력 → 후처리 롤백**까지 전 과정을 포함한  
전자상거래형 결제 시스템입니다.

주문 · 결제 · 포인트 · 멤버십 · 재고 등 복잡한 도메인 간 의존성을 고려한  
실무형 아키텍처를 목표로 설계했습니다.

---

## 🎯 프로젝트 목표

- 실결제 연동(PortOne) 전체 플로우 완성
- 결제 완료 후 후처리 로직(재고, 포인트, 멤버십 등급) 안정화
- 결제 취소 후 롤백 처리(재고 복구, 포인트 회수, 멤버십 재계산) 구현
- CI/CD 파이프라인과 연계한 자동 배포
- WebFlux + JPA 혼합 환경에서 안정적인 트랜잭션 처리
- 복잡한 비즈니스 로직 구조화 역량 향상

---

## 🧩 기술 스택

- Java 17
- Spring Boot 3.x
- Spring MVC + Spring WebFlux(WebClient)
- Spring Security + JWT
- Spring Data JPA
- Lombok

### **사용 툴**

- MySQL 8.0
- AWS EC2 / S3 / GitHub Actions
- PortOne Payments API v2

---

## 비즈니스 로직 플로우차트

![payment-system-flowchart.png](payment-system-flowchart.png)

## ERD 설계

![ERD.png](ERD.png)

## 📁 프로젝트 디렉토리 구조

```
payment-system/
├── controller/          # API 진입점 (Auth, Orders, Payment)
├── service/             # 비즈니스 로직 계층
├── repository/          # Spring Data JPA
├── entity/              # 도메인 엔티티 (User, Order, Payment 등)
├── dto/                 # Request / Response DTO
├── client/              # PortOne API WebClient
├── global/              # JWT, Security, WebFlux, 예외 처리
├── resources/
│   ├── static/          # 결제 JS, HTML 화면
│   ├── templates/       # Thymeleaf View
│   └── application.yml  # DB / PortOne 설정
└── PaymentSystemApplication.java
```