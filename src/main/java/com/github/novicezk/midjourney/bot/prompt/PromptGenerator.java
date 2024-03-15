package com.github.novicezk.midjourney.bot.prompt;

import com.github.novicezk.midjourney.bot.model.Character;
import com.github.novicezk.midjourney.bot.model.Reference;
import com.github.novicezk.midjourney.bot.model.Style;

import java.util.List;
import java.util.Random;

public class PromptGenerator {
    private final DataProvider dataProvider;

    public PromptGenerator(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public String generatePrompt(List<String> imageUrls) {
        String basePrompt = dataProvider.getBasePrompt();
        Style style = getRandomStyle(dataProvider.getStyles());
        Character character = getRandomCharacter(dataProvider.getCharacters());
        String reference = getReferenceValue(style.getRef());

        StringBuilder promptBuilder = new StringBuilder();
        for (String imageUrl : imageUrls) {
            promptBuilder.append(imageUrl).append(" ");
        }

        promptBuilder.append(basePrompt).append(", ")
                .append(character.getValue()).append(", ")
                .append(style.getValue()).append(", ")
                .append("--sref ").append(reference);

        return promptBuilder.toString();
    }

    private Style getRandomStyle(List<Style> styles) {
        Random random = new Random();
        int index = random.nextInt(styles.size());
        return styles.get(index);
    }

    private Character getRandomCharacter(List<Character> characters) {
        Random random = new Random();
        int index = random.nextInt(characters.size());
        return characters.get(index);
    }

    private String getReferenceValue(String refName) {
        List<Reference> refs = dataProvider.getRefs();
        for (Reference ref : refs) {
            if (ref.getName().equals(refName)) {
                return ref.getValue();
            }
        }
        return "";
    }
}