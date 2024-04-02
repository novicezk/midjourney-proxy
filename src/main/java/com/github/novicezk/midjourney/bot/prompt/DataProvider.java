package com.github.novicezk.midjourney.bot.prompt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.bot.model.*;
import com.github.novicezk.midjourney.bot.model.Character;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DataProvider {
    private static final String JSON_PROMPT_PATH = "prompt.json";
    private static final String JSON_CHARACTERS_PATH = "characters.json";

    final private ObjectMapper objectMapper;
    private PromptData data;
    private List<Character> characters;

    public DataProvider() {
        this.objectMapper = new ObjectMapper();
        loadData();
    }

    private void loadData() {
        try {
            loadPromptData();
            loadCharacterData();
        } catch (IOException e) {
            throw new RuntimeException("Error loading data", e);
        }
    }

    private void loadPromptData() throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_PROMPT_PATH)) {
            data = objectMapper.readValue(in, PromptData.class);
        }
    }

    private void loadCharacterData() throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_CHARACTERS_PATH)) {
            characters = objectMapper.readValue(in, new TypeReference<>() {});
        }
    }

    public String getBasePrompt() {
        return data.getBasePrompt();
    }

    public List<Style> getStyles() {
        return data.getStyles();
    }

    public List<Reference> getRefs() {
        return data.getReferences();
    }

    public List<Character> getCharacters() {
        return characters;
    }

    public Arguments getArguments() {
        return data.getArguments();
    }
}