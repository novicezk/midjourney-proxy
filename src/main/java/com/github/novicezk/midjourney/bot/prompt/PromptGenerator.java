package com.github.novicezk.midjourney.bot.prompt;

import com.github.novicezk.midjourney.bot.model.*;
import com.github.novicezk.midjourney.bot.model.Character;
import com.github.novicezk.midjourney.bot.utils.SeasonTracker;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

@Slf4j
public class PromptGenerator {
    private final DataProvider dataProvider;

    public PromptGenerator() {
        this.dataProvider = new DataProvider();
    }

    /**
     * @param imageUrls
     * @param user is an optional, default is the Discord's name
     * @return
     */
    public GeneratedPromptData generatePrompt(List<String> imageUrls, User user) {
        CharacterStrength characterStrength = CharacterStrength.getRandomStrength();
        Character character = getRandomCharacter();
        Style style = dataProvider.getStyleByStrength(characterStrength);
        CharacterClass characterClass = getRandomCharacterClass();

        String characterSref = formatListReferences(character.getSref());
        String characterCref = getRandomFromListReferences(character.getCref());
        String userCref = getRandomFromListReferences(imageUrls);

        String basePrompt = getBasePrompt(characterStrength, characterClass, style);
        String styleSref = getRandomFromListReferences(style.getSref());

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(basePrompt)
                .append(" ").append(character.getPrompt())
                .append(" ").append("name: ").append("\"").append(user.getGlobalName()).append("\", ")
                .append(" ").append("character: ").append("\"").append(character.getDisplayName()).append("\", ");

        appendCharacterClass(characterStrength, characterClass, promptBuilder);

        promptBuilder.append("signature: ").append("\"AVIS s").append(SeasonTracker.getCurrentSeasonVersion())
                .append(".").append(SeasonTracker.getCurrentGenerationCount()).append("\"")
                .append(" ").append(getAspectRation(characterStrength, style.getAspectRatio()))
                .append(" ").append(dataProvider.getDefaultVersion())
                .append(" ").append("--sref ").append(styleSref).append(characterSref)
                .append(" ").append("--cref ").append(userCref).append(characterCref)
                .append(" ").append("--cw ").append(characterStrength.getCW());

        StringBuilder messageBuilder = buildMessage(
                character,
                characterStrength,
                characterClass,
                style.getDisplayName(),
                promptBuilder.toString()
        );

        GeneratedPromptData promptData = new GeneratedPromptData();
        promptData.setPrompt(promptBuilder.toString());
        promptData.setMessage(messageBuilder.toString());

        return promptData;
    }

    private String getAspectRation(CharacterStrength characterStrength, @Nullable List<AspectRatio> aspectRatio) {
        if (characterStrength != CharacterStrength.COMMON && aspectRatio != null) {
            return aspectRatio.get(new Random().nextInt(aspectRatio.size())).getValue();
        }

        return dataProvider.getDefaultAspectRatio();
    }

    private Character getRandomCharacter() {
        return dataProvider.getCharacters().get(new Random().nextInt(dataProvider.getCharacters().size()));
    }

    private CharacterClass getRandomCharacterClass() {
        return dataProvider.getCharacterClasses().get(new Random().nextInt(dataProvider.getCharacterClasses().size()));
    }

    private String getBasePrompt(CharacterStrength characterStrength, CharacterClass characterClass, Style style) {
        return (characterStrength == CharacterStrength.COMMON)
                ? style.getPrompt() : style.getPrompt() + " " + characterClass.getPrompt();
    }

    private void appendCharacterClass(CharacterStrength characterStrength, CharacterClass characterClass, StringBuilder promptBuilder) {
        if (characterStrength != CharacterStrength.COMMON) {
            promptBuilder.append("class: ").append("\"").append(characterClass.getName()).append("\", ");
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
                .append("**Reference Name:** ").append(character.getDisplayName()).append("\n")
                .append("**Generated Style:** ").append(messageStyle).append("\n")
                .append("**Seed:** s").append(SeasonTracker.getCurrentSeasonVersion())
                .append(".").append(SeasonTracker.getCurrentGenerationCount()).append("\n");

        if (characterStrength != CharacterStrength.COMMON) {
            messageBuilder
                    .append("\n**Strength:** ").append(characterStrength.getStrengthEmoji()).append("\n")
                    .append("**Rarity Level:** <@&").append(characterStrength.getRoleId()).append(">\n")
                    .append("**Class Name:** ").append(characterClass.getName()).append("\n");
        } else {
            messageBuilder
                    .append("\n**Rarity Level:** <@&").append(characterStrength.getRoleId()).append(">\n");
        }

        return messageBuilder.append("\n").append("**prompt:**\n`").append(prompt).append("`");
    }

    private String formatListReferences(List<String> urls) {
        StringBuilder referencesBuilder = new StringBuilder();
        for (String imageUrl : urls) {
            referencesBuilder.append(imageUrl).append(" ");
        }

        return referencesBuilder.toString();
    }

    private String getRandomFromListReferences(List<String> urls) {
        return urls.get(new Random().nextInt(urls.size()));
    }
}
