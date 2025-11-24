# Comparação de Arquiteturas

## Matriz de Decisão

| Arquitetura | Complexidade | Domínio Rico | Testabilidade | Escalabilidade | Microserviços | Features Independentes |
|-------------|--------------|--------------|---------------|----------------|---------------|------------------------|
| MVC | ⭐ | ❌ | ⭐⭐ | ⭐⭐ | ❌ | ❌ |
| Layered | ⭐⭐ | ❌ | ⭐⭐⭐ | ⭐⭐ | ❌ | ❌ |
| Clean | ⭐⭐⭐ | ✅ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ | ❌ |
| Hexagonal | ⭐⭐⭐⭐ | ✅ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ | ❌ |
| Feature-Driven | ⭐⭐ | ❌ | ⭐⭐⭐ | ⭐⭐⭐ | ✅ | ✅ |
| DDD | ⭐⭐⭐⭐⭐ | ✅ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ | ❌ |
| CQRS | ⭐⭐⭐⭐ | ✅ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ | ❌ |
| Event-Driven | ⭐⭐⭐⭐⭐ | ✅ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ | ✅ |
| Onion | ⭐⭐⭐⭐ | ✅ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ | ❌ |
| Vertical Slice | ⭐⭐⭐ | ❌ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ | ✅ |

---

## Características Principais

### MVC
**Quando usar:** CRUD simples, MVPs, projetos pequenos
**Vantagens:** Simples, rápido para desenvolver
**Desvantagens:** Acoplamento, difícil de testar

### Layered
**Quando usar:** Aplicações tradicionais, separação clara de responsabilidades
**Vantagens:** Organização clara, fácil de entender
**Desvantagens:** Dependência entre camadas

### Clean Architecture
**Quando usar:** Domínio rico, testes importantes, independência de frameworks
**Vantagens:** Alta testabilidade, independência
**Desvantagens:** Mais boilerplate

### Hexagonal (Ports & Adapters)
**Quando usar:** Múltiplos adaptadores (REST, GraphQL, CLI), testes importantes
**Vantagens:** Isolamento total do domínio, flexibilidade
**Desvantagens:** Complexidade inicial

### Feature-Driven
**Quando usar:** Features bem definidas, equipes por feature
**Vantagens:** Baixo acoplamento entre features, fácil de escalar equipe
**Desvantagens:** Possível duplicação de código

### DDD
**Quando usar:** Domínio complexo, ubiquitous language importante
**Vantagens:** Alinhamento com negócio, domínio protegido
**Desvantagens:** Curva de aprendizado alta

### CQRS
**Quando usar:** Leitura e escrita com necessidades diferentes, alta performance
**Vantagens:** Otimização separada, escalabilidade
**Desvantagens:** Complexidade, eventual consistency

### Event-Driven
**Quando usar:** Sistema distribuído, comunicação assíncrona
**Vantagens:** Desacoplamento, escalabilidade horizontal
**Desvantagens:** Debug complexo, eventual consistency

### Onion
**Quando usar:** Dependências controladas, domínio no centro
**Vantagens:** Dependências unidirecionais, testabilidade
**Desvantagens:** Complexidade conceitual

### Vertical Slice
**Quando usar:** Features independentes, deploy incremental
**Vantagens:** Mudanças isoladas, sem layers desnecessárias
**Desvantagens:** Possível duplicação

---

## Fluxo de Dados

### MVC
```
Request → Controller → Service → Repository → Database
                    ↓
                Response
```

### Layered
```
Request → Presentation → Business → Persistence → Database
                      ↓
                  Response
```

### Clean Architecture
```
Request → Infrastructure → Application → Domain
                        ↓
                    Response
```

### Hexagonal
```
Request → InAdapter → Application → Domain ← OutAdapter → Database
                   ↓
               Response
```

### Feature-Driven
```
Request → Feature/Controller → Feature/Service → Feature/Repository → Database
                            ↓
                        Response
```

### DDD
```
Request → Infrastructure → Application → Domain
              ↓
          Response
```

### CQRS
```
Command → CommandHandler → Write Model → Database
Query → QueryHandler → Read Model → Database
```

### Event-Driven
```
Request → Service → Event Publisher → Event Bus → Event Handlers
                 ↓
             Response
```

### Onion
```
Request → Infrastructure → Services → Core/Domain
                        ↓
                    Response
```

### Vertical Slice
```
Request → Feature/Handler → Database
                 ↓
             Response
```

---

## Quantidade de Arquivos por Feature

### MVC
**4-5 arquivos:** Controller, Service, Repository, Entity, DTO

### Layered
**6-8 arquivos:** Controller, DTO, Service, Validator, Repository, Entity

### Clean Architecture
**7-10 arquivos:** Controller, UseCase, DomainModel, Repository Interface, Repository Impl, JPA Entity, JPA Repository

### Hexagonal
**8-12 arquivos:** Controller, InputPort, OutputPort, Service, InAdapter, OutAdapter, JPA Entity, JPA Repository

### Feature-Driven
**4-5 arquivos por feature:** Controller, Service, Repository, Entity

### DDD
**10-15 arquivos:** Aggregate, Entity, ValueObject, Domain Service, Repository Interface, Repository Impl, Controller, JPA Entity

### CQRS
**8-12 arquivos:** Command, Query, CommandHandler, QueryHandler, Model, Repository, Controller

### Event-Driven
**10-15 arquivos:** Event, Model, Service, EventHandler, EventPublisher, Repository, Controller

### Onion
**8-10 arquivos:** Domain Model, Domain Service, Interface, Application Service, Repository Impl, Controller, JPA Entity

### Vertical Slice
**3-6 arquivos por operação:** Request, Response, Handler

---

## Tamanho de Equipe Recomendado

**MVC, Layered:** 1-5 devs

**Clean, Hexagonal, Onion:** 3-10 devs

**Feature-Driven, Vertical Slice:** 5-50 devs (múltiplas equipes)

**DDD, CQRS, Event-Driven:** 5-20 devs (domínio complexo)

---

## Performance

### Alta Leitura
**Melhor:** CQRS, Event-Driven
**Bom:** Vertical Slice, Feature-Driven
**Adequado:** Demais

### Alta Escrita
**Melhor:** Event-Driven, CQRS
**Bom:** Vertical Slice
**Adequado:** Demais

### Leitura e Escrita Balanceadas
**Melhor:** Clean, Hexagonal, Onion
**Bom:** Layered, Feature-Driven
**Adequado:** MVC, DDD

---

## Manutenibilidade

### Fácil de Modificar
**Melhor:** Vertical Slice, Feature-Driven, Hexagonal
**Bom:** Clean, CQRS
**Médio:** Layered, Onion, DDD
**Difícil:** Event-Driven, MVC

### Fácil de Entender
**Melhor:** MVC, Layered
**Bom:** Feature-Driven, Vertical Slice
**Médio:** Clean, Hexagonal
**Difícil:** DDD, CQRS, Event-Driven, Onion

### Fácil de Testar
**Melhor:** Hexagonal, Onion, Clean
**Bom:** Vertical Slice, CQRS, DDD
**Médio:** Layered, Feature-Driven, Event-Driven
**Difícil:** MVC

---

## Exemplo de Uso

### Startup MVP
✅ MVC, Layered

### E-commerce
✅ Clean, Feature-Driven, Vertical Slice

### Banking System
✅ DDD, Hexagonal, Event-Driven

### Social Network
✅ Event-Driven, CQRS, Vertical Slice

### CRM
✅ Clean, Feature-Driven, Layered

### Microserviços
✅ Hexagonal, Clean, Vertical Slice

### Monolito Modular
✅ Feature-Driven, Vertical Slice, Clean
