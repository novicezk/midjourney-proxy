package com.github.novicezk.midjourney.bot.providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.bot.model.CharacterStrength;
import com.github.novicezk.midjourney.bot.model.Style;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class StyleDataProvider {
    private static final String JSON_STYLES_PATH = "data-generation/styles.json";
    private final static String DEFAULT_STYLE = "Common";

    private final ObjectMapper objectMapper;
    private List<Style> styles;

    public StyleDataProvider() {
        this.objectMapper = new ObjectMapper();
        loadStylesData();
    }

    private void loadStylesData() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_STYLES_PATH)) {
            styles = objectMapper.readValue(in, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Style getDefaultStyle() {
        for (Style style : styles) {
            if (style.getName().equalsIgnoreCase(DEFAULT_STYLE)) {
                return style;
            }
        }
        return new Style();
    }

    public Style getStyleByStrength(CharacterStrength strength) {
        for (Style style : styles) {
            if (style.getName().equalsIgnoreCase(strength.getStrengthName())) {
                return style;
            }
        }
        return getDefaultStyle();
    }
}
