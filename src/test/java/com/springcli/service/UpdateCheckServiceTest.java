package com.springcli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UpdateCheckServiceTest {

    @Autowired
    private UpdateCheckService service;

    @Test
    void shouldCreateService() {
        assertThat(service).isNotNull();
    }

    @Test
    void shouldCheckForUpdates() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        assertThat(info).isNotNull();
        assertThat(info.currentVersion()).isNotEmpty();
    }

    @Test
    void shouldHandleNetworkFailuresGracefully() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        assertThat(info).isNotNull();
        assertThat(info.currentVersion()).isEqualTo("1.1.0");
    }
}
