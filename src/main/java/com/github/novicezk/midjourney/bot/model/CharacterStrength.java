package com.github.novicezk.midjourney.bot.model;

import java.util.Random;

public enum CharacterStrength {
    COMMON("Common", "1225826423311437824", 40),
    RARE("Rare", "1225826457981423748", 27),
    STRANGE("Strange", "1225826281153761362", 18),
    UNIQUE("Unique", "1225826496824742070", 10),
    EPIC("Epic", "1225826560997851216", 5);

    private final String strengthName;
    private final String roleId;
    private final int probability;

    CharacterStrength(String strengthName, String roleId, int probability) {
        this.strengthName = strengthName;
        this.roleId = roleId;
        this.probability = probability;
    }

    public String getStrengthName() {
        return strengthName;
    }

    public String getRoleId() {
        return roleId;
    }

    public static CharacterStrength getRandomStrength() {
        Random random = new Random();
        int randomNumber = random.nextInt(100) + 1; // Generate random number from 1 to 100

        int cumulativeProbability = 0;
        for (CharacterStrength strength : values()) {
            cumulativeProbability += strength.probability;
            if (randomNumber <= cumulativeProbability) {
                return strength;
            }
        }
        return COMMON; // Fallback if probabilities do not sum up to 100
    }

    public int getCW() {
        return switch (this) {
            case EPIC -> 0;
            case UNIQUE -> 25;
            case STRANGE -> 50;
            case RARE -> 75;
            default -> 100;
        };
    }

    public String getStrengthEmoji() {
        return switch (this) {
            case EPIC -> "\uD83D\uDCAA\uD83D\uDCAA\uD83D\uDCAA\uD83D\uDCAA";
            case UNIQUE -> "\uD83D\uDCAA\uD83D\uDCAA\uD83D\uDCAA";
            case STRANGE -> "\uD83D\uDCAA\uD83D\uDCAA";
            case RARE -> "\uD83D\uDCAA";
            default -> "";
        };
    }
}
