package com.rotacerta.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "system_settings")
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(name = "setting_value", nullable = false, length = 500)
    private String settingValue;

    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType;

    @Column(length = 300)
    private String description;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SystemSetting() {}

    public Long getId() { return id; }
    public String getSettingKey() { return settingKey; }
    public String getCategory() { return category; }
    public String getLabel() { return label; }
    public String getSettingValue() { return settingValue; }
    public String getValueType() { return valueType; }
    public String getDescription() { return description; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void updateValue(String value) {
        this.settingValue = value;
        this.updatedAt = OffsetDateTime.now();
    }
}
