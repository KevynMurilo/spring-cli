package com.springcli.model;

import java.util.List;

public record DependencyGroup(
    String name,
    List<Dependency> dependencies
) {}
