package com.github.novicezk.midjourney.bot.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.bot.model.*;
import com.github.novicezk.midjourney.bot.model.Character;

import java.io.InputStream;
import java.util.List;

public class DataProvider {
    private static final String JSON_FILE_PATH = "prompt.json";

    private ObjectMapper objectMapper;
    private PromptData data;

    public DataProvider() {
        this.objectMapper = new ObjectMapper();
        loadData();
    }

    private void loadData() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_FILE_PATH)) {

            objectMapper = new ObjectMapper();
            data = objectMapper.readValue(in, PromptData.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        return data.getCharacters();
    }

    public Arguments getArguments() {
        return data.getArguments();
    }
}