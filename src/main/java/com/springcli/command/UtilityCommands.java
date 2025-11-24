package com.springcli.command;

import com.springcli.infra.console.ConsoleService;
import com.springcli.model.Preset;
import com.springcli.model.UserConfig;
import com.springcli.service.CacheService;
import com.springcli.service.ConfigService;
import com.springcli.service.MetadataService;
import com.springcli.service.PresetService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class UtilityCommands {

    private final PresetService presetService;
    private final ConfigService configService;
    private final CacheService cacheService;
    private final MetadataService metadataService;
    private final ConsoleService consoleService;

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
}