package com.github.novicezk.midjourney.bot.prompt;

import com.github.novicezk.midjourney.bot.model.*;
import com.github.novicezk.midjourney.bot.model.Character;

import java.util.List;
import java.util.Random;

public class PromptGenerator {
    private final DataProvider dataProvider;

    public PromptGenerator() {
        this.dataProvider = new DataProvider();
    }

    /**
     *
     * @param imageUrls
     * @param username is an optional, default is the Discord's name
     * @return
     */
    public GeneratedPromptData generatePrompt(List<String> imageUrls, String username) {
        CharacterStrength characterStrength = CharacterStrength.getRandomStrength();
        Character character = getRandomCharacter(dataProvider.getCharacters());
        String aspectRatio = dataProvider.getDefaultAspectRatio();
        String version = dataProvider.getDefaultVersion();
        Style style = dataProvider.getDefaultStyle();

        String characterSref =  formatListReferences(character.getSref());
        String styleSref = formatListReferences(style.getSref());
        String characterCref =  formatListReferences(character.getCref());
        String userCref = formatListReferences(imageUrls);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(style.getPrompt()).append(" ")
                .append(character.getPrompt()).append(" ")
                .append("name ").append("\"").append(username).append("\" ")
                .append("character").append("\"").append(character.getDisplayName()).append("\" ")
                .append(aspectRatio).append(" ")
                .append(version).append(" ")
                .append("--sref ").append(styleSref).append(characterSref)
                .append("--cref ").append(userCref).append(characterCref)
                .append("--cw ").append(characterStrength.getCW());

        GeneratedPromptData promptData = new GeneratedPromptData();
        promptData.setPrompt(promptBuilder.toString());
        promptData.setStyle(style.getDisplayName());
        promptData.setCharacter(character.getDisplayName());
        promptData.setCharacterStrength(characterStrength);

        return promptData;
    }

    private Character getRandomCharacter(List<Character> characters) {
        Random random = new Random();
        int index = random.nextInt(characters.size());
        return characters.get(index);
    }

    private String formatListReferences(List<String> urls) {
        StringBuilder referencesBuilder = new StringBuilder();
        for (String imageUrl: urls) {
            referencesBuilder.append(imageUrl).append(" ");
        }

        return referencesBuilder.toString();
    }
}