package com.steve.ai.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.steve.ai.SteveMod;
import com.steve.ai.config.AgentConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client for running requests against a locally hosted OpenAI compatible server
 * (for example llama.cpp started with the {@code --api} flag or LM Studio).
 *
 * The server is expected to expose the chat completions endpoint that accepts
 * OpenAI-style payloads. Configuration is supplied through the Forge config file
 * under the {@code local} section.
 */
public class LocalLLMClient {
    private final HttpClient client;

    public LocalLLMClient() {
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    public String sendRequest(AgentConfig agentConfig, String systemPrompt, String userPrompt) {
        String serverUrl = agentConfig.getLocalServerUrl();
        if (serverUrl == null || serverUrl.isEmpty()) {
            SteveMod.LOGGER.error("Local LLM server URL is not configured. Set 'local.serverUrl' in the config file.");
            return null;
        }

        JsonObject requestBody = buildRequestBody(agentConfig, systemPrompt, userPrompt);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(serverUrl))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));

        String apiKey = agentConfig.getLocalApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            }

            SteveMod.LOGGER.error("Local LLM request failed: {}", response.statusCode());
            SteveMod.LOGGER.error("Response body: {}", response.body());
            return null;
        } catch (Exception e) {
            SteveMod.LOGGER.error("Error communicating with local LLM server", e);
            return null;
        }
    }

    private JsonObject buildRequestBody(AgentConfig agentConfig, String systemPrompt, String userPrompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", agentConfig.getLocalModel());
        body.addProperty("temperature", agentConfig.getLocalTemperature());
        body.addProperty("max_tokens", agentConfig.getLocalMaxTokens());

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        messages.add(userMessage);

        body.add("messages", messages);

        return body;
    }

    private String parseResponse(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            if (json.has("choices") && json.getAsJsonArray("choices").size() > 0) {
                JsonObject firstChoice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
                if (firstChoice.has("message")) {
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    if (message.has("content")) {
                        return message.get("content").getAsString();
                    }
                }
            }

            SteveMod.LOGGER.error("Unexpected local LLM response format: {}", responseBody);
            return null;
        } catch (Exception e) {
            SteveMod.LOGGER.error("Error parsing local LLM response", e);
            return null;
        }
    }
}

