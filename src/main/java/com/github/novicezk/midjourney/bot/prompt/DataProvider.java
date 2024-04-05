package com.github.novicezk.midjourney.bot.prompt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.bot.model.*;
import com.github.novicezk.midjourney.bot.model.Character;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DataProvider {
    private static final String JSON_PROMPT_PATH = "data-generation/arguments.json";
    private static final String JSON_CHARACTERS_PATH = "data-generation/characters.json";
    private static final String JSON_STYLES_PATH = "data-generation/styles.json";
    private static final String JSON_CLASSES_PATH = "data-generation/classes.json";

    private final static String DEFAULT_ASPECT_RATION = "Square";
    private final static String DEFAULT_VERSION = "Realistic";
    private final static String DEFAULT_STYLE = "concept_art";

    final private ObjectMapper objectMapper;
    private Arguments arguments;
    private List<Character> characters;
    private List<Style> styles;
    private List<CharacterClass> classes;

    public DataProvider() {
        this.objectMapper = new ObjectMapper();
        loadData();
    }

    private void loadData() {
        try {
            loadPromptData();
            loadCharacterData();
            loadStylesData();
            loadClassesData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadPromptData() throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_PROMPT_PATH)) {
            arguments = objectMapper.readValue(in, Arguments.class);
        }
    }

    private void loadCharacterData() throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_CHARACTERS_PATH)) {
            characters = objectMapper.readValue(in, new TypeReference<>() {
            });
        }
    }

    private void loadStylesData() throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_STYLES_PATH)) {
            styles = objectMapper.readValue(in, new TypeReference<>() {
            });
        }
    }

    private void loadClassesData() throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_CLASSES_PATH)) {
            classes = objectMapper.readValue(in, new TypeReference<>() {
            });
        }
    }

    public Style getDefaultStyle() {
        for (Style style: styles) {
            if (style.getName().equalsIgnoreCase(DEFAULT_STYLE)) {
                return style;
            }
        }

        return new Style();
    }

    public List<Character> getCharacters() {
        return characters;
    }

    public List<CharacterClass> getCharacterClasses() {
        return classes;
    }

    public String getDefaultVersion() {
        for (Version version : arguments.getVersions()) {
            if (version.getName().equalsIgnoreCase(DEFAULT_VERSION)) {
                return version.getValue();
            }
        }
        return "";
    }

    public String getDefaultAspectRatio() {
        for (AspectRatio aspectRatio : arguments.getAspectRatio()) {
            if (aspectRatio.getName().equalsIgnoreCase(DEFAULT_ASPECT_RATION)) {
                return aspectRatio.getValue();
            }
        }
        return "";
    }
}
