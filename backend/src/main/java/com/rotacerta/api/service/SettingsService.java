package com.rotacerta.api.service;

import com.rotacerta.api.dto.SettingsDtos;
import com.rotacerta.api.model.SystemSetting;
import com.rotacerta.api.repository.SystemSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class SettingsService {

    private final SystemSettingRepository repository;

    public SettingsService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SettingsDtos.Response> findAll() {
        return repository.findAllByOrderByCategoryAscLabelAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public SettingsDtos.Response update(String key, SettingsDtos.UpdateRequest request) {
        SystemSetting setting = repository.findBySettingKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuração não encontrada."));
        String value = request.value().trim();
        validateValue(setting.getValueType(), value);
        setting.updateValue(value);
        return toResponse(repository.save(setting));
    }

    private void validateValue(String type, String value) {
        try {
            switch (type.toUpperCase(Locale.ROOT)) {
                case "BOOLEAN" -> {
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        throw new IllegalArgumentException();
                    }
                }
                case "INTEGER" -> Integer.parseInt(value);
                case "DECIMAL" -> new BigDecimal(value);
                case "STRING" -> {
                    if (value.isBlank()) throw new IllegalArgumentException();
                }
                default -> throw new IllegalArgumentException();
            }
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor incompatível com o tipo da configuração.");
        }
    }

    private SettingsDtos.Response toResponse(SystemSetting setting) {
        return new SettingsDtos.Response(
                setting.getId(), setting.getSettingKey(), setting.getCategory(), setting.getLabel(),
                setting.getSettingValue(), setting.getValueType(), setting.getDescription(), setting.getUpdatedAt()
        );
    }
}
