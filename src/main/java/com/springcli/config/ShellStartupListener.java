package com.springcli.config;

import com.springcli.infra.console.ConsoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShellStartupListener {

    private final ConsoleService consoleService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        consoleService.clearScreen();
        printWelcome();
    }

    private void printWelcome() {
        consoleService.printBanner();
        System.out.println();
        consoleService.printInfo("╔═══════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                                                               ║");
        consoleService.printInfo("║  Welcome to Spring CLI - Modern Spring Boot Generator        ║");
        consoleService.printInfo("║                                                               ║");
        consoleService.printInfo("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();

        consoleService.printSuccess("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        consoleService.printSuccess("  📖 HOW TO USE:");
        consoleService.printSuccess("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();

        System.out.println("  " + BOLD + "1️⃣  Interactive Menu (Recommended)" + RESET);
        System.out.println("     Type: " + GREEN + BOLD + "m" + RESET + " or " + GREEN + BOLD + "menu" + RESET);
        System.out.println("     → Navigate with " + CYAN + "arrow keys" + RESET + ", select with " + CYAN + "ENTER" + RESET);
        System.out.println();

        System.out.println("  " + BOLD + "2️⃣  Quick Generation" + RESET);
        System.out.println("     Type: " + GREEN + BOLD + "new" + RESET + " " + YELLOW + "<project-name>" + RESET + " [options]");
        System.out.println("     Example: " + GREEN + "new my-api --groupId=com.company" + RESET);
        System.out.println();

        System.out.println("  " + BOLD + "3️⃣  Full Generation" + RESET);
        System.out.println("     Type: " + GREEN + BOLD + "generate" + RESET);
        System.out.println("     → Choose presets and configure everything");
        System.out.println();

        consoleService.printSuccess("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        consoleService.printSuccess("  🚀 QUICK START COMMANDS:");
        consoleService.printSuccess("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();

        System.out.println("  " + GREEN + BOLD + "m" + RESET +
                          "                    → " + CYAN + "Interactive menu" + RESET + " ⭐");
        System.out.println("  " + GREEN + BOLD + "generate" + RESET +
                          "            → " + CYAN + "Generate with presets" + RESET);
        System.out.println("  " + GREEN + BOLD + "preset-manager" + RESET +
                          "     → " + CYAN + "Manage custom presets" + RESET);
        System.out.println("  " + GREEN + BOLD + "help" + RESET +
                          "                → " + CYAN + "Show all commands" + RESET);
        System.out.println();

        consoleService.printSuccess("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();

        System.out.println(BOLD + YELLOW + "  💡 First time? Just type: " + GREEN + "m" + YELLOW + " and press ENTER!" + RESET);
        System.out.println();
        System.out.println("  " + CYAN + "Need help? Type: " + GREEN + "help" + RESET);
        System.out.println();
    }

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";
}
