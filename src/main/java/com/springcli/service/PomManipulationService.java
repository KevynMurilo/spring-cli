package com.springcli.service;

import com.springcli.model.ProjectFeatures;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PomManipulationService {

    private static final String JJWT_VERSION = "0.11.5";
    private static final String SPRINGDOC_VERSION = "2.3.0";
    private static final String MAPSTRUCT_VERSION = "1.5.5.Final";

    public String injectDependencies(String pomContent, ProjectFeatures features) {
        StringBuilder result = new StringBuilder(pomContent);

        int dependenciesEnd = findDependenciesEndTag(pomContent);
        if (dependenciesEnd == -1) {
            log.warn("Could not find </dependencies> tag in pom.xml");
            return pomContent;
        }

        StringBuilder injections = new StringBuilder();

        if (features.enableJwt()) {
            injections.append(getJwtDependencies());
        }

        if (features.enableSwagger()) {
            injections.append(getSwaggerDependency());
        }

        if (features.enableMapStruct()) {
            injections.append(getMapStructDependency());
        }

        if (injections.length() > 0) {
            result.insert(dependenciesEnd, injections.toString());
        }

        if (features.enableMapStruct()) {
            result = new StringBuilder(injectMapStructProcessor(result.toString()));
        }

        return result.toString();
    }

    private int findDependenciesEndTag(String pom) {
        return pom.indexOf("</dependencies>");
    }

    private String getJwtDependencies() {
        return """

                    <!-- JWT Dependencies -->
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-api</artifactId>
                        <version>%s</version>
                    </dependency>
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-impl</artifactId>
                        <version>%s</version>
                        <scope>runtime</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-jackson</artifactId>
                        <version>%s</version>
                        <scope>runtime</scope>
                    </dependency>
                """.formatted(JJWT_VERSION, JJWT_VERSION, JJWT_VERSION);
    }

    private String getSwaggerDependency() {
        return """

                    <!-- Swagger/OpenAPI -->
                    <dependency>
                        <groupId>org.springdoc</groupId>
                        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                        <version>%s</version>
                    </dependency>
                """.formatted(SPRINGDOC_VERSION);
    }

    private String getMapStructDependency() {
        return """

                    <!-- MapStruct -->
                    <dependency>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct</artifactId>
                        <version>%s</version>
                    </dependency>
                """.formatted(MAPSTRUCT_VERSION);
    }

    private String injectMapStructProcessor(String pomContent) {
        String processorPath = """
                                    <path>
                                        <groupId>org.mapstruct</groupId>
                                        <artifactId>mapstruct-processor</artifactId>
                                        <version>%s</version>
                                    </path>
                """.formatted(MAPSTRUCT_VERSION);

        int processorPathsEnd = pomContent.indexOf("</annotationProcessorPaths>");
        if (processorPathsEnd != -1) {
            return pomContent.substring(0, processorPathsEnd) + processorPath + pomContent.substring(processorPathsEnd);
        }

        return pomContent;
    }
}
