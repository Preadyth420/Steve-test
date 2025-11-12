package com.steve.ai.config;

import org.jetbrains.annotations.Nullable;

/**
 * Immutable snapshot of the AI configuration that should be used for a specific Steve instance.
 * <p>
 * The values are primarily sourced from the global {@link SteveConfig} (steve-common.toml) but can
 * be overridden by per-agent configuration files (e.g. {@code steve-planner.toml}).
 */
public class AgentConfig {
    private final String profileName;
    private final String sourceDescription;
    private final String provider;
    private final String openAiApiKey;
    private final String openAiModel;
    private final int maxTokens;
    private final double temperature;
    private final String localServerUrl;
    private final String localModel;
    private final String localApiKey;
    private final int localMaxTokens;
    private final double localTemperature;

    public AgentConfig(String profileName,
                       String sourceDescription,
                       String provider,
                       String openAiApiKey,
                       String openAiModel,
                       int maxTokens,
                       double temperature,
                       String localServerUrl,
                       String localModel,
                       String localApiKey,
                       int localMaxTokens,
                       double localTemperature) {
        this.profileName = profileName;
        this.sourceDescription = sourceDescription;
        this.provider = provider;
        this.openAiApiKey = openAiApiKey;
        this.openAiModel = openAiModel;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.localServerUrl = localServerUrl;
        this.localModel = localModel;
        this.localApiKey = localApiKey;
        this.localMaxTokens = localMaxTokens;
        this.localTemperature = localTemperature;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public String getProvider() {
        return provider;
    }

    public String getOpenAiApiKey() {
        return openAiApiKey;
    }

    public String getOpenAiModel() {
        return openAiModel;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getLocalServerUrl() {
        return localServerUrl;
    }

    public String getLocalModel() {
        return localModel;
    }

    public String getLocalApiKey() {
        return localApiKey;
    }

    public int getLocalMaxTokens() {
        return localMaxTokens;
    }

    public double getLocalTemperature() {
        return localTemperature;
    }

    /**
     * Convenience method for logging which file was attempted for this configuration. When {@code null},
     * the configuration was sourced entirely from the Forge config defaults.
     */
    @Nullable
    public String describeSourceFile() {
        return sourceDescription;
    }
}

