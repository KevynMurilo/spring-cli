package com.springcli.model;

public record UserConfig(
        String defaultGroupId,
        String defaultJavaVersion,
        String defaultPackaging,
        Architecture defaultArchitecture,
        String defaultOutputDir,
        boolean autoOpenIde,
        String preferredIde,
        boolean useApplicationYml,
        boolean generateReadme,
        boolean generateGitignore
) {
    public static UserConfig defaults() {
        return new UserConfig(
                "com.example",
                "21",
                "jar",
                Architecture.MVC,
                ".",
                false,
                "idea",
                true,
                true,
                true
        );
    }
}