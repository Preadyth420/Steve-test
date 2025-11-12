package com.steve.ai.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.steve.ai.SteveMod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Helper for resolving the effective configuration that should be applied to a Steve instance.
 */
public final class AgentConfigLoader {
    private static final String DEFAULT_PROFILE = "common";
    private static final String FILE_PREFIX = "steve-";
    private static final String FILE_EXTENSION = ".toml";

    private AgentConfigLoader() {
    }

    public static AgentConfig loadForAgent(String agentName, @Nullable String explicitProfile) {
        String profileCandidate = (explicitProfile != null && !explicitProfile.isBlank())
            ? explicitProfile
            : agentName;

        String sanitizedProfile = sanitizeProfile(profileCandidate);
        if (sanitizedProfile.isEmpty()) {
            sanitizedProfile = DEFAULT_PROFILE;
        }

        Path configDir = FMLPaths.CONFIGDIR.get();
        Path profilePath = configDir.resolve(FILE_PREFIX + sanitizedProfile + FILE_EXTENSION);

        AgentConfig defaultConfig = loadDefault();

        if (Files.exists(profilePath)) {
            return loadFromFile(profilePath, sanitizedProfile);
        }

        if (explicitProfile != null && !explicitProfile.isBlank()) {
            SteveMod.LOGGER.warn("Requested config profile '{}' for Steve but file {} does not exist. Falling back to {}.",
                explicitProfile, profilePath.getFileName(), defaultConfig.describeSourceFile());
        } else if (!sanitizedProfile.equals(DEFAULT_PROFILE)) {
            SteveMod.LOGGER.info("No config file {} found for Steve '{}', using default settings.",
                profilePath.getFileName(), agentName);
        }

        return new AgentConfig(
            sanitizedProfile,
            defaultConfig.describeSourceFile(),
            defaultConfig.getProvider(),
            defaultConfig.getOpenAiApiKey(),
            defaultConfig.getOpenAiModel(),
            defaultConfig.getMaxTokens(),
            defaultConfig.getTemperature(),
            defaultConfig.getLocalServerUrl(),
            defaultConfig.getLocalModel(),
            defaultConfig.getLocalApiKey(),
            defaultConfig.getLocalMaxTokens(),
            defaultConfig.getLocalTemperature()
        );
    }

    public static AgentConfig loadForProfile(String profileName) {
        if (profileName == null || profileName.isBlank()) {
            return loadDefault();
        }

        String sanitizedProfile = sanitizeProfile(profileName);
        if (sanitizedProfile.isEmpty()) {
            return loadDefault();
        }

        Path configDir = FMLPaths.CONFIGDIR.get();
        Path profilePath = configDir.resolve(FILE_PREFIX + sanitizedProfile + FILE_EXTENSION);

        if (Files.exists(profilePath)) {
            return loadFromFile(profilePath, sanitizedProfile);
        }

        AgentConfig defaultConfig = loadDefault();
        return new AgentConfig(
            sanitizedProfile,
            defaultConfig.describeSourceFile(),
            defaultConfig.getProvider(),
            defaultConfig.getOpenAiApiKey(),
            defaultConfig.getOpenAiModel(),
            defaultConfig.getMaxTokens(),
            defaultConfig.getTemperature(),
            defaultConfig.getLocalServerUrl(),
            defaultConfig.getLocalModel(),
            defaultConfig.getLocalApiKey(),
            defaultConfig.getLocalMaxTokens(),
            defaultConfig.getLocalTemperature()
        );
    }

    public static AgentConfig loadDefault() {
        return new AgentConfig(
            DEFAULT_PROFILE,
            FILE_PREFIX + DEFAULT_PROFILE + FILE_EXTENSION,
            SteveConfig.AI_PROVIDER.get(),
            SteveConfig.OPENAI_API_KEY.get(),
            SteveConfig.OPENAI_MODEL.get(),
            SteveConfig.MAX_TOKENS.get(),
            SteveConfig.TEMPERATURE.get(),
            SteveConfig.LOCAL_SERVER_URL.get(),
            SteveConfig.LOCAL_MODEL.get(),
            SteveConfig.LOCAL_API_KEY.get(),
            SteveConfig.LOCAL_MAX_TOKENS.get(),
            SteveConfig.LOCAL_TEMPERATURE.get()
        );
    }

    private static AgentConfig loadFromFile(Path profilePath, String sanitizedProfile) {
        CommentedFileConfig config = CommentedFileConfig.builder(profilePath).autosave().sync().build();
        config.load();

        try {
            String provider = getString(config, "ai.provider", SteveConfig.AI_PROVIDER.get());
            String openAiKey = getString(config, "openai.apiKey", SteveConfig.OPENAI_API_KEY.get());
            String openAiModel = getString(config, "openai.model", SteveConfig.OPENAI_MODEL.get());
            int maxTokens = getInt(config, "openai.maxTokens", SteveConfig.MAX_TOKENS.get());
            double temperature = getDouble(config, "openai.temperature", SteveConfig.TEMPERATURE.get());

            String localServerUrl = getString(config, "local.serverUrl", SteveConfig.LOCAL_SERVER_URL.get());
            String localModel = getString(config, "local.model", SteveConfig.LOCAL_MODEL.get());
            String localApiKey = getString(config, "local.apiKey", SteveConfig.LOCAL_API_KEY.get());
            int localMaxTokens = getInt(config, "local.maxTokens", SteveConfig.LOCAL_MAX_TOKENS.get());
            double localTemperature = getDouble(config, "local.temperature", SteveConfig.LOCAL_TEMPERATURE.get());

            return new AgentConfig(
                sanitizedProfile,
                profilePath.getFileName().toString(),
                provider,
                openAiKey,
                openAiModel,
                maxTokens,
                temperature,
                localServerUrl,
                localModel,
                localApiKey,
                localMaxTokens,
                localTemperature
            );
        } finally {
            config.close();
        }
    }

    private static String sanitizeProfile(String input) {
        String normalized = input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("-+", "-");
        if (normalized.startsWith("-")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("-")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.trim();
    }

    private static String getString(CommentedFileConfig config, String path, String defaultValue) {
        Object value = config.get(path);
        return value instanceof String ? (String) value : defaultValue;
    }

    private static int getInt(CommentedFileConfig config, String path, int defaultValue) {
        Object value = config.get(path);
        return value instanceof Number ? ((Number) value).intValue() : defaultValue;
    }

    private static double getDouble(CommentedFileConfig config, String path, double defaultValue) {
        Object value = config.get(path);
        return value instanceof Number ? ((Number) value).doubleValue() : defaultValue;
    }
}

