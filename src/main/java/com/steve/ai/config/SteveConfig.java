package com.steve.ai.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SteveConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> AI_PROVIDER;
    public static final ForgeConfigSpec.ConfigValue<String> OPENAI_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> OPENAI_MODEL;
    public static final ForgeConfigSpec.IntValue MAX_TOKENS;
    public static final ForgeConfigSpec.DoubleValue TEMPERATURE;
    public static final ForgeConfigSpec.ConfigValue<String> LOCAL_SERVER_URL;
    public static final ForgeConfigSpec.ConfigValue<String> LOCAL_MODEL;
    public static final ForgeConfigSpec.ConfigValue<String> LOCAL_API_KEY;
    public static final ForgeConfigSpec.IntValue LOCAL_MAX_TOKENS;
    public static final ForgeConfigSpec.DoubleValue LOCAL_TEMPERATURE;
    public static final ForgeConfigSpec.IntValue ACTION_TICK_DELAY;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CHAT_RESPONSES;
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_STEVES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("AI API Configuration").push("ai");

        AI_PROVIDER = builder
            .comment("AI provider to use: 'groq' (FASTEST, FREE), 'openai', 'gemini', or 'local'")
            .define("provider", "groq");

        builder.pop();

        builder.comment("OpenAI/Gemini/Groq API Configuration (same key field used for all)").push("openai");
        
        OPENAI_API_KEY = builder
            .comment("Your OpenAI API key (required)")
            .define("apiKey", "");
        
        OPENAI_MODEL = builder
            .comment("OpenAI model to use (gpt-4, gpt-4-turbo-preview, gpt-3.5-turbo)")
            .define("model", "gpt-4-turbo-preview");
        
        MAX_TOKENS = builder
            .comment("Maximum tokens per API request")
            .defineInRange("maxTokens", 8000, 100, 65536);
        
        TEMPERATURE = builder
            .comment("Temperature for AI responses (0.0-2.0, lower is more deterministic)")
            .defineInRange("temperature", 0.7, 0.0, 2.0);
        
        builder.pop();

        builder.pop();

        builder.comment("Local GGUF LLM configuration (OpenAI compatible server, e.g. llama.cpp --api)").push("local");

        LOCAL_SERVER_URL = builder
            .comment("Base URL for the local inference server chat completions endpoint")
            .define("serverUrl", "http://localhost:8080/v1/chat/completions");

        LOCAL_MODEL = builder
            .comment("Model identifier or file name as expected by your server")
            .define("model", "model.gguf");

        LOCAL_API_KEY = builder
            .comment("Optional auth token for the local server (leave blank if not required)")
            .define("apiKey", "");

        LOCAL_MAX_TOKENS = builder
            .comment("Maximum tokens to request from the local model")
            .defineInRange("maxTokens", 512, 64, 8192);

        LOCAL_TEMPERATURE = builder
            .comment("Sampling temperature used for local completions")
            .defineInRange("temperature", 0.7, 0.0, 2.0);

        builder.pop();

        builder.comment("Steve Behavior Configuration").push("behavior");
        
        ACTION_TICK_DELAY = builder
            .comment("Ticks between action checks (20 ticks = 1 second)")
            .defineInRange("actionTickDelay", 20, 1, 100);
        
        ENABLE_CHAT_RESPONSES = builder
            .comment("Allow Steves to respond in chat")
            .define("enableChatResponses", true);
        
        MAX_ACTIVE_STEVES = builder
            .comment("Maximum number of Steves that can be active simultaneously")
            .defineInRange("maxActiveSteves", 10, 1, 50);
        
        builder.pop();

        SPEC = builder.build();
    }
}

