# Guia de Contribuição - Spring CLI

Obrigado por contribuir com o Spring CLI! Este guia explica como adicionar novas dependências e features ao sistema.

## Índice

1. [Visão Geral da Arquitetura](#visão-geral-da-arquitetura)
2. [Como Adicionar uma Nova Dependência](#como-adicionar-uma-nova-dependência)
3. [Schema do JSON](#schema-do-json)
4. [Regras de Negócio](#regras-de-negócio)
5. [Exemplos Práticos](#exemplos-práticos)
6. [Testando suas Mudanças](#testando-suas-mudanças)
7. [Melhores Práticas](#melhores-práticas)

## Visão Geral da Arquitetura

O Spring CLI utiliza um sistema **100% declarativo baseado em JSON** para gerenciar dependências. Não há lógica hardcoded de if/else nos serviços.

### Fluxo de Configuração

```
dependency-rules.json (fonte única de verdade)
        ↓
DependencyRulesService (carrega e cacheia regras)
        ↓
┌────────────────────────────────────────┐
│ Services que LEEM do JSON:             │
│ • DependencyConfigurationRegistry      │ → application.yml
│ • PomManipulationService               │ → pom.xml
│ • GradleManipulationService            │ → build.gradle
│ • DockerComposeGeneratorService        │ → docker-compose.yml
│ • ScaffoldingGeneratorService          │ → código Java
└────────────────────────────────────────┘
```

**Importante**: Os serviços NUNCA devem ter lógica específica de dependências. Tudo vem do JSON.

## Como Adicionar uma Nova Dependência

### Passo 1: Editar o `dependency-rules.json`

Adicione sua regra ao arquivo `src/main/resources/dependency-rules.json`:

```json
{
  "id": "sua-dependencia",
  "category": "TOOL",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [ ... ],
      "plugins": [ ... ],
      "exclusions": [ ... ]
    },
    "gradle": {
      "implementation": [ ... ],
      "compileOnly": [ ... ],
      "runtimeOnly": [ ... ],
      "annotationProcessor": [ ... ],
      "compilerOptions": [ ... ]
    }
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

### Passo 2: Se for uma Feature, Mapeie o ID

Se sua dependência é ativada por uma feature (como JWT ou Swagger), adicione o mapeamento em:

**`PomManipulationService.java`** e **`GradleManipulationService.java`**:

```java
private List<String> getActiveFeaturesAsDependencyIds(ProjectFeatures features) {
    List<String> dependencies = new ArrayList<>();

    if (features.enableJwt()) {
        dependencies.add("jwt");
    }
    if (features.enableSwagger()) {
        dependencies.add("swagger");
    }
    // ADICIONE AQUI:
    if (features.enableSuaNovaDependencia()) {
        dependencies.add("sua-dependencia");
    }

    return dependencies;
}
```

### Passo 3: Compile e Teste

```bash
mvn clean compile
mvn test
```

## Schema do JSON

### Estrutura Completa

```json
{
  "id": "string (OBRIGATÓRIO - identificador único)",
  "category": "string (OBRIGATÓRIO - DATA, SECURITY, TOOL, IO, OBSERVABILITY)",
  "priority": "integer (OBRIGATÓRIO - 0 a 10, onde 10 = mais alta prioridade)",

  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "string (OBRIGATÓRIO)",
          "artifactId": "string (OBRIGATÓRIO)",
          "version": "string (OPCIONAL - omitir se gerenciado pelo BOM)",
          "scope": "string (OPCIONAL - compile, runtime, provided, test)"
        }
      ],
      "plugins": [
        {
          "groupId": "string (OBRIGATÓRIO)",
          "artifactId": "string (OBRIGATÓRIO)",
          "executionGoal": "string (OPCIONAL - compile, test, etc.)"
        }
      ],
      "exclusions": [
        {
          "groupId": "string (OBRIGATÓRIO)",
          "artifactId": "string (OBRIGATÓRIO)"
        }
      ]
    },
    "gradle": {
      "implementation": ["string - formato: groupId:artifactId:version"],
      "compileOnly": ["string"],
      "runtimeOnly": ["string"],
      "annotationProcessor": ["string"],
      "compilerOptions": ["string - flags do compilador como -Amapstruct..."],
      "plugins": ["string - ID do plugin Gradle"]
    }
  },

  "runtime": {
    "properties": [
      {
        "key": "string (OBRIGATÓRIO - chave da propriedade Spring)",
        "value": "string (OBRIGATÓRIO - valor padrão)",
        "comment": "string (OPCIONAL - comentário explicativo)"
      }
    ]
  },

  "infrastructure": {
    "dockerCompose": {
      "serviceName": "string (OBRIGATÓRIO - nome do serviço no docker-compose)",
      "image": "string (OBRIGATÓRIO - imagem Docker com tag)",
      "ports": ["string - formato: host:container"],
      "environment": {
        "KEY": "value"
      },
      "volumes": ["string - formato: volume:mountpoint"],
      "depends_on": ["string - nome de outro serviço"],
      "healthcheck": {
        "test": ["CMD", "comando", "args"],
        "interval": "string (ex: 10s)",
        "timeout": "string (ex: 5s)",
        "retries": "integer"
      }
    }
  },

  "scaffolding": {
    "files": [
      {
        "path": "string (OBRIGATÓRIO - caminho do arquivo com {{basePackage}})",
        "content": "string (OBRIGATÓRIO - conteúdo do arquivo com suporte a {{basePackage}})"
      }
    ]
  }
}
```

### Campos Nulos vs Arrays Vazios

- Use `null` quando a seção inteira não é aplicável (ex: Docker Compose para Lombok)
- Use `[]` (array vazio) quando a seção existe mas não tem items (ex: plugins Maven para PostgreSQL)

## Regras de Negócio

### 1. Prioridades

Dependências com annotation processors devem ter prioridades específicas:

| Dependência | Priority | Motivo |
|------------|----------|--------|
| Lombok | 10 | Deve processar primeiro |
| MapStruct | 5 | Depende do Lombok |
| Outras | 0 | Padrão |

### 2. Bancos de Dados

**URLs de Conexão Docker**:

Para bancos de dados, sempre use o nome do serviço Docker como hostname:

```json
{
  "key": "spring.datasource.url",
  "value": "jdbc:postgresql://postgres:5432/mydb"
}
```

**Não use** `localhost` - isso quebrará quando rodar no Docker!

### 3. Infrastructure

**Kafka**: Sempre crie DOIS serviços no JSON:
- `kafka-zookeeper` (id separado)
- `kafka` (com `depends_on: ["zookeeper"]`)

**Healthchecks**: Sempre adicione healthchecks para containers de infraestrutura.

### 4. Scaffolding

**Template Variables**:
- `{{basePackage}}`: Será substituído pelo package base (ex: `com.example.app`)

**SecurityConfig**: Para Spring Security, sempre gere um `SecurityConfig.java` com `permitAll()` para evitar lockout do desenvolvedor.

**Flyway**: Crie a pasta `src/main/resources/db/migration` com um `.gitkeep` ou migration inicial.

### 5. Scopes Maven

| Scope | Quando Usar |
|-------|------------|
| `compile` (padrão) | Disponível em compile e runtime |
| `provided` | Fornecido pelo container (ex: Lombok) |
| `runtime` | Necessário apenas em runtime (ex: drivers JDBC) |
| `test` | Apenas para testes |

### 6. Gradle Configurations

| Configuration | Equivalente Maven | Quando Usar |
|--------------|------------------|------------|
| `implementation` | `compile` | Dependência normal |
| `compileOnly` | `provided` | Disponível em compile mas não em runtime |
| `runtimeOnly` | `runtime` | Disponível apenas em runtime |
| `annotationProcessor` | N/A | Processadores de anotação (Lombok, MapStruct) |

## Exemplos Práticos

### Exemplo 1: Dependência Simples (Biblioteca)

```json
{
  "id": "commons-lang3",
  "category": "TOOL",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "org.apache.commons",
          "artifactId": "commons-lang3",
          "version": "3.14.0"
        }
      ],
      "plugins": [],
      "exclusions": []
    },
    "gradle": {
      "implementation": ["org.apache.commons:commons-lang3:3.14.0"],
      "compileOnly": [],
      "runtimeOnly": [],
      "annotationProcessor": [],
      "compilerOptions": []
    }
  },
  "runtime": {
    "properties": []
  },
  "infrastructure": {
    "dockerCompose": null
  },
  "scaffolding": {
    "files": []
  }
}
```

### Exemplo 2: Banco de Dados com Docker

```json
{
  "id": "mariadb",
  "category": "DATA",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "org.springframework.boot",
          "artifactId": "spring-boot-starter-data-jpa"
        },
        {
          "groupId": "org.mariadb.jdbc",
          "artifactId": "mariadb-java-client",
          "scope": "runtime"
        }
      ],
      "plugins": [],
      "exclusions": []
    },
    "gradle": {
      "implementation": ["org.springframework.boot:spring-boot-starter-data-jpa"],
      "compileOnly": [],
      "runtimeOnly": ["org.mariadb.jdbc:mariadb-java-client"],
      "annotationProcessor": [],
      "compilerOptions": []
    }
  },
  "runtime": {
    "properties": [
      {
        "key": "spring.datasource.url",
        "value": "jdbc:mariadb://mariadb:3306/mydb"
      },
      {
        "key": "spring.datasource.username",
        "value": "root"
      },
      {
        "key": "spring.datasource.password",
        "value": "root"
      },
      {
        "key": "spring.datasource.driver-class-name",
        "value": "org.mariadb.jdbc.Driver"
      },
      {
        "key": "spring.jpa.database-platform",
        "value": "org.hibernate.dialect.MariaDBDialect"
      }
    ]
  },
  "infrastructure": {
    "dockerCompose": {
      "serviceName": "mariadb",
      "image": "mariadb:11.2",
      "ports": ["3306:3306"],
      "environment": {
        "MARIADB_DATABASE": "mydb",
        "MARIADB_ROOT_PASSWORD": "root"
      },
      "volumes": ["mariadb_data:/var/lib/mysql"],
      "healthcheck": {
        "test": ["CMD", "healthcheck.sh", "--connect", "--innodb_initialized"],
        "interval": "10s",
        "timeout": "5s",
        "retries": 5
      }
    }
  },
  "scaffolding": {
    "files": [
      {
        "path": "src/main/java/{{basePackage}}/entity/package-info.java",
        "content": "package {{basePackage}}.entity;"
      }
    ]
  }
}
```

### Exemplo 3: Feature com Scaffolding

```json
{
  "id": "graphql",
  "category": "IO",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "org.springframework.boot",
          "artifactId": "spring-boot-starter-graphql"
        }
      ],
      "plugins": [],
      "exclusions": []
    },
    "gradle": {
      "implementation": ["org.springframework.boot:spring-boot-starter-graphql"],
      "compileOnly": [],
      "runtimeOnly": [],
      "annotationProcessor": [],
      "compilerOptions": []
    }
  },
  "runtime": {
    "properties": [
      {
        "key": "spring.graphql.graphiql.enabled",
        "value": "true"
      },
      {
        "key": "spring.graphql.graphiql.path",
        "value": "/graphiql"
      }
    ]
  },
  "infrastructure": {
    "dockerCompose": null
  },
  "scaffolding": {
    "files": [
      {
        "path": "src/main/resources/graphql/schema.graphqls",
        "content": "type Query {\n    hello: String\n}\n"
      },
      {
        "path": "src/main/java/{{basePackage}}/graphql/QueryResolver.java",
        "content": "package {{basePackage}}.graphql;\n\nimport org.springframework.graphql.data.method.annotation.QueryMapping;\nimport org.springframework.stereotype.Controller;\n\n@Controller\npublic class QueryResolver {\n\n    @QueryMapping\n    public String hello() {\n        return \"Hello from GraphQL!\";\n    }\n}\n"
      }
    ]
  }
}
```

## Testando suas Mudanças

### 1. Validação do JSON

Antes de commitar, valide o JSON:

```bash
cat src/main/resources/dependency-rules.json | jq . > /dev/null
```

Se houver erro de sintaxe, o `jq` apontará a linha exata.

### 2. Compilação

```bash
mvn clean compile
```

### 3. Testes Unitários

```bash
mvn test
```

### 4. Teste de Integração

Gere um projeto real usando sua nova dependência:

```bash
mvn clean package -DskipTests
./target/spring-cli
```

No CLI interativo, selecione sua nova dependência e gere um projeto.

### 5. Verificações

Depois de gerar o projeto:

1. **Build**: O pom.xml/build.gradle tem as dependências corretas?
2. **Runtime**: O application.yml tem as propriedades corretas?
3. **Infrastructure**: O docker-compose.yml foi gerado (se aplicável)?
4. **Scaffolding**: Os arquivos Java foram criados corretamente?
5. **Compile**: O projeto gerado compila sem erros?

```bash
cd projeto-gerado
mvn clean compile  # ou ./gradlew build
```

## Melhores Práticas

### ✅ FAÇA

1. **Sempre use versões explícitas** para bibliotecas que não estão no Spring Boot BOM
2. **Adicione healthchecks** para todos os containers Docker
3. **Use nomes de serviço Docker** nas URLs de conexão
4. **Documente propriedades** usando o campo `comment`
5. **Teste com Maven E Gradle** - garanta que ambas funcionem
6. **Use templates `{{basePackage}}`** em scaffolding
7. **Ordene propriedades logicamente** (URL primeiro, credenciais depois, configs avançadas por último)
8. **Crie package-info.java** para novos pacotes no scaffolding

### ❌ NÃO FAÇA

1. **Não adicione lógica específica** nos serviços Java - tudo deve vir do JSON
2. **Não use `localhost`** em propriedades de banco de dados
3. **Não omita o campo `category`** - ele pode ser usado para agrupamento futuro
4. **Não crie dependências circulares** no Docker Compose
5. **Não use versões SNAPSHOT** - apenas releases estáveis
6. **Não adicione dependências desnecessárias** - seja minimalista
7. **Não quebre compatibilidade** com versões antigas do Spring Boot sem documentar

### 🎯 Dicas de Performance

1. **Prioridades**: Use apenas quando realmente necessário (annotation processors)
2. **Exclusions**: Use para evitar conflitos de versão
3. **Scopes**: Use `provided` ou `runtime` sempre que possível para reduzir o classpath de compile

### 📝 Documentação

Quando adicionar uma dependência significativa:

1. Atualize o `README.md` com a nova dependência na lista
2. Se for uma feature complexa, adicione um exemplo no `ARCHITECTURE.md`
3. Se mudar o schema do JSON, atualize este `CONTRIBUTING.md`

## Estrutura de Commit

Ao fazer commit de novas dependências:

```
feat(deps): add MariaDB support

- Add MariaDB dependency rule to dependency-rules.json
- Include Docker Compose configuration with healthcheck
- Add connection properties pointing to Docker service
- Generate entity package scaffold

Closes #123
```

## Perguntas Frequentes

### Q: Preciso adicionar código Java quando adiciono uma dependência?

**R**: Apenas se a dependência requer scaffolding (código inicial). Caso contrário, apenas o JSON é suficiente.

### Q: Como adiciono suporte a um plugin Maven complexo?

**R**: Use o campo `executionGoal` em `maven.plugins`. Para configurações XML complexas, considere criar um template.

### Q: Posso ter múltiplas versões da mesma dependência?

**R**: Não diretamente. O `id` deve ser único. Se precisar de variantes, use IDs diferentes (ex: `postgresql-14`, `postgresql-15`).

### Q: Como testo apenas minha dependência sem gerar um projeto completo?

**R**: Crie um teste unitário em `DependencyRulesServiceTest.java` que valide sua regra.

### Q: O que fazer se minha dependência conflita com outra?

**R**: Use o campo `exclusions` no Maven ou mecanismos de exclude no Gradle.

## Suporte

Dúvidas? Abra uma issue:
https://github.com/spring-cli/issues

---

**Obrigado por contribuir!** 🚀
