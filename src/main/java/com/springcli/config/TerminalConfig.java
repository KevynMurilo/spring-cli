package com.springcli.config;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class TerminalConfig {

    @Bean
    public Terminal terminal() throws IOException {
        return TerminalBuilder.builder()
                .system(true)
                .encoding(System.getProperty("file.encoding", "UTF-8"))
                .dumb(false)
                .build();
    }
}
