package com.springcli.config;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandHandlingResult;

@Configuration
public class ExceptionConfig {

    @Bean
    public CommandExceptionResolver customExceptionResolver(Terminal terminal) {
        return (exception) -> {
            String errorMessage = new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                    .append("\n⚠️  Erro: ")
                    .append(exception.getMessage() != null ? exception.getMessage() : "Ocorreu um erro inesperado.")
                    .append("\n")
                    .toAnsi();

            terminal.writer().println(errorMessage);
            terminal.writer().flush();

            return CommandHandlingResult.of(null, 1);
        };
    }
}