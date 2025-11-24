package com.springcli.command;

import com.springcli.infra.console.ConsoleService;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.style.TemplateExecutor;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class MainMenuCommand {

    private final GenerateCommand generateCommand;
    private final UtilityCommands utilityCommands;
    private final ConsoleService consoleService;
    private final Terminal terminal;
    private final ResourceLoader resourceLoader;
    private final TemplateExecutor templateExecutor;

    @ShellMethod(key = {"menu", "m"}, value = "Show interactive main menu")
    public void showMainMenu() {
        boolean running = true;

        while (running) {
            consoleService.printBanner();
            consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
            consoleService.printInfo("║                    SPRING CLI GENERATOR                          ║");
            consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

            List<SelectorItem<String>> menuItems = List.of(
                    SelectorItem.of("🚀 Generate New Project      - Create a complete Spring Boot project", "generate"),
                    SelectorItem.of("📦 Quick Generate (minimal)  - Fast project generation", "quick"),
                    SelectorItem.of("⚙️  Configure CLI            - Set default preferences", "config"),
                    SelectorItem.of("📋 List Presets              - View available project templates", "presets"),
                    SelectorItem.of("ℹ️  About                    - Information about Spring CLI", "about"),
                    SelectorItem.of("❌ Exit                      - Close the application", "exit")
            );

            SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                    terminal,
                    menuItems,
                    "Select an option:",
                    null
            );
            selector.setResourceLoader(resourceLoader);
            selector.setTemplateExecutor(templateExecutor);

            SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context =
                    selector.run(SingleItemSelector.SingleItemSelectorContext.empty());

            String choice = context.getResultItem().map(SelectorItem::getItem).orElse("exit");

            try {
                switch (choice) {
                    case "generate":
                        generateCommand.generate();
                        waitForKeyPress();
                        break;
                    case "quick":
                        handleQuickGenerate();
                        waitForKeyPress();
                        break;
                    case "config":
                        utilityCommands.showConfig();
                        waitForKeyPress();
                        break;
                    case "presets":
                        utilityCommands.listPresets();
                        waitForKeyPress();
                        break;
                    case "about":
                        showAbout();
                        waitForKeyPress();
                        break;
                    case "exit":
                        running = false;
                        consoleService.printSuccess("\n👋 Thanks for using Spring CLI Generator!\n");
                        break;
                    default:
                        consoleService.printWarning("Invalid option!");
                        break;
                }
            } catch (Exception e) {
                consoleService.printError("Error: " + e.getMessage());
                waitForKeyPress();
            }
        }
    }

    private void handleQuickGenerate() {
        consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                    QUICK GENERATE                                ║");
        consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");
        consoleService.printInfo("Example: new my-project --groupId=com.example --architecture=CLEAN\n");
        consoleService.printInfo("For quick generation, use the 'new' command with parameters.");
        consoleService.printInfo("Run 'help new' for more information.\n");
    }

    private void showAbout() {
        consoleService.printBanner();
        consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                      ABOUT SPRING CLI                            ║");
        consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");
        consoleService.printInfo("  Spring CLI Generator v1.0.0");
        consoleService.printInfo("  A powerful CLI tool for generating Spring Boot projects\n");
        consoleService.printInfo("  📐 Supported Architectures:");
        consoleService.printInfo("     • MVC (Model-View-Controller)");
        consoleService.printInfo("     • Layered Architecture");
        consoleService.printInfo("     • Clean Architecture");
        consoleService.printInfo("     • Hexagonal (Ports & Adapters)");
        consoleService.printInfo("     • Feature-Driven");
        consoleService.printInfo("     • Domain-Driven Design (DDD)");
        consoleService.printInfo("     • CQRS (Command Query Responsibility Segregation)");
        consoleService.printInfo("     • Event-Driven");
        consoleService.printInfo("     • Onion Architecture");
        consoleService.printInfo("     • Vertical Slice\n");
        consoleService.printInfo("  🎯 Features:");
        consoleService.printInfo("     • JWT Authentication");
        consoleService.printInfo("     • Swagger/OpenAPI Documentation");
        consoleService.printInfo("     • CORS Configuration");
        consoleService.printInfo("     • Global Exception Handler");
        consoleService.printInfo("     • Docker & Kubernetes Support");
        consoleService.printInfo("     • CI/CD Pipeline (GitHub Actions)");
        consoleService.printInfo("     • MapStruct Integration");
        consoleService.printInfo("     • JPA Auditing\n");
        consoleService.printInfo("  📚 For help: Type 'help' or 'h'");
        consoleService.printInfo("  🌐 GitHub: github.com/yourusername/spring-cli\n");
    }

    private void waitForKeyPress() {
        try {
            consoleService.printInfo("\nPress ENTER to continue...");
            terminal.reader().read();
        } catch (Exception e) {
            // Ignore
        }
    }
}
