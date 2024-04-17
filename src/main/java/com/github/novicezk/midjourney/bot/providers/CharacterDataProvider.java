package com.github.novicezk.midjourney.bot.providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.github.novicezk.midjourney.bot.model.Character;

public class CharacterDataProvider {
    private static final String JSON_CHARACTERS_PATH = "data-generation/characters.json";
    private final ObjectMapper objectMapper;
    private List<Character> characters;

    public CharacterDataProvider() {
        this.objectMapper = new ObjectMapper();
        loadCharacterData();
    }

    private void loadCharacterData() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_CHARACTERS_PATH)) {
            characters = objectMapper.readValue(in, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Character> getCharacters() {
        return characters;
    }
}
