package com.springcli.web.dto;

import java.util.List;

public record ProjectOptions(
        List<String> springBootVersions,
        List<String> javaVersions,
        List<String> buildTools
) {}