package com.springcli.web.dto;

public record UserConfigDto(
        String defaultGroupId,
        String defaultJavaVersion,
        String defaultPackaging,
        String defaultArchitecture,
        String defaultOutputDir,
        boolean autoOpenIde,
        String preferredIde,
        boolean useApplicationYml,
        boolean generateReadme,
        boolean generateGitignore
) {}
