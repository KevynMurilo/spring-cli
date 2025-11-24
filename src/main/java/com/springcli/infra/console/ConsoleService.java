package com.springcli.infra.console;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Service;

@Service
public class ConsoleService {

    private final Terminal terminal;

    public ConsoleService(Terminal terminal) {
        this.terminal = terminal;
    }

    public void printSuccess(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    public void printError(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold());
    }

    public void printInfo(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
    }

    public void printWarning(String message) {
        print(message, AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

    public void printBanner() {
        String banner = """
                ╔═══════════════════════════════════════════════════════════════════╗
                ║                                                                   ║
                ║      ███████╗██████╗ ██████╗ ██╗███╗   ██╗ ██████╗              ║
                ║      ██╔════╝██╔══██╗██╔══██╗██║████╗  ██║██╔════╝              ║
                ║      ███████╗██████╔╝██████╔╝██║██╔██╗ ██║██║  ███╗             ║
                ║      ╚════██║██╔═══╝ ██╔══██╗██║██║╚██╗██║██║   ██║             ║
                ║      ███████║██║     ██║  ██║██║██║ ╚████║╚██████╔╝             ║
                ║      ╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝ ╚═════╝              ║
                ║                                                                   ║
                ║           ⚡ Spring Boot Project Generator v1.0.0 ⚡              ║
                ║          Modern Spring Boot scaffolding tool                     ║
                ║                                                                   ║
                ║  🚀 Generate production-ready projects with best practices       ║
                ║                                                                   ║
                ╚═══════════════════════════════════════════════════════════════════╝
                """;
        print(banner, AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold());
    }

    public void printSeparator() {
        println("═".repeat(60), AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
    }

    public void printGenerationSuccess(String projectPath) {
        String projectName = projectPath.substring(projectPath.lastIndexOf('\\') + 1);

        println("\n" + "═".repeat(70), AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold());
        printSuccess("✓ PROJECT GENERATED SUCCESSFULLY!");
        println("═".repeat(70), AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold());

        println("\n📁 Location: " + projectPath, AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));

        println("\n🚀 Next Steps:", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold());
        println("  1. cd " + projectName, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
        println("  2. mvn spring-boot:run", AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
        println("  3. Open http://localhost:8080", AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));

        println("\n🌐 Available Endpoints:", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold());
        println("  • Application:   http://localhost:8080", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        println("  • Swagger UI:    http://localhost:8080/swagger-ui.html", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        println("  • H2 Console:    http://localhost:8080/h2-console", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        println("  • Actuator:      http://localhost:8080/actuator", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));

        println("\n✨ Happy coding! ✨\n", AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA).bold());
        println("═".repeat(70), AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold());
    }

    public void clearScreen() {
        terminal.puts(org.jline.utils.InfoCmp.Capability.clear_screen);
        terminal.flush();
    }

    public void printSection(String title) {
        println("\n╔══ " + title + " " + "═".repeat(Math.max(0, 60 - title.length())) + "╗",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA).bold());
    }

    private void print(String message, AttributedStyle style) {
        terminal.writer().println(new AttributedString(message, style).toAnsi());
        terminal.flush();
    }

    private void println(String message, AttributedStyle style) {
        print(message, style);
    }

    private void println(String message) {
        terminal.writer().println(message);
        terminal.flush();
    }
}
