# Spring CLI - Arquitetura Refatorada

## Visão Geral

O Spring CLI foi completamente refatorado para utilizar um sistema baseado em JSON para configuração de dependências, eliminando toda a lógica hardcoded (if/else) que estava espalhada pelo código.

## Arquitetura Baseada em Regras

### dependency-rules.json

Localizado em `src/main/resources/dependency-rules.json`, este arquivo é o coração do sistema. Ele define todas as regras de configuração para cada dependência suportada.

#### Estrutura de uma Regra

```json
{
  "id": "postgresql",
  "category": "DATA",
  "priority": 0,
  "build": {
    "maven": { ... },
    "gradle": { ... }
  },
  "runtime": {
    "properties": [ ... ]
  },
  "infrastructure": {
    "dockerCompose": { ... }
  },
  "scaffolding": {
    "files": [ ... ]
  }
}
```

### Componentes Principais

#### 1. Modelos de Dados (`com.springcli.model.rules`)

- **DependencyRule**: Modelo raiz que representa uma regra completa
- **BuildConfig**: Configurações de build (Maven/Gradle)
- **RuntimeConfig**: Propriedades do application.yml
- **InfrastructureConfig**: Configuração de infraestrutura (Docker Compose)
- **ScaffoldingConfig**: Arquivos de código a serem gerados

#### 2. DependencyRulesService

Responsável por:
- Carregar o `dependency-rules.json` na inicialização
- Fornecer acesso às regras por ID
- Ordenar regras por prioridade

```java
@Service
public class DependencyRulesService {
    public Optional<DependencyRule> getRule(String dependencyId);
    public List<DependencyRule> getRules(List<String> dependencyIds);
}
```

#### 3. DependencyConfigurationRegistry

Refatorado para usar o `DependencyRulesService` ao invés de lógica hardcoded:

**Antes:**
```java
configurations.put("postgresql", DependencyConfiguration.builder("postgresql")
    .requiredProperties(Map.of(
        "spring.datasource.url", "jdbc:postgresql://localhost:5432/${spring.application.name}",
        "spring.datasource.username", "postgres",
        "spring.datasource.password", "postgres"
    ))
    .build());
```

**Depois:**
```java
public Optional<DependencyConfiguration> getConfiguration(String dependencyId) {
    return rulesService.getRule(dependencyId)
        .map(rule -> {
            Map<String, String> properties = rule.runtime().properties().stream()
                .collect(Collectors.toMap(p -> p.key(), p -> p.value()));
            return DependencyConfiguration.builder(dependencyId)
                .requiredProperties(properties)
                .build();
        });
}
```

#### 4. PomManipulationService

Refatorado para injetar dependências Maven diretamente do JSON:

```java
private String injectFeatureDependencies(String pomContent, ProjectFeatures features, LibraryVersions versions) {
    StringBuilder injections = new StringBuilder();

    if (features.enableMapStruct()) {
        configRegistry.getRule("mapstruct").ifPresent(rule -> {
            injections.append(generateMavenDependenciesXml(rule.build().maven().dependencies()));
        });
    }

    return pomContent.substring(0, lastDependenciesEnd) + injections + pomContent.substring(lastDependenciesEnd);
}
```

#### 5. GradleManipulationService

Mesmo padrão aplicado ao Gradle:

```java
private String generateGradleDependencies(GradleConfig gradle) {
    StringBuilder deps = new StringBuilder();

    gradle.implementation().forEach(dep ->
        deps.append("    implementation \"").append(dep).append("\"\n")
    );

    gradle.compileOnly().forEach(dep ->
        deps.append("    compileOnly \"").append(dep).append("\"\n")
    );

    return deps.toString();
}
```

#### 6. DockerComposeGeneratorService

Novo serviço que gera `docker-compose.yml` completamente a partir do JSON:

```java
public String generateDockerCompose(Set<String> dependencies) {
    List<DockerComposeConfig> services = rules.stream()
        .filter(rule -> rule.infrastructure() != null)
        .filter(rule -> rule.infrastructure().dockerCompose() != null)
        .map(rule -> rule.infrastructure().dockerCompose())
        .collect(Collectors.toList());

    // Gera YAML completo
}
```

#### 7. ScaffoldingGeneratorService

Novo serviço que gera arquivos de código a partir das regras:

```java
public Map<String, String> generateScaffoldingFiles(Set<String> dependencies, String basePackage, Path projectPath) {
    List<DependencyRule> rules = configRegistry.getRules(new ArrayList<>(dependencies));

    Map<String, String> filesToGenerate = new HashMap<>();

    for (DependencyRule rule : rules) {
        for (ScaffoldingFile file : rule.scaffolding().files()) {
            String resolvedPath = resolvePath(file.path(), basePackage, projectPath);
            String resolvedContent = resolveContent(file.content(), basePackage);
            filesToGenerate.put(resolvedPath, resolvedContent);
        }
    }

    return filesToGenerate;
}
```

## Prioridades de Dependências

O sistema suporta prioridades para garantir ordem de processamento:

- **Lombok**: priority 10 (processado primeiro)
- **MapStruct**: priority 5 (processado após Lombok)
- **Outros**: priority 0

Isso é crucial para annotation processors que têm dependências entre si.

## Fluxo de Geração de Projeto

```
1. Usuário seleciona dependências
        ↓
2. DependencyRulesService carrega regras do JSON
        ↓
3. Regras são ordenadas por prioridade
        ↓
4. PomManipulationService/GradleManipulationService injetam dependências
        ↓
5. DependencyConfigurationRegistry injeta propriedades no application.yml
        ↓
6. DockerComposeGeneratorService gera docker-compose.yml
        ↓
7. ScaffoldingGeneratorService gera arquivos de código
        ↓
8. Projeto completo é gerado
```

## Vantagens da Nova Arquitetura

1. **Zero Lógica Hardcoded**: Toda configuração está no JSON
2. **Facilidade de Manutenção**: Adicionar novas dependências é apenas editar o JSON
3. **Separação de Responsabilidades**: Cada serviço tem uma única função
4. **Testabilidade**: Fácil mockar o DependencyRulesService
5. **Extensibilidade**: Suporte fácil para novos tipos de configuração
6. **Versionamento**: Configurações podem ser versionadas junto com o código

## Adicionando uma Nova Dependência

Para adicionar suporte a uma nova dependência, basta adicionar uma entrada no `dependency-rules.json`:

```json
{
  "id": "nova-dependencia",
  "category": "TOOL",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "com.example",
          "artifactId": "nova-lib",
          "version": "1.0.0"
        }
      ]
    },
    "gradle": {
      "implementation": ["com.example:nova-lib:1.0.0"]
    }
  },
  "runtime": {
    "properties": [
      {
        "key": "app.nova.enabled",
        "value": "true"
      }
    ]
  },
  "infrastructure": {
    "dockerCompose": null
  },
  "scaffolding": {
    "files": []
  }
}
```

## Dependências Suportadas

- lombok
- mapstruct
- postgresql
- mysql
- h2
- mongodb
- redis
- flyway
- security
- web
- actuator
- kafka (com zookeeper)
- zipkin
- graalvm

## Estrutura de Pacotes

```
com.springcli
├── model
│   └── rules              (Modelos de dados do JSON)
├── service
│   ├── config             (Serviços de configuração)
│   ├── DependencyRulesService
│   ├── DockerComposeGeneratorService
│   ├── ScaffoldingGeneratorService
│   ├── PomManipulationService
│   └── GradleManipulationService
└── resources
    └── dependency-rules.json
```

## Conclusão

A refatoração eliminou aproximadamente 500+ linhas de código hardcoded, substituindo por uma solução declarativa baseada em JSON que é mais fácil de manter, testar e estender.
