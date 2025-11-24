package com.springcli.command;

import com.springcli.infra.console.ConsoleService;
import com.springcli.model.*;
import com.springcli.service.*;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.MultiItemSelector;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.style.TemplateExecutor;

import java.util.*;
import java.util.stream.Collectors;

@ShellComponent
@RequiredArgsConstructor
public class GenerateCommand {

    private final ProjectGeneratorService generatorService;
    private final MetadataService metadataService;
    private final PresetService presetService;
    private final ConfigService configService;
    private final ConsoleService consoleService;
    private final Terminal terminal;
    private final ResourceLoader resourceLoader;
    private final TemplateExecutor templateExecutor;

    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    @ShellMethod(key = "generate", value = "Generate a new Spring Boot project")
    public void generate() {
        try {
            consoleService.printBanner();
            consoleService.printInfo("\nWelcome to Spring CLI Project Generator!\n");

            SpringMetadata metadata = metadataService.getMetadata();
            UserConfig userConfig = configService.loadConfig();

            Optional<Preset> selectedPreset = selectPreset();
            ProjectConfig config = selectedPreset.isPresent()
                    ? buildConfigFromPreset(selectedPreset.get(), metadata, userConfig)
                    : buildConfigFromScratch(metadata, userConfig);

            if (config == null) {
                consoleService.printWarning("\n👋 Project generation cancelled. See you next time!");
                return;
            }

            consoleService.printInfo("\nGenerating project...\n");
            generatorService.generateProject(config);

            consoleService.printGenerationSuccess(config.outputDirectory() + "/" + config.artifactId());

        } catch (java.io.IOError e) {
            consoleService.printWarning("\n\n👋 Operation cancelled by user. See you next time!");
        } catch (Exception e) {
            if (e.getCause() instanceof java.io.InterruptedIOException) {
                consoleService.printWarning("\n\n👋 Operation cancelled by user. See you next time!");
            } else {
                consoleService.printError("Failed to generate project: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @ShellMethod(key = "new", value = "Quick project generation with minimal prompts")
    public void newProject(
            @ShellOption(help = "Project artifact ID") String artifactId,
            @ShellOption(defaultValue = "com.example", help = "Group ID") String groupId,
            @ShellOption(defaultValue = "CLEAN", help = "Architecture") String architecture,
            @ShellOption(defaultValue = ".", help = "Output directory") String output
    ) {
        try {
            SpringMetadata metadata = metadataService.getMetadata();

            ProjectConfig config = new ProjectConfig(
                    groupId,
                    artifactId,
                    artifactId,
                    "Spring Boot Application",
                    groupId + "." + artifactId.replace("-", ""),
                    "21",
                    metadata.defaultBuildTool() != null ? metadata.defaultBuildTool() : "maven-project",
                    "jar",
                    Architecture.valueOf(architecture.toUpperCase()),
                    metadata.defaultSpringBootVersion(),
                    Set.of("web", "lombok"),
                    ProjectFeatures.defaults(),
                    output
            );

            consoleService.printInfo("Generating project: " + artifactId + "...\n");
            generatorService.generateProject(config);
            consoleService.printGenerationSuccess(output + "/" + artifactId);

        } catch (Exception e) {
            consoleService.printError("Failed to generate project: " + e.getMessage());
        }
    }

    private Optional<Preset> selectPreset() {
        List<Preset> presets = presetService.getAllPresets();

        List<SelectorItem<String>> items = new ArrayList<>();
        items.add(SelectorItem.of("Start from scratch", "SCRATCH"));

        presets.forEach(preset ->
                items.add(SelectorItem.of(preset.name() + " - " + preset.description(), preset.name()))
        );

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                items,
                "Select a preset or start from scratch:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        Optional<String> selected = context.getResultItem().map(SelectorItem::getItem);

        if (selected.isEmpty() || "SCRATCH".equals(selected.get())) {
            return Optional.empty();
        }

        String presetName = selected.get();
        return presets.stream()
                .filter(p -> p.name().equals(presetName))
                .findFirst();
    }

    private ProjectConfig buildConfigFromPreset(Preset preset, SpringMetadata metadata, UserConfig userConfig) {
        consoleService.printInfo("\n╔════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║  Preset: " + preset.name() + " ".repeat(Math.max(0, 53 - preset.name().length())) + "║");
        consoleService.printInfo("║  " + preset.description() + " ".repeat(Math.max(0, 61 - preset.description().length())) + "║");
        consoleService.printInfo("╚════════════════════════════════════════════════════════════════╝\n");

        consoleService.printInfo("📋 PROJECT METADATA\n");

        String groupId = askString("Group:", userConfig.defaultGroupId());
        String artifactId = askValidArtifactId("Artifact:", "demo", userConfig.defaultOutputDir());

        if (artifactId == null) {
            return null;
        }

        String name = askString("Name:", artifactId);
        String description = askString("Description:", "Demo project for Spring Boot");
        String packageName = askString("Package name:", groupId + "." + artifactId.replace("-", ""));

        consoleService.printInfo("\n⚙️  PROJECT SETTINGS\n");

        String springBootVersion = selectSpringBootVersion(metadata, metadata.defaultSpringBootVersion());
        String buildTool = selectBuildTool(metadata);
        String javaVersion = selectJavaVersion(metadata, preset.javaVersion());
        String packaging = selectPackaging(metadata);

        Architecture architecture = selectArchitecture(preset.architecture());

        Set<String> dependencies = selectDependenciesByCategory(preset.dependencies(), metadata);

        ProjectFeatures features = customizeFeatures(preset.features(), dependencies);

        consoleService.printInfo("");
        String output = askString("📁 Output Directory:", userConfig.defaultOutputDir());

        return new ProjectConfig(
                groupId,
                artifactId,
                name,
                description,
                packageName,
                javaVersion,
                buildTool,
                packaging,
                architecture,
                springBootVersion,
                dependencies,
                features,
                output
        );
    }

    private String askString(String prompt, String defaultValue) {
        try {
            StringInput input = new StringInput(terminal, "  " + prompt, defaultValue);
            input.setResourceLoader(resourceLoader);
            input.setTemplateExecutor(templateExecutor);
            String result = input.run(StringInput.StringInputContext.empty()).getResultValue();
            return result != null && !result.trim().isEmpty() ? result : defaultValue;
        } catch (java.io.IOError e) {
            throw new RuntimeException("User cancelled operation", e);
        } catch (Exception e) {
            if (e.getCause() instanceof java.io.InterruptedIOException) {
                throw new RuntimeException("User cancelled operation", e);
            }
            return defaultValue;
        }
    }

    private String askValidArtifactId(String prompt, String defaultValue, String outputDir) {
        String artifactId;
        int attempts = 0;
        final int maxAttempts = 5;

        while (attempts < maxAttempts) {
            artifactId = askString(prompt, defaultValue);

            java.nio.file.Path projectPath = java.nio.file.Paths.get(outputDir).resolve(artifactId);

            if (!java.nio.file.Files.exists(projectPath)) {
                return artifactId;
            }

            consoleService.printWarning("\n⚠️  Project '" + artifactId + "' already exists at: " + projectPath);

            boolean useAnother = askYesNo("  Would you like to use a different name?", true);

            if (!useAnother) {
                consoleService.printError("Cannot proceed: directory already exists.");
                consoleService.printInfo("Please delete the existing directory or choose a different name.\n");
                return null;
            }

            defaultValue = artifactId + "-new";
            attempts++;
        }

        consoleService.printError("Too many attempts. Project generation cancelled.");
        return null;
    }

    private String selectSpringBootVersion(SpringMetadata metadata, String defaultVersion) {
        consoleService.printInfo("  Spring Boot:");

        List<String> stableVersions = metadata.springBootVersions().stream()
                .filter(v -> !v.contains("SNAPSHOT") && !v.contains("M") && !v.contains("RC"))
                .collect(Collectors.toList());

        if (stableVersions.isEmpty()) {
            stableVersions = metadata.springBootVersions();
        }

        String stableDefault = stableVersions.isEmpty() ? defaultVersion : stableVersions.get(0);

        List<SelectorItem<String>> versionItems = stableVersions.stream()
                .map(version -> SelectorItem.of(
                        version + (version.equals(stableDefault) ? " (recommended)" : ""),
                        version
                ))
                .collect(Collectors.toList());

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                versionItems,
                "    Select version:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        String selected = context.getResultItem().map(SelectorItem::getItem).orElse(stableDefault);
        consoleService.printInfo("  Selected Spring Boot version: " + selected);
        return selected;
    }

    private String selectBuildTool(SpringMetadata metadata) {
        consoleService.printInfo("  Build Tool:");

        List<SelectorItem<String>> buildToolItems = metadata.buildTools().stream()
                .map(tool -> SelectorItem.of(
                        tool.name() + (tool.id().equals(metadata.defaultBuildTool()) ? " (recommended)" : ""),
                        tool.id()
                ))
                .collect(Collectors.toList());

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                buildToolItems,
                "    Select:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        return context.getResultItem().map(SelectorItem::getItem).orElse(metadata.defaultBuildTool());
    }

    private String selectJavaVersion(SpringMetadata metadata, String defaultVersion) {
        consoleService.printInfo("  Java:");

        List<SelectorItem<String>> javaItems = metadata.javaVersions().stream()
                .map(version -> SelectorItem.of(
                        version + (version.equals(defaultVersion) ? " (recommended)" : ""),
                        version
                ))
                .collect(Collectors.toList());

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                javaItems,
                "    Select version:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        return context.getResultItem().map(SelectorItem::getItem).orElse(defaultVersion);
    }

    private String selectPackaging(SpringMetadata metadata) {
        consoleService.printInfo("  Packaging:");

        List<SelectorItem<String>> packagingItems = metadata.packagingTypes().stream()
                .map(type -> SelectorItem.of(type.toUpperCase(), type))
                .collect(Collectors.toList());

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                terminal,
                packagingItems,
                "    Select type:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        return context.getResultItem().map(SelectorItem::getItem).orElse("jar");
    }

    private Architecture selectArchitecture(Architecture defaultArch) {
        consoleService.printInfo("\n📐 ARCHITECTURE\n");

        List<SelectorItem<Architecture>> architectureItems = Arrays.stream(Architecture.values())
                .map(arch -> {
                    String label = String.format("%-20s %s",
                            arch.name(),
                            (arch == defaultArch ? "(recommended)" : ""));
                    return SelectorItem.of(label, arch);
                })
                .collect(Collectors.toList());

        SingleItemSelector<Architecture, SelectorItem<Architecture>> selector = new SingleItemSelector<>(
                terminal,
                architectureItems,
                "  Select pattern:",
                null
        );
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);

        SingleItemSelector.SingleItemSelectorContext<Architecture, SelectorItem<Architecture>> context = selector.run(
                SingleItemSelector.SingleItemSelectorContext.empty()
        );

        return context.getResultItem().map(SelectorItem::getItem).orElse(defaultArch);
    }

    private Set<String> selectDependenciesByCategory(Set<String> presetDeps, SpringMetadata metadata) {
        Set<String> selectedDeps = new HashSet<>(presetDeps);

        printDependenciesTree(selectedDeps, metadata);

        boolean customize = askYesNo("  Do you want to manage dependencies?", presetDeps.isEmpty());
        if (!customize) {
            return selectedDeps;
        }

        if (metadata.dependencyGroups() == null || metadata.dependencyGroups().isEmpty()) {
            consoleService.printWarning("  No dependency metadata available");
            return selectedDeps;
        }

        boolean keepManaging = true;
        while (keepManaging) {
            consoleService.printInfo("\n" + CYAN + "╔══════════════ DEPENDENCY MANAGER ══════════════╗" + RESET);
            List<SelectorItem<String>> options = List.of(
                    SelectorItem.of(GREEN + "📂 Browse & Select Dependencies" + RESET, "browse"),
                    SelectorItem.of(YELLOW + "✅ Finish & Continue" + RESET, "done")
            );

            SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(
                    terminal, options, "", null
            );
            selector.setResourceLoader(resourceLoader);
            selector.setTemplateExecutor(templateExecutor);

            String choice = selector.run(SingleItemSelector.SingleItemSelectorContext.empty())
                    .getResultItem().map(SelectorItem::getItem).orElse("done");

            if ("browse".equals(choice)) {
                selectedDeps = browseDependenciesByCategory(metadata, selectedDeps);
                consoleService.printInfo("\n" + BOLD + "Current Selection:" + RESET);
                printDependenciesTree(selectedDeps, metadata);
            } else {
                keepManaging = false;
            }
        }

        return selectedDeps;
    }

    private Set<String> browseDependenciesByCategory(SpringMetadata metadata, Set<String> currentSelection) {
        Set<String> workingSelection = new HashSet<>(currentSelection);

        List<SelectorItem<String>> categoryItems = new ArrayList<>();
        categoryItems.add(SelectorItem.of(YELLOW + "← Back to Menu" + RESET, "back"));
        categoryItems.addAll(
                metadata.dependencyGroups().keySet().stream()
                        .map(cat -> SelectorItem.of(cat, cat))
                        .collect(Collectors.toList())
        );

        SingleItemSelector<String, SelectorItem<String>> catSelector = new SingleItemSelector<>(
                terminal, categoryItems, "\n  📂 Select Category:", null
        );
        catSelector.setResourceLoader(resourceLoader);
        catSelector.setTemplateExecutor(templateExecutor);

        String category = catSelector.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem().map(SelectorItem::getItem).orElse("back");

        if ("back".equals(category)) {
            return workingSelection;
        }

        DependencyGroup group = metadata.dependencyGroups().get(category);
        if (group == null) {
            return workingSelection;
        }

        List<SelectorItem<String>> items = group.dependencies().stream()
                .map(dep -> SelectorItem.of(
                        String.format("%-20s %s", dep.name(), dep.description() != null ? "(" + truncate(dep.description(), 40) + ")" : ""),
                        dep.id()
                ))
                .collect(Collectors.toList());

        MultiItemSelector<String, SelectorItem<String>> multiSelector = new MultiItemSelector<>(
                terminal,
                items,
                "Select dependencies for " + category + " (SPACE to toggle, ENTER to confirm):",
                null
        );
        multiSelector.setResourceLoader(resourceLoader);
        multiSelector.setTemplateExecutor(templateExecutor);

        MultiItemSelector.MultiItemSelectorContext<String, SelectorItem<String>> context =
                multiSelector.run(MultiItemSelector.MultiItemSelectorContext.empty());

        Set<String> selectedIdsInThisRun = context.getResultItems().stream()
                .map(SelectorItem::getItem)
                .collect(Collectors.toSet());

        Set<String> allIdsInGroup = group.dependencies().stream().map(Dependency::id).collect(Collectors.toSet());

        workingSelection.removeIf(allIdsInGroup::contains);
        workingSelection.addAll(selectedIdsInThisRun);

        return workingSelection;
    }

    private String truncate(String str, int maxWidth) {
        if (str.length() <= maxWidth) return str;
        return str.substring(0, maxWidth - 3) + "...";
    }

    private void printDependenciesTree(Set<String> selectedIds, SpringMetadata metadata) {
        if (selectedIds.isEmpty()) {
            consoleService.printWarning("\n   📦 No dependencies selected");
            return;
        }

        consoleService.printInfo("\n   " + BOLD + CYAN + "📦 SELECTED DEPENDENCIES (" + selectedIds.size() + "):" + RESET);

        Map<String, List<String>> organized = new LinkedHashMap<>();
        Set<String> processedIds = new HashSet<>();

        if (metadata.dependencyGroups() != null) {
            metadata.dependencyGroups().forEach((groupName, group) -> {
                List<String> depsInGroup = group.dependencies().stream()
                        .filter(d -> selectedIds.contains(d.id()))
                        .map(d -> {
                            processedIds.add(d.id());
                            return d.name();
                        })
                        .collect(Collectors.toList());

                if (!depsInGroup.isEmpty()) {
                    organized.put(groupName, depsInGroup);
                }
            });
        }

        List<String> others = selectedIds.stream()
                .filter(id -> !processedIds.contains(id))
                .collect(Collectors.toList());

        if (!others.isEmpty()) {
            organized.put("Custom / Others", others);
        }

        organized.forEach((category, items) -> {
            consoleService.printInfo("   " + YELLOW + "├─ " + category + RESET);
            for (int i = 0; i < items.size(); i++) {
                String prefix = (i == items.size() - 1) ? "   └──" : "   ├──";
                consoleService.printInfo(prefix + GREEN + " ✓ " + items.get(i) + RESET);
            }
        });
    }

    private ProjectFeatures customizeFeatures(ProjectFeatures presetFeatures, Set<String> dependencies) {
        consoleService.printInfo("\n🎯 FEATURES & GENERATION OPTIONS\n");

        boolean hasSecurity = dependencies.contains("security");

        boolean enableJwt = false;
        if (hasSecurity) {
            enableJwt = askYesNo("  JWT Authentication", presetFeatures.enableJwt());
        }

        boolean enableSwagger = askYesNo("  Swagger/OpenAPI", presetFeatures.enableSwagger());
        boolean enableCors = askYesNo("  CORS Configuration", presetFeatures.enableCors());
        boolean enableExceptionHandler = askYesNo("  Global Exception Handler", presetFeatures.enableExceptionHandler());

        consoleService.printInfo("\n  DevOps & Infrastructure:");
        boolean enableDocker = askYesNo("    Docker files", presetFeatures.enableDocker());
        boolean enableKubernetes = askYesNo("    Kubernetes manifests", presetFeatures.enableKubernetes());
        boolean enableCiCd = askYesNo("    CI/CD pipeline (GitHub Actions)", presetFeatures.enableCiCd());

        return new ProjectFeatures(
                enableJwt,
                enableSwagger,
                enableCors,
                enableExceptionHandler,
                presetFeatures.enableMapStruct(),
                enableDocker,
                enableKubernetes,
                enableCiCd,
                presetFeatures.enableAudit()
        );
    }

    private boolean askYesNo(String question, boolean defaultValue) {
        String defaultText = defaultValue ? "Y/n" : "y/N";
        String prompt = String.format("%-40s (%s):", question, defaultText);
        StringInput input = new StringInput(terminal, prompt, "");
        input.setResourceLoader(resourceLoader);
        input.setTemplateExecutor(templateExecutor);

        String answer = input.run(StringInput.StringInputContext.empty()).getResultValue();

        if (answer == null || answer.trim().isEmpty()) {
            return defaultValue;
        }

        return answer.trim().equalsIgnoreCase("y") || answer.trim().equalsIgnoreCase("yes");
    }

    private ProjectConfig buildConfigFromScratch(SpringMetadata metadata, UserConfig userConfig) {
        consoleService.printInfo("\n╔════════════════════════════════════════════════════════════════╗");
        consoleService.printInfo("║                CUSTOM PROJECT CONFIGURATION                    ║");
        consoleService.printInfo("╚════════════════════════════════════════════════════════════════╝\n");

        consoleService.printInfo("📋 PROJECT METADATA\n");

        String artifactId = askValidArtifactId("Artifact:", "demo", userConfig.defaultOutputDir());

        if (artifactId == null) {
            return null;
        }

        String groupId = askString("Group:", userConfig.defaultGroupId());
        String name = askString("Name:", artifactId);
        String description = askString("Description:", "Demo project for Spring Boot");
        String packageName = askString("Package name:", groupId + "." + artifactId.replace("-", ""));

        consoleService.printInfo("\n⚙️  PROJECT SETTINGS\n");

        String springBootVersion = selectSpringBootVersion(metadata, metadata.defaultSpringBootVersion());
        String buildTool = selectBuildTool(metadata);
        String javaVersion = selectJavaVersion(metadata, "21");
        String packaging = selectPackaging(metadata);

        Architecture architecture = selectArchitecture(Architecture.CLEAN);

        Set<String> dependencies = selectDependenciesByCategory(new HashSet<>(), metadata);

        ProjectFeatures features = customizeFeatures(ProjectFeatures.defaults(), dependencies);

        consoleService.printInfo("");
        String output = askString("📁 Output Directory:", userConfig.defaultOutputDir());

        return new ProjectConfig(
                groupId,
                artifactId,
                name,
                description,
                packageName,
                javaVersion,
                buildTool,
                packaging,
                architecture,
                springBootVersion,
                dependencies,
                features,
                output
        );
    }
}