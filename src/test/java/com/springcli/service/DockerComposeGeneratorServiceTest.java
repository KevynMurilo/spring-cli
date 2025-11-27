package com.springcli.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DockerComposeGeneratorServiceTest {

    @Autowired
    private DockerComposeGeneratorService service;

    @Test
    void shouldGenerateDockerComposeWithPostgresql() {
        Set<String> dependencies = Set.of("postgresql");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).isNotNull();
        assertThat(dockerCompose).contains("version: '3.8'");
        assertThat(dockerCompose).contains("postgres:");
        assertThat(dockerCompose).contains("image: postgres:16-alpine");
        assertThat(dockerCompose).contains("5432:5432");
    }

    @Test
    void shouldGenerateDockerComposeWithMultipleServices() {
        Set<String> dependencies = Set.of("postgresql", "redis", "mongodb");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("postgres:");
        assertThat(dockerCompose).contains("redis:");
        assertThat(dockerCompose).contains("mongo:");
    }

    @Test
    void shouldGenerateVolumes() {
        Set<String> dependencies = Set.of("postgresql", "mongodb");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("volumes:");
        assertThat(dockerCompose).contains("postgres_data:");
        assertThat(dockerCompose).contains("mongo_data:");
    }

    @Test
    void shouldHandleKafkaWithZookeeper() {
        Set<String> dependencies = Set.of("kafka", "kafka-zookeeper");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("zookeeper:");
        assertThat(dockerCompose).contains("kafka:");
        assertThat(dockerCompose).contains("depends_on:");
    }

    @Test
    void shouldReturnNullForNonInfraDependencies() {
        Set<String> dependencies = Set.of("lombok", "mapstruct");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).isNull();
    }

    @Test
    void shouldIncludeHealthchecks() {
        Set<String> dependencies = Set.of("postgresql");

        String dockerCompose = service.generateDockerCompose(dependencies);

        assertThat(dockerCompose).contains("healthcheck:");
        assertThat(dockerCompose).contains("test:");
        assertThat(dockerCompose).contains("interval:");
        assertThat(dockerCompose).contains("retries:");
    }
}
