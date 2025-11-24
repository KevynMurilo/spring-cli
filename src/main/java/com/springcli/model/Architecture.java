package com.springcli.model;

import java.util.*;

public enum Architecture {

    MVC(define("Model-View-Controller")
            .layer("model", "model")
            .layer("dto", "dto")
            .layer("mapper", "mapper")
            .layer("repository", "repository")
            .layer("service", "service")
            .layer("controller", "controller")
            .layer("config", "config")
            .layer("security", "security")

            .addFile("model", "common/Entity", ".java")
            .addFile("dto", "common/DTO", "DTO.java")
            .addFile("mapper", "common/Mapper", "Mapper.java")
            .addFile("repository", "common/Repository", "Repository.java")
            .addFile("service", "common/Service", "Service.java")
            .addFile("controller", "common/Controller", "Controller.java")
    ),

    LAYERED(define("Layered Architecture")
            .layer("model", "database/model")
            .layer("dto", "presentation/dto")
            .layer("mapper", "business/mapper")
            .layer("repository", "persistence/repository")
            .layer("service", "business/service")
            .layer("controller", "presentation/controller")
            .layer("config", "presentation/config")
            .layer("security", "security")

            .addFile("model", "common/Entity", ".java")
            .addFile("dto", "common/DTO", "DTO.java")
            .addFile("mapper", "common/Mapper", "Mapper.java")
            .addFile("repository", "common/Repository", "Repository.java")
            .addFile("service", "common/Service", "Service.java")
            .addFile("controller", "common/Controller", "Controller.java")
    ),

    CLEAN(define("Clean Architecture")
            .layer("model", "domain/model")
            .layer("usecase", "application/usecase")
            .layer("port-out", "domain/repository")
            .layer("repository-impl", "infrastructure/persistence")
            .layer("controller", "infrastructure/controller")
            .layer("config", "infrastructure/config")
            .layer("security", "infrastructure/security")

            .addFile("model", "clean/DomainModel", ".java")
            .addFile("port-out", "clean/RepositoryInterface", "Repository.java")
            .addFile("usecase", "clean/UseCase", "UseCase.java")
            .addFile("controller", "clean/InfrastructureController", "Controller.java")
            .addFile("repository-impl", "clean/JpaEntity", "Entity.java")
            .addFile("repository-impl", "clean/JpaRepository", "JpaRepository.java")
            .addFile("repository-impl", "clean/RepositoryImpl", "RepositoryImpl.java")
    ),

    HEXAGONAL(define("Hexagonal (Ports & Adapters)")
            .layer("model", "domain/model")
            .layer("port-in", "application/port/in")
            .layer("port-out", "application/port/out")
            .layer("service", "application/service")
            .layer("adapter-web", "adapter/in/web")
            .layer("adapter-db", "adapter/out/persistence")
            .layer("config", "adapter/config")
            .layer("security", "adapter/security")

            .addFile("model", "hexagonal/DomainModel", ".java")
            .addFile("port-in", "hexagonal/InputPort", "InputPort.java")
            .addFile("port-out", "hexagonal/OutputPort", "OutputPort.java")
            .addFile("service", "hexagonal/Service", "Service.java")
            .addFile("adapter-web", "hexagonal/InAdapter", "Controller.java")
            .addFile("adapter-db", "hexagonal/OutAdapter", "PersistenceAdapter.java")
            .addFile("adapter-db", "hexagonal/JpaEntity", "Entity.java")
            .addFile("adapter-db", "hexagonal/JpaRepository", "JpaRepository.java")
    ),

    FEATURE_DRIVEN(define("Feature-Driven")
            .layer("model", "features/{feature}/model")
            .layer("repository", "features/{feature}/repository")
            .layer("service", "features/{feature}/service")
            .layer("controller", "features/{feature}/controller")
            .layer("config", "shared/config")
            .layer("security", "shared/security")

            .addFile("model", "common/Entity", ".java")
            .addFile("repository", "common/Repository", "Repository.java")
            .addFile("service", "common/Service", "Service.java")
            .addFile("controller", "common/Controller", "Controller.java")
    ),

    DDD(define("Domain-Driven Design")
            .layer("model", "domain/entities")
            .layer("aggregate", "domain/aggregates")
            .layer("valueobject", "domain/valueobjects")
            .layer("event", "domain/events")
            .layer("factory", "domain/factories")
            .layer("repository", "domain/repositories")
            .layer("service", "domain/services")
            .layer("dto", "application/dto")
            .layer("repository-impl", "infrastructure/persistence")
            .layer("controller", "infrastructure/web")
            .layer("config", "infrastructure/config")
            .layer("security", "infrastructure/security")

            .addFile("aggregate", "ddd/Aggregate", "Aggregate.java")
            .addFile("model", "ddd/Entity", "Entity.java")
            .addFile("valueobject", "ddd/ValueObject", "Id.java")
            .addFile("event", "ddd/DomainEvent", "CreatedEvent.java")
            .addFile("factory", "ddd/Factory", "Factory.java")
            .addFile("service", "ddd/DomainService", "DomainService.java")
            .addFile("repository", "ddd/Repository", "Repository.java")
            .addFile("dto", "common/DTO", "DTO.java")
            .addFile("controller", "ddd/Controller", "Controller.java")
            .addFile("repository-impl", "ddd/RepositoryImpl", "RepositoryImpl.java")
            .addFile("repository-impl", "ddd/JpaEntity", "JpaEntity.java")
            .addFile("repository-impl", "ddd/JpaRepository", "JpaRepository.java")
    ),

    CQRS(define("CQRS")
            .layer("model", "domain/model")
            .layer("dto", "application/dto")
            .layer("command", "application/commands")
            .layer("query", "application/queries")
            .layer("command-handler", "application/handlers")
            .layer("query-handler", "application/handlers")
            .layer("repository", "infrastructure/persistence")
            .layer("controller", "infrastructure/web")
            .layer("config", "shared/config")
            .layer("security", "shared/security")

            .addFile("command", "cqrs/Command", "Command.java")
            .addFile("query", "cqrs/Query", "Query.java")
            .addFile("command-handler", "cqrs/CommandHandler", "CommandHandler.java")
            .addFile("query-handler", "cqrs/QueryHandler", "QueryHandler.java")
            .addFile("model", "common/Entity", ".java")
            .addFile("dto", "common/DTO", "DTO.java")
            .addFile("repository", "common/Repository", "Repository.java")
            .addFile("controller", "cqrs/Controller", "Controller.java")
    ),

    EVENT_DRIVEN(define("Event-Driven")
            .layer("model", "domain/model")
            .layer("event", "domain/events")
            .layer("repository", "infrastructure/persistence")
            .layer("service", "application/services")
            .layer("handler", "application/handlers")
            .layer("publisher", "infrastructure/messaging")
            .layer("controller", "infrastructure/web")
            .layer("config", "shared/config")
            .layer("security", "shared/security")

            .addFile("model", "common/Entity", ".java")
            .addFile("repository", "common/Repository", "Repository.java")
            .addFile("event", "event-driven/Event", "CreatedEvent.java")
            .addFile("handler", "event-driven/EventHandler", "EventHandler.java")
            .addFile("publisher", "event-driven/EventPublisher", "EventPublisher.java")
            .addFile("service", "event-driven/Service", "Service.java")
            .addFile("controller", "event-driven/Controller", "Controller.java")
    ),

    ONION(define("Onion Architecture")
            .layer("model", "core/domain")
            .layer("service", "core/services")
            .layer("repository", "infrastructure/persistence")
            .layer("controller", "infrastructure/web")
            .layer("config", "infrastructure/config")
            .layer("security", "infrastructure/security")

            .addFile("model", "common/Entity", ".java")
            .addFile("service", "common/Service", "Service.java")
            .addFile("repository", "common/Repository", "Repository.java")
            .addFile("controller", "common/Controller", "Controller.java")
    ),

    VERTICAL_SLICE(define("Vertical Slice")
            .layer("feature", "features/{feature}")
            .layer("model", "features/{feature}")
            .layer("repository", "features/{feature}")
            .layer("controller", "features/{feature}")
            .layer("config", "shared/config")
            .layer("security", "shared/security")

            .addFile("model", "common/Entity", ".java")
            .addFile("repository", "common/Repository", "Repository.java")
            .addFile("feature", "common/Service", "Service.java")
            .addFile("controller", "common/Controller", "Controller.java")
    );

    private final String displayName;
    private final Map<String, String> layerMappings;
    private final List<ArchitectureBlueprint> blueprints;

    Architecture(Builder builder) {
        this.displayName = builder.displayName;
        this.layerMappings = builder.mappings;
        this.blueprints = builder.blueprints;
    }

    public String getPathForLayer(String layer) {
        return layerMappings.getOrDefault(layer, layer).replace("/", ".");
    }

    public Map<String, String> getLayerMappings() {
        return layerMappings;
    }

    public List<ArchitectureBlueprint> getBlueprints() {
        return blueprints;
    }

    public String getDisplayName() { return displayName; }

    private static Builder define(String name) { return new Builder(name); }

    private static class Builder {
        String displayName;
        Map<String, String> mappings = new HashMap<>();
        List<ArchitectureBlueprint> blueprints = new ArrayList<>();

        public Builder(String name) { this.displayName = name; }

        public Builder layer(String logicalName, String physicalPath) {
            this.mappings.put(logicalName, physicalPath);
            return this;
        }

        public Builder addFile(String layer, String template, String suffix) {
            this.blueprints.add(new ArchitectureBlueprint(layer, template, suffix));
            return this;
        }
    }
}