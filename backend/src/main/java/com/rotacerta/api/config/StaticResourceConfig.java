package com.rotacerta.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final Path uploadRoot;

    public StaticResourceConfig(@Value("${app.upload-dir:uploads}") String uploadDirectory) {
        try {
            this.uploadRoot = Paths.get(uploadDirectory).toAbsolutePath().normalize();
            Files.createDirectories(this.uploadRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível preparar o diretório público de uploads.", exception);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadRoot.toUri().toString());
    }
}
