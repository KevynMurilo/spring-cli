package com.springcli.service;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import com.springcli.model.TemplateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TemplateService {

    private final PebbleEngine pebbleEngine;

    public TemplateService() {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates");
        loader.setSuffix(".peb");

        this.pebbleEngine = new PebbleEngine.Builder()
                .loader(loader)
                .strictVariables(false)
                .build();
    }

    public String renderTemplate(String templatePath, TemplateContext context) {
        try {
            Map<String, Object> templateContext = buildTemplateContext(context);
            if (templatePath.startsWith("/")) {
                templatePath = templatePath.substring(1);
            }

            PebbleTemplate template = pebbleEngine.getTemplate(templatePath);
            Writer writer = new StringWriter();
            template.evaluate(writer, templateContext);

            return writer.toString();

        } catch (IOException e) {
            log.error("Failed to render template: {}", templatePath, e);
            throw new RuntimeException("Failed to render template: " + templatePath, e);
        }
    }

    public String renderJavaClass(String templateName, TemplateContext context) {
        return renderTemplate("java/" + templateName, context);
    }

    public String renderConfig(String templateName, TemplateContext context) {
        return renderTemplate("config/" + templateName, context);
    }

    public String renderOps(String templateName, TemplateContext context) {
        return renderTemplate("ops/" + templateName, context);
    }

    private Map<String, Object> buildTemplateContext(TemplateContext context) {
        Map<String, Object> map = new HashMap<>();

        map.put("packageName", context.packageName());
        map.put("basePackage", context.basePackage());
        map.put("projectName", context.projectName());
        map.put("entityName", context.entityName());
        map.put("architecture", context.architecture() != null ? context.architecture().name() : "");
        map.put("javaVersion", context.javaVersion());
        map.put("buildTool", context.buildTool());

        if (context.features() != null) {
            map.put("enableJwt", context.features().enableJwt());
            map.put("enableSwagger", context.features().enableSwagger());
            map.put("enableCors", context.features().enableCors());
            map.put("enableExceptionHandler", context.features().enableExceptionHandler());
            map.put("enableMapStruct", context.features().enableMapStruct());
            map.put("enableDocker", context.features().enableDocker());
            map.put("enableKubernetes", context.features().enableKubernetes());
            map.put("enableCiCd", context.features().enableCiCd());
            map.put("enableAudit", context.features().enableAudit());
            map.put("features", context.features());
        }

        if (context.additionalProperties() != null) {
            map.putAll(context.additionalProperties());
            map.put("additionalProperties", context.additionalProperties());
        }

        return map;
    }
}