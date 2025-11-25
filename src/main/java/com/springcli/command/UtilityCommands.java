package com.springcli.command;

import com.springcli.infra.console.ConsoleService;
import com.springcli.model.Architecture;
import com.springcli.model.Preset;
import com.springcli.model.UserConfig;
import com.springcli.service.CacheService;
import com.springcli.service.ConfigService;
import com.springcli.service.MetadataService;
import com.springcli.service.PresetService;
import com.springcli.service.UISelector;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.style.TemplateExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ShellComponent
@RequiredArgsConstructor
public class UtilityCommands {

    private final PresetService presetService;
    private final ConfigService configService;
    private final CacheService cacheService;
    private final MetadataService metadataService;
    private final ConsoleService consoleService;
    private final UISelector uiSelector;
    private final Terminal terminal;
    private final ResourceLoader resourceLoader;
    private final TemplateExecutor templateExecutor;

    @ShellMethod(key = "list-presets", value = "List all available presets")
    public void listPresets() {
        List<Preset> presets = presetService.getAllPresets();

        consoleService.printInfo("\n Available Presets:\n");
        consoleService.printSeparator();

        presets.forEach(preset -> {
            String badge = preset.builtIn() ? "[BUILT-IN]" : "[CUSTOM]";
            consoleService.printInfo("\n  " + badge + " " + preset.name());
            System.out.println("    Description: " + preset.description());
            System.out.println("    Architecture: " + preset.architecture().getDisplayName());
            System.out.println("    Java Version: " + preset.javaVersion());
            System.out.println("    Dependencies: " + String.join(", ", preset.dependencies()));
        });

        consoleService.printSeparator();
    }

    @ShellMethod(key = "show-config", value = "Show current configuration")
    public void showConfig() {
        UserConfig config = configService.loadConfig();

        consoleService.printInfo("\n Current Configuration:\n");
        consoleService.printSeparator();
        System.out.println("  Default Group ID: " + config.defaultGroupId());
        System.out.println("  Default Java Version: " + config.defaultJavaVersion());
        System.out.println("  Default Packaging: " + config.defaultPackaging());
        System.out.println("  Default Architecture: " + config.defaultArchitecture().getDisplayName());
        System.out.println("  Default Output Dir: " + config.defaultOutputDir());
        System.out.println("  Auto Open IDE: " + config.autoOpenIde());
        System.out.println("  Preferred IDE: " + config.preferredIde());
        consoleService.printSeparator();
    }

    @ShellMethod(key = "reset-config", value = "Reset configuration to defaults")
    public void resetConfig() {
        configService.resetConfig();
        consoleService.printSuccess("✓ Configuration reset to defaults");
    }

    @ShellMethod(key = "clear-cache", value = "Clear metadata cache")
    public void clearCache() {
        cacheService.clearCache();
        consoleService.printSuccess("✓ Cache cleared successfully");
    }

    @ShellMethod(key = "refresh-metadata", value = "Refresh metadata from Spring Initializr")
    public void refreshMetadata() {
        consoleService.printInfo("Refreshing metadata from Spring Initializr...");
        metadataService.refreshMetadata();
        consoleService.printSuccess("✓ Metadata refreshed successfully");
    }

    @ShellMethod(key = "delete-preset", value = "Delete a custom preset")
    public void deletePreset(@ShellOption(help = "Preset name") String name) {
        try {
            presetService.deletePreset(name);
            consoleService.printSuccess("✓ Preset '" + name + "' deleted successfully");
        } catch (Exception e) {
            consoleService.printError("Failed to delete preset: " + e.getMessage());
        }
    }

    @ShellMethod(key = "clear", value = "Clear the terminal screen")
    public void clear() {
        consoleService.clearScreen();
    }

    @ShellMethod(key = "version", value = "Show CLI version")
    public void version() {
        consoleService.printInfo("\nSpring CLI v1.0.0");
        consoleService.printInfo("Java-based Spring Boot Project Generator\n");
    }

    @ShellMethod(key = "info", value = "Show system information")
    public void info() {
        consoleService.printInfo("\n System Information:\n");
        consoleService.printSeparator();
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("  User Home: " + System.getProperty("user.home"));
        System.out.println("  Config Dir: " + System.getProperty("user.home") + "/.spring-cli");
        consoleService.printSeparator();
    }

    public void configureInteractive() {
        consoleService.clearScreen();
        consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                    CONFIGURE CLI                                 ║");
        consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

        consoleService.printSuccess("⚙️  Configure default values for project generation\n");

        UserConfig currentConfig = configService.loadConfig();

        String groupId = uiSelector.askString("Default Group ID", currentConfig.defaultGroupId());

        List<SelectorItem<String>> javaItems = List.of(
                SelectorItem.of("Java 21 (Recommended)", "21"),
                SelectorItem.of("Java 17", "17")
        );
        String javaVersion = selectFromList(javaItems, "Default Java Version:", currentConfig.defaultJavaVersion());

        List<SelectorItem<String>> packagingItems = List.of(
                SelectorItem.of("JAR (Recommended)", "jar"),
                SelectorItem.of("WAR", "war")
        );
        String packaging = selectFromList(packagingItems, "Default Packaging:", currentConfig.defaultPackaging());

        List<SelectorItem<Architecture>> archItems = Arrays.stream(Architecture.values())
                .map(arch -> SelectorItem.of(arch.name() + " - " + arch.getDisplayName(), arch))
                .collect(Collectors.toList());
        Architecture architecture = selectArchitecture(archItems, "Default Architecture:", currentConfig.defaultArchitecture());

        String outputDir = uiSelector.askString("Default Output Directory", currentConfig.defaultOutputDir());

        boolean autoOpenIde = uiSelector.askYesNo("Auto-open IDE after generation?", currentConfig.autoOpenIde());

        String preferredIde = currentConfig.preferredIde();
        if (autoOpenIde) {
            List<SelectorItem<String>> ideItems = List.of(
                    SelectorItem.of("IntelliJ IDEA", "idea"),
                    SelectorItem.of("VS Code", "code"),
                    SelectorItem.of("Eclipse", "eclipse")
            );
            preferredIde = selectFromList(ideItems, "Preferred IDE:", currentConfig.preferredIde());
        }

        boolean generateReadme = uiSelector.askYesNo("Generate README.md by default?", currentConfig.generateReadme());

        boolean generateGitignore = uiSelector.askYesNo("Generate .gitignore by default?", currentConfig.generateGitignore());

        boolean useApplicationYml = uiSelector.askYesNo("Use application.yml instead of application.properties?", currentConfig.useApplicationYml());

        UserConfig newConfig = new UserConfig(
                groupId,
                javaVersion,
                packaging,
                architecture,
                outputDir,
                autoOpenIde,
                preferredIde,
                useApplicationYml,
                generateReadme,
                generateGitignore
        );

        configService.saveConfig(newConfig);

        consoleService.clearScreen();
        consoleService.printSuccess("\n✅ CONFIGURATION SAVED!\n");
        showConfig();
    }

    private String selectFromList(List<SelectorItem<String>> items, String prompt, String defaultValue) {
        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal, items, prompt, null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        var context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());
        return context.getResultItem().map(SelectorItem::getItem).orElse(defaultValue);
    }

    private Architecture selectArchitecture(List<SelectorItem<Architecture>> items, String prompt, Architecture defaultValue) {
        SingleItemSelector<Architecture, SelectorItem<Architecture>> selector = new SingleItemSelector<>(
                terminal, items, prompt, null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        var context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());
        return context.getResultItem().map(SelectorItem::getItem).orElse(defaultValue);
    }
}