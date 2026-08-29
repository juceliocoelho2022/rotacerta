package com.rotacerta.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DriverPhotoStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Path driverUploadDirectory;

    public DriverPhotoStorageService(@Value("${app.upload-dir:uploads}") String uploadDirectory) {
        try {
            Path uploadRoot = Paths.get(uploadDirectory).toAbsolutePath().normalize();
            this.driverUploadDirectory = uploadRoot.resolve("drivers").normalize();
            Files.createDirectories(this.driverUploadDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível preparar o diretório de fotos dos motoristas.", exception);
        }
    }

    public String store(MultipartFile file) {
        validate(file);

        String extension = extensionFor(file.getContentType());
        String fileName = UUID.randomUUID() + extension;
        Path target = driverUploadDirectory.resolve(fileName).normalize();

        if (!target.startsWith(driverUploadDirectory)) {
            throw new IllegalArgumentException("Nome de arquivo inválido.");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/drivers/" + fileName;
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível salvar a foto do motorista.", exception);
        }
    }

    public void delete(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return;
        }

        String prefix = "/uploads/drivers/";
        if (!photoUrl.startsWith(prefix)) {
            return;
        }

        String fileName = photoUrl.substring(prefix.length());
        Path target = driverUploadDirectory.resolve(fileName).normalize();
        if (!target.startsWith(driverUploadDirectory)) {
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível remover a foto anterior do motorista.", exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Selecione uma imagem para o motorista.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("A foto deve ter no máximo 5 MB.");
        }

        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Formato inválido. Use JPG, PNG ou WebP.");
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType == null ? "" : contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Formato de imagem não suportado.");
        };
    }
}
