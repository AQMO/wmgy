package com.orchard.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** 全局配置：config_key -> config_value（如 DEFAULT_DEPOSIT 默认保证金） */
@Data
@Entity
@Table(name = "t_config")
public class Config {
    @Id
    private String configKey;

    private String configValue;
}
