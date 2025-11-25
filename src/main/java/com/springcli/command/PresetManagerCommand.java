package com.springcli.command;

import com.springcli.infra.console.ConsoleService;
import com.springcli.model.*;
import com.springcli.service.*;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.style.TemplateExecutor;

import java.util.*;
import java.util.stream.Collectors;

@ShellComponent
@RequiredArgsConstructor
public class PresetManagerCommand {

    private final PresetService presetService;
    private final MetadataService metadataService;
    private final DependencySelector dependencySelector;
    private final UISelector uiSelector;
    private final FeatureCustomizer featureCustomizer;
    private final ConsoleService consoleService;
    private final Terminal terminal;
    private final ResourceLoader resourceLoader;
    private final TemplateExecutor templateExecutor;

    @ShellMethod(key = "preset-manager", value = "Manage custom presets (create, edit, delete)")
    public void managePresets() {
        boolean running = true;

        while (running) {
            consoleService.clearScreen();
            consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
            consoleService.printInfo("║                    PRESET MANAGER                                ║");
            consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

            List<SelectorItem<String>> menuItems = List.of(
                    SelectorItem.of("➕ Create New Preset     - Build a custom preset from scratch", "create"),
                    SelectorItem.of("✏️  Edit Existing Preset - Modify an existing preset", "edit"),
                    SelectorItem.of("📋 List All Presets      - View all available presets", "list"),
                    SelectorItem.of("🗑️  Delete Preset        - Remove a custom preset", "delete"),
                    SelectorItem.of("🔙 Back to Main Menu     - Return to main menu", "back")
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

            String choice = context.getResultItem().map(SelectorItem::getItem).orElse("back");

            try {
                switch (choice) {
                    case "create":
                        createPreset();
                        waitForKeyPress();
                        break;
                    case "edit":
                        editPreset();
                        waitForKeyPress();
                        break;
                    case "list":
                        listPresets();
                        waitForKeyPress();
                        break;
                    case "delete":
                        deletePreset();
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

    private void createPreset() {
        consoleService.clearScreen();
        consoleService.printBanner();
        consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                    CREATE NEW PRESET                             ║");
        consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

        consoleService.printSuccess("Let's create your custom preset! 🎨\n");

        // 1. Nome do Preset
        String presetName = uiSelector.askString("Preset Name (e.g., 'My API Template')", "");
        if (presetName.trim().isEmpty()) {
            consoleService.printError("Preset name cannot be empty!");
            return;
        }

        // Verifica se já existe
        List<Preset> existingPresets = presetService.getAllPresets();
        boolean nameExists = existingPresets.stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(presetName));

        if (nameExists) {
            consoleService.printError("A preset with this name already exists!");
            return;
        }

        // 2. Descrição
        String description = uiSelector.askString("Description (brief description of the preset)", "");

        // 3. Arquitetura
        consoleService.printInfo("\n📐 SELECT ARCHITECTURE");
        Architecture architecture = selectArchitecture();

        // 4. Versão do Java
        consoleService.printInfo("\n☕ SELECT JAVA VERSION");
        String javaVersion = selectJavaVersion();

        // 5. Dependências
        consoleService.printInfo("\n📦 SELECT DEPENDENCIES");
        consoleService.printInfo("  ℹ️  Choose the dependencies your preset should include\n");

        SpringMetadata metadata = metadataService.getMetadata();
        Set<String> dependencies = dependencySelector.selectDependenciesByCategory(new HashSet<>(), metadata);

        // 6. Features
        consoleService.printInfo("\n⚙️  CONFIGURE FEATURES");
        consoleService.printInfo("  ℹ️  Set default feature configurations for this preset\n");

        ProjectFeatures features = featureCustomizer.customizeFeatures(ProjectFeatures.defaults(), dependencies);

        // Criar o preset
        Preset newPreset = new Preset(
                presetName,
                description.isEmpty() ? "Custom preset" : description,
                architecture,
                javaVersion,
                dependencies,
                features,
                false // não é built-in
        );

        // Salvar
        presetService.savePreset(newPreset);

        // Confirmação
        consoleService.clearScreen();
        consoleService.printSuccess("\n✅ PRESET CREATED SUCCESSFULLY!\n");
        displayPresetSummary(newPreset);
    }

    private void editPreset() {
        consoleService.clearScreen();
        consoleService.printBanner();
        consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                    EDIT PRESET                                   ║");
        consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

        List<Preset> allPresets = presetService.getAllPresets();
        if (allPresets.isEmpty()) {
            consoleService.printWarning("No presets available to edit.");
            return;
        }

        // Selecionar preset para editar
        Preset selectedPreset = selectPreset(allPresets, "Select preset to edit:");
        if (selectedPreset == null) return;

        consoleService.printInfo("\n📝 Editing preset: " + selectedPreset.name());
        consoleService.printWarning("  ℹ️  Press ENTER to keep current value\n");

        // Editar cada campo
        String newName = uiSelector.askString("Preset Name", selectedPreset.name());
        String newDescription = uiSelector.askString("Description", selectedPreset.description());

        consoleService.printInfo("\n📐 ARCHITECTURE");
        consoleService.printInfo("  Current: " + selectedPreset.architecture().getDisplayName());
        boolean changeArch = uiSelector.askYesNo("    Change architecture?", false);
        Architecture newArchitecture = changeArch ? selectArchitecture() : selectedPreset.architecture();

        consoleService.printInfo("\n☕ JAVA VERSION");
        consoleService.printInfo("  Current: " + selectedPreset.javaVersion());
        boolean changeJava = uiSelector.askYesNo("    Change Java version?", false);
        String newJavaVersion = changeJava ? selectJavaVersion() : selectedPreset.javaVersion();

        consoleService.printInfo("\n📦 DEPENDENCIES");
        consoleService.printInfo("  Current dependencies: " + selectedPreset.dependencies().size());
        boolean changeDeps = uiSelector.askYesNo("    Change dependencies?", false);
        Set<String> newDependencies = changeDeps ?
                dependencySelector.selectDependenciesByCategory(selectedPreset.dependencies(), metadataService.getMetadata()) :
                selectedPreset.dependencies();

        consoleService.printInfo("\n⚙️  FEATURES");
        boolean changeFeatures = uiSelector.askYesNo("    Change feature configuration?", false);
        ProjectFeatures newFeatures = changeFeatures ?
                featureCustomizer.customizeFeatures(selectedPreset.features(), newDependencies) :
                selectedPreset.features();

        // Criar preset atualizado
        Preset updatedPreset = new Preset(
                newName,
                newDescription,
                newArchitecture,
                newJavaVersion,
                newDependencies,
                newFeatures,
                false // sempre salva como custom
        );

        // Se mudou o nome, deletar o antigo
        if (!newName.equals(selectedPreset.name()) && !selectedPreset.builtIn()) {
            presetService.deletePreset(selectedPreset.name());
        }

        // Salvar
        presetService.savePreset(updatedPreset);

        consoleService.clearScreen();
        consoleService.printSuccess("\n✅ PRESET UPDATED SUCCESSFULLY!\n");
        displayPresetSummary(updatedPreset);
    }

    private void deletePreset() {
        consoleService.clearScreen();
        consoleService.printBanner();
        consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                    DELETE PRESET                                 ║");
        consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

        List<Preset> userPresets = presetService.getUserPresets();
        if (userPresets.isEmpty()) {
            consoleService.printWarning("No custom presets to delete.");
            consoleService.printInfo("  ℹ️  Built-in presets cannot be deleted.\n");
            return;
        }

        Preset selectedPreset = selectPreset(userPresets, "Select preset to delete:");
        if (selectedPreset == null) return;

        displayPresetSummary(selectedPreset);

        boolean confirm = uiSelector.askYesNo("\n⚠️  Are you sure you want to delete this preset?", false);

        if (confirm) {
            presetService.deletePreset(selectedPreset.name());
            consoleService.printSuccess("\n✅ Preset '" + selectedPreset.name() + "' deleted successfully!\n");
        } else {
            consoleService.printInfo("\nDeletion cancelled.\n");
        }
    }

    private void listPresets() {
        consoleService.clearScreen();
        consoleService.printBanner();
        consoleService.printInfo("\n╔══════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                    ALL PRESETS                                   ║");
        consoleService.printInfo("╚══════════════════════════════════════════════════════════════════╝\n");

        List<Preset> allPresets = presetService.getAllPresets();

        List<Preset> builtIn = allPresets.stream().filter(Preset::builtIn).collect(Collectors.toList());
        List<Preset> custom = allPresets.stream().filter(p -> !p.builtIn()).collect(Collectors.toList());

        if (!builtIn.isEmpty()) {
            consoleService.printInfo("🔧 BUILT-IN PRESETS:");
            for (Preset preset : builtIn) {
                displayPresetListItem(preset);
            }
        }

        if (!custom.isEmpty()) {
            consoleService.printInfo("\n⭐ CUSTOM PRESETS:");
            for (Preset preset : custom) {
                displayPresetListItem(preset);
            }
        }

        if (allPresets.isEmpty()) {
            consoleService.printWarning("No presets available.");
        }

        System.out.println();
    }

    private void displayPresetListItem(Preset preset) {
        System.out.printf("  • %-20s │ %-15s │ Java %-2s │ %2d deps\n",
                preset.name(),
                preset.architecture().name(),
                preset.javaVersion(),
                preset.dependencies().size());
        System.out.printf("    %s\n", preset.description());
    }

    private void displayPresetSummary(Preset preset) {
        consoleService.printInfo("╔══ PRESET SUMMARY ═════════════════════════════════════════╗");
        consoleService.printInfo("│  Name:         " + preset.name());
        consoleService.printInfo("│  Description:  " + preset.description());
        consoleService.printInfo("│  Architecture: " + preset.architecture().getDisplayName());
        consoleService.printInfo("│  Java Version: " + preset.javaVersion());
        consoleService.printInfo("│  Dependencies: " + preset.dependencies().size() + " selected");
        consoleService.printInfo("│  Type:         " + (preset.builtIn() ? "Built-in" : "Custom"));
        consoleService.printInfo("╚═══════════════════════════════════════════════════════════╝");
    }

    private Preset selectPreset(List<Preset> presets, String message) {
        List<SelectorItem<Preset>> items = presets.stream()
                .map(p -> SelectorItem.of(
                        String.format("%-20s - %s", p.name(), p.description()),
                        p
                ))
                .collect(Collectors.toList());

        SingleItemSelector<Preset, SelectorItem<Preset>> selector = new SingleItemSelector<>(
                terminal,
                items,
                message,
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<Preset, SelectorItem<Preset>> context =
                selector.run(SingleItemSelector.SingleItemSelectorContext.empty());

        return context.getResultItem().map(SelectorItem::getItem).orElse(null);
    }

    private Architecture selectArchitecture() {
        List<SelectorItem<Architecture>> architectureItems = Arrays.stream(Architecture.values())
                .map(arch -> SelectorItem.of(
                        String.format("%-20s - %s", arch.name(), arch.getDisplayName()),
                        arch
                ))
                .collect(Collectors.toList());

        SingleItemSelector<Architecture, SelectorItem<Architecture>> archSelector = new SingleItemSelector<>(
                terminal,
                architectureItems,
                "Select Architecture:",
                null
        );
        archSelector.setResourceLoader(resourceLoader);
        archSelector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<Architecture, SelectorItem<Architecture>> context =
                archSelector.run(SingleItemSelector.SingleItemSelectorContext.empty());

        return context.getResultItem()
                .map(SelectorItem::getItem)
                .orElse(Architecture.MVC);
    }

    private String selectJavaVersion() {
        SpringMetadata metadata = metadataService.getMetadata();
        List<String> javaVersions = metadata.javaVersions().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        if (javaVersions.isEmpty()) {
            return "21"; // fallback
        }

        List<SelectorItem<String>> versionItems = javaVersions.stream()
                .map(v -> SelectorItem.of("Java " + v, v))
                .collect(Collectors.toList());

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                versionItems,
                "Select Java Version:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context =
                selector.run(SingleItemSelector.SingleItemSelectorContext.empty());

        return context.getResultItem()
                .map(SelectorItem::getItem)
                .orElse(javaVersions.get(0));
    }

    private void waitForKeyPress() {
        try {
            consoleService.printSuccess("\n🔙 Press ENTER to return to preset manager...");
            terminal.reader().read();
        } catch (Exception e) {
            // Ignore
        }
    }
}
