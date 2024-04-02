package com.github.novicezk.midjourney.bot.prompt;

import com.github.novicezk.midjourney.bot.model.*;
import com.github.novicezk.midjourney.bot.model.Character;

import java.util.List;
import java.util.Random;

public class PromptGenerator {
    private final static String DEFAULT_ASPECT_RATION = "Square";

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
        String basePrompt = dataProvider.getBasePrompt();
        Style style = getRandomStyle(dataProvider.getStyles());
        Character character = getRandomCharacter(dataProvider.getCharacters());
        Version version = getRandomVersion(dataProvider.getArguments().getVersions());
        String reference = getReferenceValue(style.getRef(), dataProvider.getRefs());
        String aspectRatio = getDefaultAspectRation(dataProvider.getArguments().getAspectRatio());

        StringBuilder promptBuilder = new StringBuilder();
        for (String imageUrl : imageUrls) {
            promptBuilder.append(imageUrl).append(" ");
        }

        promptBuilder.append(basePrompt).append(", ")
                .append(character.getDisplayName()).append(", ")
                .append("signature \"").append(username).append("\", ")
                .append(style.getValue()).append(", ")
                .append(aspectRatio).append(" ")
                .append(version.getValue()).append(" ")
                .append("--sref ").append(reference);

        GeneratedPromptData promptData = new GeneratedPromptData();
        promptData.setMessage(character.getDisplayName());
        promptData.setPrompt(promptBuilder.toString());
        return promptData;
    }

    private String getDefaultAspectRation(List<AspectRatio> aspectRatioList) {
        for (AspectRatio aspectRatio : aspectRatioList) {
            if (aspectRatio.getName().equalsIgnoreCase(DEFAULT_ASPECT_RATION)) {
                return aspectRatio.getValue();
            }
        }
        return "";
    }

    private Version getRandomVersion(List<Version> versions) {
        Random random = new Random();
        int index = random.nextInt(versions.size());
        return versions.get(index);
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

    private String getReferenceValue(String refName, List<Reference> refs) {
        for (Reference ref : refs) {
            if (ref.getName().equals(refName)) {
                return ref.getValue();
            }
        }
        return "";
    }
}