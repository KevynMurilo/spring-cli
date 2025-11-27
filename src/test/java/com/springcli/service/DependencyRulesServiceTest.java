package com.springcli.service;

import com.springcli.model.rules.DependencyRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DependencyRulesServiceTest {

    @Autowired
    private DependencyRulesService service;

    @Test
    void shouldLoadRulesFromJson() {
        List<DependencyRule> allRules = service.getAllRules();
        assertThat(allRules).isNotEmpty();
        assertThat(allRules.size()).isGreaterThan(15);
    }

    @Test
    void shouldGetRuleById() {
        Optional<DependencyRule> rule = service.getRule("lombok");
        assertThat(rule).isPresent();
        assertThat(rule.get().id()).isEqualTo("lombok");
        assertThat(rule.get().priority()).isEqualTo(10);
        assertThat(rule.get().category()).isEqualTo("TOOL");
    }

    @Test
    void shouldRespectPriorities() {
        List<DependencyRule> rules = service.getRules(List.of("lombok", "mapstruct", "postgresql"));

        assertThat(rules).hasSize(3);
        assertThat(rules.get(0).id()).isEqualTo("lombok");
        assertThat(rules.get(1).id()).isEqualTo("mapstruct");
        assertThat(rules.get(2).id()).isEqualTo("postgresql");
    }

    @Test
    void shouldReturnEmptyForNonExistentRule() {
        Optional<DependencyRule> rule = service.getRule("non-existent-dep");
        assertThat(rule).isEmpty();
    }

    @Test
    void shouldHavePostgresqlWithDocker() {
        Optional<DependencyRule> rule = service.getRule("postgresql");
        assertThat(rule).isPresent();
        assertThat(rule.get().infrastructure()).isNotNull();
        assertThat(rule.get().infrastructure().dockerCompose()).isNotNull();
        assertThat(rule.get().infrastructure().dockerCompose().serviceName()).isEqualTo("postgres");
    }

    @Test
    void shouldHaveMapstructWithCompilerOptions() {
        Optional<DependencyRule> rule = service.getRule("mapstruct");
        assertThat(rule).isPresent();
        assertThat(rule.get().build().gradle().compilerOptions())
            .contains("-Amapstruct.defaultComponentModel=spring");
    }

    @Test
    void shouldHaveSecurityWithScaffolding() {
        Optional<DependencyRule> rule = service.getRule("security");
        assertThat(rule).isPresent();
        assertThat(rule.get().scaffolding()).isNotNull();
        assertThat(rule.get().scaffolding().files()).isNotEmpty();
    }

    @Test
    void shouldHaveKafkaWithZookeeper() {
        assertThat(service.hasRule("kafka")).isTrue();
        assertThat(service.hasRule("kafka-zookeeper")).isTrue();

        Optional<DependencyRule> kafka = service.getRule("kafka");
        assertThat(kafka).isPresent();
        assertThat(kafka.get().infrastructure().dockerCompose().depends_on())
            .contains("zookeeper");
    }
}
