package com.springcli.config;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

@Configuration
public class TerminalConfig {

    @Bean
    @Lazy
    public Terminal terminal() throws IOException {
        return TerminalBuilder.builder()
                .name("SpringCLI")
                .system(true)
                .jna(true)
                .dumb(true)
                .encoding("UTF-8")
                .build();
    }
}