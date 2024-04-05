package com.github.novicezk.midjourney.bot.images;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.github.novicezk.midjourney.bot.model.images.ImageResponse;
import io.github.cdimascio.dotenv.Dotenv;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ImageBBUploader {
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload";
    private static final String API_KEY;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        Dotenv config = Dotenv.configure().ignoreIfMissing().load();
        API_KEY = config.get("IMGBB_TOKEN");

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ImageResponse uploadImageNew(String imageUrl) {
        Client client = ClientBuilder.newClient();

        // Request body
        Form form = new Form();
        form.param("key", API_KEY);
        form.param("image", imageUrl);

        // send POST request
        Response response = client.target(UPLOAD_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        // Handle the response
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            try {
                String responseJson = response.readEntity(String.class);
                return objectMapper.readValue(responseJson, ImageResponse.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Error uploading image. HTTP response code: " + response.getStatus());
        }

        return null;
    }
}
