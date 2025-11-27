package com.springcli.config;

import com.springcli.service.UpdateCheckService;
import com.springcli.service.UpdateCheckService.UpdateInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupUpdateChecker implements CommandLineRunner {

    private final UpdateCheckService updateCheckService;
    private final Terminal terminal;

    @Override
    public void run(String... args) throws Exception {
        UpdateInfo updateInfo = updateCheckService.checkForUpdates();

        if (updateInfo.updateAvailable()) {
            displayUpdateNotification(updateInfo);
        }
    }

    private void displayUpdateNotification(UpdateInfo updateInfo) throws IOException {
        terminal.writer().println();
        terminal.writer().println(createStyledMessage(
            "╔═══════════════════════════════════════════════════════════════╗",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        ));
        terminal.writer().println(createStyledMessage(
            "║                                                               ║",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        ));
        terminal.writer().println(createStyledMessage(
            "║              🎉 NEW VERSION AVAILABLE! 🎉                    ║",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold()
        ));
        terminal.writer().println(createStyledMessage(
            "║                                                               ║",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        ));
        terminal.writer().println(createStyledMessage(
            String.format("║  Current version:  %-40s ║", updateInfo.currentVersion()),
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        ));
        terminal.writer().println(createStyledMessage(
            String.format("║  Latest version:   %-40s ║", updateInfo.latestVersion()),
            AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold()
        ));
        terminal.writer().println(createStyledMessage(
            "║                                                               ║",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        ));
        terminal.writer().println(createStyledMessage(
            String.format("║  Release notes: %-43s ║", truncate(updateInfo.releaseUrl(), 43)),
            AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN)
        ));
        terminal.writer().println(createStyledMessage(
            "║                                                               ║",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        ));
        terminal.writer().println(createStyledMessage(
            "║  To update:                                                   ║",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        ));
        terminal.writer().println(createStyledMessage(
            "║    • Download: mvn dependency:get -Dartifact=...             ║",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)
        ));
        terminal.writer().println(createStyledMessage(
            "║    • Or visit the release page above                          ║",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)
        ));
        terminal.writer().println(createStyledMessage(
            "║                                                               ║",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        ));
        terminal.writer().println(createStyledMessage(
            "╚═══════════════════════════════════════════════════════════════╝",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        ));
        terminal.writer().println();
        terminal.writer().flush();
    }

    private String createStyledMessage(String text, AttributedStyle style) {
        return new AttributedStringBuilder()
            .style(style)
            .append(text)
            .toAnsi();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }
}
