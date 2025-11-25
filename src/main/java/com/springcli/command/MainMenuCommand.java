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
    private final PresetManagerCommand presetManagerCommand;
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
                    SelectorItem.of("📦 Quick Generate            - Fast project generation (interactive)", "quick"),
                    SelectorItem.of("⭐ Manage Presets            - Create, edit, or delete custom presets", "manage-presets"),
                    SelectorItem.of("📋 List Presets              - View available project templates", "presets"),
                    SelectorItem.of("⚙️  Configure CLI            - Set default preferences (interactive)", "config"),
                    SelectorItem.of("🛠️  Utilities                - Clear cache, refresh metadata, system info", "utilities"),
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
                    case "manage-presets":
                        presetManagerCommand.managePresets();
                        break;
                    case "presets":
                        utilityCommands.listPresets();
                        waitForKeyPress();
                        break;
                    case "config":
                        utilityCommands.configureInteractive();
                        waitForKeyPress();
                        break;
                    case "utilities":
                        showUtilitiesMenu();
                        break;
                    case "about":
                        showAbout();
                        waitForKeyPress();
                        break;
                    case "exit":
                        running = false;
                        consoleService.printSuccess("\n👋 Thanks for using Spring CLI Generator!\n");
                        System.exit(0);
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
        consoleService.clearScreen();
        consoleService.printBanner();
        consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                    QUICK GENERATE                                ║");
        consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

        consoleService.printSuccess("⚡ Fast project generation with minimal setup!\n");

        try {
            String artifactId = askInput("Project Name (e.g., my-api)", "my-project");

            String groupId = askInput("Group ID", "com.example");

            List<SelectorItem<String>> archItems = List.of(
                    SelectorItem.of("CLEAN       - Clean Architecture (Recommended)", "CLEAN"),
                    SelectorItem.of("HEXAGONAL   - Hexagonal (Ports & Adapters)", "HEXAGONAL"),
                    SelectorItem.of("MVC         - Model-View-Controller", "MVC"),
                    SelectorItem.of("LAYERED     - Layered Architecture", "LAYERED"),
                    SelectorItem.of("DDD         - Domain-Driven Design", "DDD"),
                    SelectorItem.of("CQRS        - Command Query Responsibility Segregation", "CQRS"),
                    SelectorItem.of("🔙 Cancel   - Return to menu", "CANCEL")
            );

            SingleItemSelector<String, SelectorItem<String>> archSelector = new SingleItemSelector<>(
                    terminal, archItems, "Select Architecture:", null
            );
            archSelector.setResourceLoader(resourceLoader);
            archSelector.setTemplateExecutor(templateExecutor);

            var archContext = archSelector.run(SingleItemSelector.SingleItemSelectorContext.empty());
            String architecture = archContext.getResultItem().map(SelectorItem::getItem).orElse("CANCEL");

            if ("CANCEL".equals(architecture)) {
                consoleService.printWarning("\n❌ Generation cancelled.");
                return;
            }

            String outputDir = askInput("Output Directory", ".");

            consoleService.printInfo("\n🚀 Generating project...\n");
            generateCommand.newProject(artifactId, groupId, architecture, outputDir);

        } catch (Exception e) {
            consoleService.printError("Error: " + e.getMessage());
        }
    }

    private String askInput(String prompt, String defaultValue) {
        org.springframework.shell.component.StringInput input =
            new org.springframework.shell.component.StringInput(terminal, prompt + " [" + defaultValue + "]:", defaultValue);
        input.setResourceLoader(resourceLoader);
        input.setTemplateExecutor(templateExecutor);

        org.springframework.shell.component.StringInput.StringInputContext context =
            input.run(org.springframework.shell.component.StringInput.StringInputContext.empty());

        String value = context.getResultValue();
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    private void showUtilitiesMenu() {
        boolean running = true;

        while (running) {
            consoleService.clearScreen();
            consoleService.printBanner();
            consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
            consoleService.printInfo("║                    UTILITIES                                     ║");
            consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

            List<SelectorItem<String>> utilityItems = List.of(
                    SelectorItem.of("🗑️  Clear Cache              - Remove cached metadata", "clear-cache"),
                    SelectorItem.of("🔄 Refresh Metadata         - Update from Spring Initializr", "refresh-metadata"),
                    SelectorItem.of("ℹ️  System Info              - Show system information", "system-info"),
                    SelectorItem.of("📖 Help                     - Show all available commands", "help"),
                    SelectorItem.of("🔙 Back to Main Menu        - Return to main menu", "back")
            );

            SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                    terminal, utilityItems, "Select an option:", null
            );
            selector.setResourceLoader(resourceLoader);
            selector.setTemplateExecutor(templateExecutor);

            var context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());
            String choice = context.getResultItem().map(SelectorItem::getItem).orElse("back");

            try {
                switch (choice) {
                    case "clear-cache":
                        utilityCommands.clearCache();
                        waitForKeyPress();
                        break;
                    case "refresh-metadata":
                        utilityCommands.refreshMetadata();
                        waitForKeyPress();
                        break;
                    case "system-info":
                        utilityCommands.info();
                        waitForKeyPress();
                        break;
                    case "help":
                        showHelp();
                        waitForKeyPress();
                        break;
                    case "back":
                        running = false;
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

    private void showHelp() {
        consoleService.clearScreen();
        consoleService.printBanner();
        consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                    COMMAND REFERENCE                             ║");
        consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

        consoleService.printSuccess("📚 INTERACTIVE MENU COMMANDS:\n");
        consoleService.printInfo("  m, menu              Open interactive menu (recommended)");
        consoleService.printInfo("  generate             Start project generation wizard");
        consoleService.printInfo("  preset-manager       Manage custom presets\n");

        consoleService.printSuccess("⚡ QUICK COMMANDS:\n");
        consoleService.printInfo("  new <name>           Quick project generation");
        consoleService.printInfo("    Example: new my-api --groupId=com.company --architecture=CLEAN\n");

        consoleService.printSuccess("🛠️  UTILITY COMMANDS:\n");
        consoleService.printInfo("  list-presets         List all available presets");
        consoleService.printInfo("  show-config          Show current configuration");
        consoleService.printInfo("  reset-config         Reset configuration to defaults");
        consoleService.printInfo("  clear-cache          Clear metadata cache");
        consoleService.printInfo("  refresh-metadata     Refresh metadata from Spring Initializr");
        consoleService.printInfo("  info                 Show system information");
        consoleService.printInfo("  version              Show CLI version");
        consoleService.printInfo("  clear                Clear terminal screen\n");

        consoleService.printSuccess("📖 GETTING HELP:\n");
        consoleService.printInfo("  help                 Show this help message");
        consoleService.printInfo("  help <command>       Show help for specific command\n");

        consoleService.printSuccess("💡 TIPS:\n");
        consoleService.printInfo("  • Press TAB for command completion");
        consoleService.printInfo("  • Press UP/DOWN arrows for command history");
        consoleService.printInfo("  • Type 'm' for the easiest experience!\n");
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
        consoleService.printInfo("  🌐 GitHub: https://github.com/KevynMurilo/spring-cli\n");
    }

    private void waitForKeyPress() {
        try {
            consoleService.printSuccess("\n🔙 Press ENTER to return to main menu...");
            terminal.reader().read();
            consoleService.clearScreen();
        } catch (Exception e) {
            // Ignore
        }
    }
}
