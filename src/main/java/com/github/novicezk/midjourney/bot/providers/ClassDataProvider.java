package com.github.novicezk.midjourney.bot.providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.bot.model.CharacterClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ClassDataProvider {
    private static final String JSON_CLASSES_PATH = "data-generation/classes.json";
    private final ObjectMapper objectMapper;
    private List<CharacterClass> classes;

    public ClassDataProvider() {
        this.objectMapper = new ObjectMapper();
        loadClassesData();
    }

    private void loadClassesData() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_CLASSES_PATH)) {
            classes = objectMapper.readValue(in, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CharacterClass> getCharacterClasses() {
        return classes;
    }
}
