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
     * @param imageUrls
     * @param user      is an optional, default is the Discord's name
     * @return
     */
    public GeneratedPromptData generatePrompt(List<String> imageUrls, User user) {
        CharacterStrength characterStrength = CharacterStrength.getRandomStrength();
        Character character = getRandomCharacter();
        Style defaultStyle = dataProvider.getDefaultStyle();
        CharacterClass characterClass = getRandomCharacterClass();

        String characterSref = formatListReferences(character.getSref());
        String styleSref = formatListReferences(defaultStyle.getSref());
        String characterCref = formatListReferences(character.getCref());
        String userCref = formatListReferences(imageUrls);

        String messageStyle = getMessageStyle(characterStrength, defaultStyle);
        String basePrompt = getBasePrompt(characterStrength, characterClass, defaultStyle);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(basePrompt)
                .append(" ").append(character.getPrompt())
                .append(" ").append("name ").append("\"").append(user.getGlobalName()).append("\" ")
                .append(" ").append("character ").append("\"").append(character.getDisplayName()).append("\" ");

        appendCharacterClass(characterStrength, characterClass, promptBuilder);

        promptBuilder.append(dataProvider.getDefaultAspectRatio())
                .append(" ").append(dataProvider.getDefaultVersion())
                .append(" ").append("--sref ").append(styleSref).append(characterSref)
                .append("--cref ").append(userCref).append(characterCref)
                .append("--cw ").append(characterStrength.getCW());

        StringBuilder messageBuilder = buildMessage(
                character,
                characterStrength,
                characterClass,
                messageStyle,
                promptBuilder.toString()
        );

        GeneratedPromptData promptData = new GeneratedPromptData();
        promptData.setPrompt(promptBuilder.toString());
        promptData.setMessage(messageBuilder.toString());

        return promptData;
    }

    private Character getRandomCharacter() {
        return dataProvider.getCharacters().get(new Random().nextInt(dataProvider.getCharacters().size()));
    }

    private CharacterClass getRandomCharacterClass() {
        return dataProvider.getCharacterClasses().get(new Random().nextInt(dataProvider.getCharacterClasses().size()));
    }

    private String getMessageStyle(CharacterStrength characterStrength, Style defaultStyle) {
        return (characterStrength == CharacterStrength.COMMON) ? defaultStyle.getDisplayName() : "Reference";
    }

    private String getBasePrompt(CharacterStrength characterStrength, CharacterClass characterClass, Style defaultStyle) {
        return (characterStrength == CharacterStrength.COMMON) ? defaultStyle.getPrompt() : characterClass.getPrompt();
    }

    private void appendCharacterClass(CharacterStrength characterStrength, CharacterClass characterClass, StringBuilder promptBuilder) {
        if (characterStrength != CharacterStrength.COMMON) {
            promptBuilder.append("class ").append("\"").append(characterClass.getName()).append("\" ");
        }
    }

    private StringBuilder buildMessage(
            Character character,
            CharacterStrength characterStrength,
            CharacterClass characterClass,
            String messageStyle,
            String prompt
    ) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder
                .append("**Generated Style:** ").append(messageStyle).append("\n")
                .append("**Character Reference:** ").append(character.getDisplayName()).append("\n")
                .append("**Seed:** s").append(seasonVersion).append(".").append("123\n");

        if (characterStrength != CharacterStrength.COMMON) {
            messageBuilder.append("\n**Rarity Level:** ").append(characterStrength.getStrengthName()).append("\n")
                    .append("**Class Name:** ").append(characterClass.getName()).append("\n")
                    .append("**Strength:** ").append(characterStrength.getStrengthEmoji()).append("\n");
        }

        return messageBuilder.append("\n").append("**prompt:**\n`").append(prompt).append("`");
    }

    private Character getRandomCharacter(List<Character> characters) {
        return characters.get(new Random().nextInt(characters.size()));
    }

    private CharacterClass getRandomCharacterClass(List<CharacterClass> classes) {
        return classes.get(new Random().nextInt(classes.size()));
    }

    private String formatListReferences(List<String> urls) {
        StringBuilder referencesBuilder = new StringBuilder();
        for (String imageUrl : urls) {
            referencesBuilder.append(imageUrl).append(" ");
        }

        return referencesBuilder.toString();
    }
}
