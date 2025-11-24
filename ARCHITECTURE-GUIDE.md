# Guia Rápido de Arquiteturas

## Componentes Compartilhados

### Security (Todas Arquiteturas)
```java
SecurityConfig.java
JwtService.java
JwtAuthenticationFilter.java
JwtAuthenticationEntryPoint.java
UserDetailsServiceImpl.java
```

### Config (Todas Arquiteturas)
```java
CorsConfig.java
SwaggerConfig.java
BeanConfig.java
```

### Exception (Todas Arquiteturas)
```java
GlobalExceptionHandler.java
ErrorResponse.java
ResourceNotFoundException.java
BadRequestException.java
```

---

## Localização por Arquitetura

### Arquiteturas Simples
**MVC e Layered**
- `config/` na raiz
- `security/` na raiz
- `exception/` na raiz

### Arquiteturas Orientadas a Domínio
**Clean, Hexagonal, DDD, CQRS, Event-Driven, Onion**
- `infrastructure/config/`
- `infrastructure/security/`
- `infrastructure/exception/`

### Arquiteturas Orientadas a Features
**Feature-Driven e Vertical Slice**
- `shared/config/`
- `shared/security/`
- `shared/exception/`

---

## Estruturas Resumidas

### 1. MVC
```
config/ security/ exception/
controller/ service/ repository/ model/
```

### 2. Layered
```
presentation/controller/ presentation/dto/
business/service/ business/validator/
persistence/repository/ persistence/entity/
config/ security/ exception/
```

### 3. Clean Architecture
```
domain/model/ domain/repository/
application/usecase/
infrastructure/controller/ infrastructure/persistence/
infrastructure/config/ infrastructure/security/ infrastructure/exception/
```

### 4. Hexagonal
```
domain/model/
application/port/in/ application/port/out/ application/service/
adapter/in/web/ adapter/out/persistence/
adapter/config/ adapter/security/ adapter/exception/
```

### 5. Feature-Driven
```
features/{feature}/controller/
features/{feature}/service/
features/{feature}/repository/
features/{feature}/model/
shared/config/ shared/security/ shared/exception/
```

### 6. DDD
```
domain/aggregates/ domain/entities/ domain/valueobjects/
domain/repositories/ domain/services/
application/services/
infrastructure/persistence/ infrastructure/web/
infrastructure/config/ infrastructure/security/ infrastructure/exception/
```

### 7. CQRS
```
domain/model/
application/commands/ application/queries/ application/handlers/
infrastructure/persistence/ infrastructure/web/
infrastructure/config/ infrastructure/security/ infrastructure/exception/
shared/
```

### 8. Event-Driven
```
domain/model/ domain/events/
application/services/ application/eventhandlers/ application/publishers/
infrastructure/messaging/ infrastructure/persistence/ infrastructure/web/
infrastructure/config/ infrastructure/security/ infrastructure/exception/
shared/
```

### 9. Onion
```
core/domain/model/ core/domain/services/
core/interfaces/repositories/
services/application/
infrastructure/persistence/ infrastructure/web/
infrastructure/config/ infrastructure/security/ infrastructure/exception/
```

### 10. Vertical Slice
```
features/{feature}/create/
features/{feature}/get/
features/{feature}/update/
features/{feature}/delete/
features/{feature}/model/
shared/config/ shared/security/ shared/exception/
```

---

## Decisão Rápida

**Projeto Simples?** → MVC ou Layered

**Domínio Rico?** → DDD ou Clean

**Microserviços?** → Hexagonal ou CQRS

**Modular por Features?** → Feature-Driven

**Alta Escalabilidade?** → Event-Driven ou CQRS

**Operações Independentes?** → Vertical Slice

**Dependências Mínimas?** → Onion

---

## Templates Disponíveis

### Common
- Controller.peb
- Entity.peb
- Repository.peb
- Service.peb
- SwaggerConfig.peb
- SecurityConfig.peb
- CorsConfig.peb
- JwtService.peb
- JwtAuthenticationFilter.peb
- JwtAuthenticationEntryPoint.peb
- UserDetailsServiceImpl.peb
- GlobalExceptionHandler.peb
- ErrorResponse.peb
- ResourceNotFoundException.peb
- BadRequestException.peb

### Clean Architecture
- UseCase.peb
- DomainModel.peb
- InfrastructureController.peb
- JpaEntity.peb
- JpaRepository.peb
- RepositoryImpl.peb

### Hexagonal
- DomainModel.peb
- InputPort.peb
- OutputPort.peb
- Service.peb
- InAdapter.peb
- OutAdapter.peb
- JpaEntity.peb
- JpaRepository.peb

### DDD
- Aggregate.peb
- Entity.peb
- ValueObject.peb
- DomainService.peb
- DomainRepository.peb
- InfrastructureRepository.peb
- JpaEntity.peb
- JpaRepository.peb
- Controller.peb

### CQRS
- Command.peb
- Query.peb
- CommandHandler.peb
- QueryHandler.peb
- Model.peb
- Repository.peb
- Controller.peb

### Event-Driven
- Event.peb
- Model.peb
- Service.peb
- EventHandler.peb
- EventPublisher.peb
- Repository.peb
- Controller.peb
