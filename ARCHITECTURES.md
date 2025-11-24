# Arquiteturas Spring Boot - Estrutura Completa

## Componentes Reutilizáveis (Common/Shared)

### Security
- SecurityConfig
- JwtService
- JwtAuthenticationFilter
- JwtAuthenticationEntryPoint
- UserDetailsServiceImpl

### Config
- CorsConfig
- SwaggerConfig

### Exception Handler
- GlobalExceptionHandler
- ErrorResponse
- ResourceNotFoundException
- BadRequestException

---

## 1. MVC Pattern

```
src/main/java/{package}/
├── config/
│   ├── CorsConfig.java
│   └── SwaggerConfig.java
├── security/
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── UserDetailsServiceImpl.java
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── repository/
│   └── UserRepository.java
├── model/
│   └── User.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ErrorResponse.java
    ├── ResourceNotFoundException.java
    └── BadRequestException.java
```

---

## 2. Layered Architecture

```
src/main/java/{package}/
├── presentation/
│   ├── controller/
│   │   └── UserController.java
│   └── dto/
│       └── UserDTO.java
├── business/
│   ├── service/
│   │   └── UserService.java
│   └── validator/
│       └── UserValidator.java
├── persistence/
│   ├── repository/
│   │   └── UserRepository.java
│   └── entity/
│       └── UserEntity.java
├── config/
│   ├── CorsConfig.java
│   └── SwaggerConfig.java
├── security/
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── UserDetailsServiceImpl.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ErrorResponse.java
    ├── ResourceNotFoundException.java
    └── BadRequestException.java
```

---

## 3. Clean Architecture

```
src/main/java/{package}/
├── domain/
│   ├── model/
│   │   └── User.java
│   └── repository/
│       └── UserRepository.java
├── application/
│   └── usecase/
│       ├── CreateUserUseCase.java
│       ├── GetUserUseCase.java
│       ├── UpdateUserUseCase.java
│       └── DeleteUserUseCase.java
└── infrastructure/
    ├── controller/
    │   └── UserController.java
    ├── persistence/
    │   ├── UserJpaEntity.java
    │   ├── UserJpaRepository.java
    │   └── UserRepositoryImpl.java
    ├── config/
    │   ├── CorsConfig.java
    │   ├── SwaggerConfig.java
    │   └── BeanConfig.java
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── JwtService.java
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtAuthenticationEntryPoint.java
    │   └── UserDetailsServiceImpl.java
    └── exception/
        ├── GlobalExceptionHandler.java
        ├── ErrorResponse.java
        ├── ResourceNotFoundException.java
        └── BadRequestException.java
```

---

## 4. Hexagonal (Ports & Adapters)

```
src/main/java/{package}/
├── domain/
│   └── model/
│       └── User.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateUserPort.java
│   │   │   ├── GetUserPort.java
│   │   │   ├── UpdateUserPort.java
│   │   │   └── DeleteUserPort.java
│   │   └── out/
│   │       ├── UserPersistencePort.java
│   │       └── UserQueryPort.java
│   └── service/
│       └── UserService.java
└── adapter/
    ├── in/
    │   └── web/
    │       └── UserController.java
    ├── out/
    │   └── persistence/
    │       ├── UserJpaEntity.java
    │       ├── UserJpaRepository.java
    │       └── UserPersistenceAdapter.java
    ├── config/
    │   ├── CorsConfig.java
    │   ├── SwaggerConfig.java
    │   └── BeanConfig.java
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── JwtService.java
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtAuthenticationEntryPoint.java
    │   └── UserDetailsServiceImpl.java
    └── exception/
        ├── GlobalExceptionHandler.java
        ├── ErrorResponse.java
        ├── ResourceNotFoundException.java
        └── BadRequestException.java
```

---

## 5. Feature-Driven (com shared)

```
src/main/java/{package}/
├── features/
│   ├── user/
│   │   ├── controller/
│   │   │   └── UserController.java
│   │   ├── service/
│   │   │   └── UserService.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   └── model/
│   │       └── User.java
│   ├── order/
│   │   ├── controller/
│   │   │   └── OrderController.java
│   │   ├── service/
│   │   │   └── OrderService.java
│   │   ├── repository/
│   │   │   └── OrderRepository.java
│   │   └── model/
│   │       └── Order.java
│   └── product/
│       ├── controller/
│       │   └── ProductController.java
│       ├── service/
│       │   └── ProductService.java
│       ├── repository/
│       │   └── ProductRepository.java
│       └── model/
│           └── Product.java
└── shared/
    ├── config/
    │   ├── CorsConfig.java
    │   ├── SwaggerConfig.java
    │   └── BeanConfig.java
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── JwtService.java
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtAuthenticationEntryPoint.java
    │   └── UserDetailsServiceImpl.java
    └── exception/
        ├── GlobalExceptionHandler.java
        ├── ErrorResponse.java
        ├── ResourceNotFoundException.java
        └── BadRequestException.java
```

---

## 6. Domain-Driven Design (DDD)

```
src/main/java/{package}/
├── domain/
│   ├── aggregates/
│   │   └── UserAggregate.java
│   ├── entities/
│   │   └── User.java
│   ├── valueobjects/
│   │   ├── Email.java
│   │   └── UserId.java
│   ├── repositories/
│   │   └── UserRepository.java
│   └── services/
│       └── UserDomainService.java
├── application/
│   └── services/
│       └── UserApplicationService.java
└── infrastructure/
    ├── persistence/
    │   ├── UserJpaEntity.java
    │   ├── UserJpaRepository.java
    │   └── UserRepositoryImpl.java
    ├── web/
    │   └── UserController.java
    ├── config/
    │   ├── CorsConfig.java
    │   ├── SwaggerConfig.java
    │   └── BeanConfig.java
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── JwtService.java
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtAuthenticationEntryPoint.java
    │   └── UserDetailsServiceImpl.java
    └── exception/
        ├── GlobalExceptionHandler.java
        ├── ErrorResponse.java
        ├── ResourceNotFoundException.java
        └── BadRequestException.java
```

---

## 7. CQRS

```
src/main/java/{package}/
├── domain/
│   └── model/
│       └── User.java
├── application/
│   ├── commands/
│   │   ├── CreateUserCommand.java
│   │   ├── UpdateUserCommand.java
│   │   └── DeleteUserCommand.java
│   ├── queries/
│   │   ├── GetUserQuery.java
│   │   └── ListUsersQuery.java
│   └── handlers/
│       ├── CreateUserCommandHandler.java
│       ├── UpdateUserCommandHandler.java
│       ├── DeleteUserCommandHandler.java
│       ├── GetUserQueryHandler.java
│       └── ListUsersQueryHandler.java
├── infrastructure/
│   ├── persistence/
│   │   ├── UserEntity.java
│   │   └── UserRepository.java
│   ├── web/
│   │   └── UserController.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── BeanConfig.java
│   ├── security/
│   │   ├── SecurityConfig.java
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtAuthenticationEntryPoint.java
│   │   └── UserDetailsServiceImpl.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ErrorResponse.java
│       ├── ResourceNotFoundException.java
│       └── BadRequestException.java
└── shared/
    └── CommandBus.java
```

---

## 8. Event-Driven

```
src/main/java/{package}/
├── domain/
│   ├── model/
│   │   └── User.java
│   └── events/
│       ├── UserCreatedEvent.java
│       ├── UserUpdatedEvent.java
│       └── UserDeletedEvent.java
├── application/
│   ├── services/
│   │   └── UserService.java
│   ├── eventhandlers/
│   │   ├── UserCreatedEventHandler.java
│   │   ├── UserUpdatedEventHandler.java
│   │   └── UserDeletedEventHandler.java
│   └── publishers/
│       └── EventPublisher.java
├── infrastructure/
│   ├── messaging/
│   │   └── KafkaEventPublisher.java
│   ├── persistence/
│   │   ├── UserEntity.java
│   │   └── UserRepository.java
│   ├── web/
│   │   └── UserController.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── SwaggerConfig.java
│   │   ├── BeanConfig.java
│   │   └── KafkaConfig.java
│   ├── security/
│   │   ├── SecurityConfig.java
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtAuthenticationEntryPoint.java
│   │   └── UserDetailsServiceImpl.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ErrorResponse.java
│       ├── ResourceNotFoundException.java
│       └── BadRequestException.java
└── shared/
    └── DomainEvent.java
```

---

## 9. Onion Architecture

```
src/main/java/{package}/
├── core/
│   ├── domain/
│   │   ├── model/
│   │   │   └── User.java
│   │   └── services/
│   │       └── UserDomainService.java
│   └── interfaces/
│       └── repositories/
│           └── IUserRepository.java
├── services/
│   └── application/
│       └── UserApplicationService.java
└── infrastructure/
    ├── persistence/
    │   ├── UserEntity.java
    │   ├── UserRepository.java
    │   └── UserRepositoryImpl.java
    ├── web/
    │   └── UserController.java
    ├── config/
    │   ├── CorsConfig.java
    │   ├── SwaggerConfig.java
    │   └── BeanConfig.java
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── JwtService.java
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtAuthenticationEntryPoint.java
    │   └── UserDetailsServiceImpl.java
    └── exception/
        ├── GlobalExceptionHandler.java
        ├── ErrorResponse.java
        ├── ResourceNotFoundException.java
        └── BadRequestException.java
```

---

## 10. Vertical Slice

```
src/main/java/{package}/
├── features/
│   ├── user/
│   │   ├── create/
│   │   │   ├── CreateUserRequest.java
│   │   │   ├── CreateUserResponse.java
│   │   │   └── CreateUserHandler.java
│   │   ├── get/
│   │   │   ├── GetUserRequest.java
│   │   │   ├── GetUserResponse.java
│   │   │   └── GetUserHandler.java
│   │   ├── update/
│   │   │   ├── UpdateUserRequest.java
│   │   │   ├── UpdateUserResponse.java
│   │   │   └── UpdateUserHandler.java
│   │   ├── delete/
│   │   │   ├── DeleteUserRequest.java
│   │   │   ├── DeleteUserResponse.java
│   │   │   └── DeleteUserHandler.java
│   │   ├── model/
│   │   │   └── User.java
│   │   └── UserController.java
│   ├── order/
│   │   ├── create/
│   │   │   ├── CreateOrderRequest.java
│   │   │   ├── CreateOrderResponse.java
│   │   │   └── CreateOrderHandler.java
│   │   ├── get/
│   │   │   ├── GetOrderRequest.java
│   │   │   ├── GetOrderResponse.java
│   │   │   └── GetOrderHandler.java
│   │   ├── model/
│   │   │   └── Order.java
│   │   └── OrderController.java
│   └── product/
│       ├── create/
│       │   ├── CreateProductRequest.java
│       │   ├── CreateProductResponse.java
│       │   └── CreateProductHandler.java
│       ├── get/
│       │   ├── GetProductRequest.java
│       │   ├── GetProductResponse.java
│       │   └── GetProductHandler.java
│       ├── model/
│       │   └── Product.java
│       └── ProductController.java
└── shared/
    ├── config/
    │   ├── CorsConfig.java
    │   ├── SwaggerConfig.java
    │   └── BeanConfig.java
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── JwtService.java
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtAuthenticationEntryPoint.java
    │   └── UserDetailsServiceImpl.java
    └── exception/
        ├── GlobalExceptionHandler.java
        ├── ErrorResponse.java
        ├── ResourceNotFoundException.java
        └── BadRequestException.java
```

---

## Resumo de Padrões

### Arquiteturas Simples (config e security na raiz)
1. MVC
2. Layered

### Arquiteturas Orientadas a Domínio (config e security em infrastructure)
3. Clean Architecture
4. Hexagonal
6. DDD
7. CQRS
8. Event-Driven
9. Onion

### Arquiteturas Orientadas a Features (config e security em shared)
5. Feature-Driven
10. Vertical Slice

---

## Componentes Comuns por Tipo

### Security (todas arquiteturas)
- SecurityConfig
- JwtService
- JwtAuthenticationFilter
- JwtAuthenticationEntryPoint
- UserDetailsServiceImpl

### Config (todas arquiteturas)
- CorsConfig
- SwaggerConfig
- BeanConfig (quando necessário)

### Exception (todas arquiteturas)
- GlobalExceptionHandler
- ErrorResponse
- ResourceNotFoundException
- BadRequestException
