package com.github.novicezk.midjourney.bot.prompt;

import com.github.novicezk.midjourney.bot.model.*;
import com.github.novicezk.midjourney.bot.model.Character;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Random;

public class PromptGenerator {
    private final DataProvider dataProvider;
    private Dotenv config = Dotenv.configure().ignoreIfMissing().load();
    private final int seasonVersion;


    public PromptGenerator() {
        this.dataProvider = new DataProvider();
        this.seasonVersion = Integer.parseInt(config.get("SEASON_VERSION"));
    }

    /**
     *
     * @param imageUrls
     * @param user is an optional, default is the Discord's name
     * @return
     */
    public GeneratedPromptData generatePrompt(List<String> imageUrls, User user) {
        CharacterStrength characterStrength = CharacterStrength.getRandomStrength();
        Character character = getRandomCharacter(dataProvider.getCharacters());
        String aspectRatio = dataProvider.getDefaultAspectRatio();
        String version = dataProvider.getDefaultVersion();
        Style defaultStyle = dataProvider.getDefaultStyle();
        CharacterClass characterClass = getRandomCharacterClass(dataProvider.getCharacterClasses());

        String characterSref =  formatListReferences(character.getSref());
        String styleSref = formatListReferences(defaultStyle.getSref());
        String characterCref =  formatListReferences(character.getCref());
        String userCref = formatListReferences(imageUrls);

        String messageStyle;
        String basePrompt;

        if (characterStrength == CharacterStrength.COMMON) {
            basePrompt = defaultStyle.getPrompt();
            messageStyle = defaultStyle.getDisplayName();
        } else {
            basePrompt = characterClass.getPrompt();
            messageStyle = "Reference";
        }

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(basePrompt).append(" ")
                .append(character.getPrompt()).append(" ")
                .append("name ").append("\"").append(user.getGlobalName()).append("\" ")
                .append("character ").append("\"").append(character.getDisplayName()).append("\" ");

        if (characterStrength != CharacterStrength.COMMON) {
            promptBuilder.append("class ").append("\"").append(characterClass.getName()).append("\" ");
        }

        promptBuilder.append(aspectRatio).append(" ")
                .append(version).append(" ")
                .append("--sref ").append(styleSref).append(characterSref)
                .append("--cref ").append(userCref).append(characterCref)
                .append("--cw ").append(characterStrength.getCW());

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("**Generated Style:** ").append(messageStyle).append("\n")
                .append("**Character Name:** ").append(character.getDisplayName()).append("\n");

        if (characterStrength != CharacterStrength.COMMON) {
            messageBuilder.append("**Class Name:** ").append(characterClass.getName()).append("\n");
            messageBuilder.append("**Rarity Level:** ").append(characterStrength.getStrengthName()).append("\n");
            messageBuilder.append("**Strength:** ").append(characterStrength.getStrengthEmoji()).append("\n");
        }

        messageBuilder.append("**Version:** v").append(seasonVersion).append(".").append("123").append("\n\n")
                .append("**prompt:**\n`").append(promptBuilder).append("`");

        GeneratedPromptData promptData = new GeneratedPromptData();
        promptData.setPrompt(promptBuilder.toString());
        promptData.setMessage(messageBuilder.toString());

        return promptData;
    }

    private Character getRandomCharacter(List<Character> characters) {
        Random random = new Random();
        int index = random.nextInt(characters.size());
        return characters.get(index);
    }

    private CharacterClass getRandomCharacterClass(List<CharacterClass> classes) {
        Random random = new Random();
        int index = random.nextInt(classes.size());
        return classes.get(index);
    }

    private String formatListReferences(List<String> urls) {
        StringBuilder referencesBuilder = new StringBuilder();
        for (String imageUrl: urls) {
            referencesBuilder.append(imageUrl).append(" ");
        }

        return referencesBuilder.toString();
    }
}
