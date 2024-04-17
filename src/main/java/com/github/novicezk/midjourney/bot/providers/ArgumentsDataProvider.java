package com.github.novicezk.midjourney.bot.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.bot.model.Arguments;
import com.github.novicezk.midjourney.bot.model.AspectRatio;
import com.github.novicezk.midjourney.bot.model.Version;

import java.io.IOException;
import java.io.InputStream;

public class ArgumentsDataProvider {
    private static final String JSON_ARGUMENTS_PATH = "data-generation/arguments.json";
    private final static String DEFAULT_ASPECT_RATION = "Square";
    private final static String DEFAULT_VERSION = "Realistic";

    private final ObjectMapper objectMapper;
    private Arguments arguments;

    public ArgumentsDataProvider() {
        this.objectMapper = new ObjectMapper();
        loadArgumentsData();
    }

    private void loadArgumentsData() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(JSON_ARGUMENTS_PATH)) {
            arguments = objectMapper.readValue(in, Arguments.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDefaultVersion() {
        for (Version version : arguments.getVersions()) {
            if (version.getName().equalsIgnoreCase(DEFAULT_VERSION)) {
                return version.getValue();
            }
        }
        return "";
    }

    public String getDefaultAspectRatio() {
        for (AspectRatio aspectRatio : arguments.getAspectRatio()) {
            if (aspectRatio.getName().equalsIgnoreCase(DEFAULT_ASPECT_RATION)) {
                return aspectRatio.getValue();
            }
        }
        return "";
    }
}
