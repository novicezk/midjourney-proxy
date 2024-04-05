package com.github.novicezk.midjourney.bot.model;

import java.util.Random;

public enum CharacterStrength {
    COMMON("Common"),
    RARE("Rare"),
    STRANGE("Strange"),
    UNIQUE("Unique"),
    EPIC("Epic");

    private final String strengthName;

    CharacterStrength(String strengthName) {
        this.strengthName = strengthName;
    }

    public String getStrengthName() {
        return strengthName;
    }

    public static CharacterStrength getRandomStrength() {
        Random random = new Random();
        int randomNumber = random.nextInt(100) + 1; // Generate random number from 1 to 100

        if (randomNumber <= 72) {
            return COMMON;
        } else if (randomNumber <= 87) {
            return RARE;
        } else if (randomNumber <= 95) {
            return STRANGE;
        } else if (randomNumber <= 99) {
            return UNIQUE;
        } else {
            return EPIC;
        }
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
