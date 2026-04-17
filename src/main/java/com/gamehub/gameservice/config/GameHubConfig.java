package com.gamehub.gameservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@ConfigurationProperties(prefix = "gamehub")
public class GameHubConfig {

    private String environment = "default";
    private boolean salesEnabled = false;

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public boolean isSalesEnabled() { return salesEnabled; }
    public void setSalesEnabled(boolean salesEnabled) { this.salesEnabled = salesEnabled; }
}
