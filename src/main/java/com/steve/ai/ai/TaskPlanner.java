package com.steve.ai.ai;

import com.steve.ai.SteveMod;
import com.steve.ai.action.Task;
import com.steve.ai.config.AgentConfig;
import com.steve.ai.entity.SteveEntity;
import com.steve.ai.memory.WorldKnowledge;

import java.util.List;

public class TaskPlanner {
    private final OpenAIClient openAIClient;
    private final GeminiClient geminiClient;
    private final GroqClient groqClient;
    private final LocalLLMClient localLLMClient;
    private final AgentConfig agentConfig;

    public TaskPlanner(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        this.openAIClient = new OpenAIClient();
        this.geminiClient = new GeminiClient();
        this.groqClient = new GroqClient();
        this.localLLMClient = new LocalLLMClient();
    }

    public ResponseParser.ParsedResponse planTasks(SteveEntity steve, String command) {
        try {
            String systemPrompt = PromptBuilder.buildSystemPrompt();
            WorldKnowledge worldKnowledge = new WorldKnowledge(steve);
            String userPrompt = PromptBuilder.buildUserPrompt(steve, command, worldKnowledge);

            String provider = agentConfig.getProvider().toLowerCase();
            SteveMod.LOGGER.info(
                "Requesting AI plan for Steve '{}' using {} profile '{}' (source: {}) for command {}",
                steve.getSteveName(),
                provider,
                agentConfig.getProfileName(),
                agentConfig.describeSourceFile(),
                command
            );

            String response = getAIResponse(provider, systemPrompt, userPrompt);
            
            if (response == null) {
                SteveMod.LOGGER.error("Failed to get AI response for command: {}", command);
                return null;
            }

            ResponseParser.ParsedResponse parsedResponse = ResponseParser.parseAIResponse(response);
            
            if (parsedResponse == null) {
                SteveMod.LOGGER.error("Failed to parse AI response");
                return null;
            }
            
            SteveMod.LOGGER.info("Plan: {} ({} tasks)", parsedResponse.getPlan(), parsedResponse.getTasks().size());
            
            return parsedResponse;
            
        } catch (Exception e) {
            SteveMod.LOGGER.error("Error planning tasks", e);
            return null;
        }
    }

    private String getAIResponse(String provider, String systemPrompt, String userPrompt) {
        String response = switch (provider) {
            case "groq" -> groqClient.sendRequest(agentConfig, systemPrompt, userPrompt);
            case "gemini" -> geminiClient.sendRequest(agentConfig, systemPrompt, userPrompt);
            case "openai" -> openAIClient.sendRequest(agentConfig, systemPrompt, userPrompt);
            case "local" -> localLLMClient.sendRequest(agentConfig, systemPrompt, userPrompt);
            default -> {
                SteveMod.LOGGER.warn("Unknown AI provider '{}' (supported: groq, openai, gemini, local), using Groq", provider);
                yield groqClient.sendRequest(agentConfig, systemPrompt, userPrompt);
            }
        };

        if (response == null && !provider.equals("groq")) {
            SteveMod.LOGGER.warn("{} failed, trying Groq as fallback", provider);
            response = groqClient.sendRequest(agentConfig, systemPrompt, userPrompt);
        }

        return response;
    }

    public boolean validateTask(Task task) {
        String action = task.getAction();
        
        return switch (action) {
            case "pathfind" -> task.hasParameters("x", "y", "z");
            case "mine" -> task.hasParameters("block", "quantity");
            case "place" -> task.hasParameters("block", "x", "y", "z");
            case "craft" -> task.hasParameters("item", "quantity");
            case "attack" -> task.hasParameters("target");
            case "follow" -> task.hasParameters("player");
            case "gather" -> task.hasParameters("resource", "quantity");
            case "build" -> task.hasParameters("structure", "blocks", "dimensions");
            default -> {
                SteveMod.LOGGER.warn("Unknown action type: {}", action);
                yield false;
            }
        };
    }

    public List<Task> validateAndFilterTasks(List<Task> tasks) {
        return tasks.stream()
            .filter(this::validateTask)
            .toList();
    }
}

