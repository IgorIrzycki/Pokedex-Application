package com.example.pokedex.Api;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class PokeApiImageDownloader {
    public static String baseUrl = "https://pokeapi.co/api/v2/pokemon/";

    public static void downloadAndDisplayImage(String pokemonName, ImageView imageView) {
        try {
            String url = baseUrl + pokemonName.toLowerCase();
            String jsonData = downloadPokemonData(url);

            if (jsonData != null && !jsonData.isEmpty()) {
                Image image = getImageFromJsonData(jsonData);
                if (image != null) {
                    imageView.setImage(image);
                } else {
                    showAlert("Cant find image for chosen pokemon.");
                }
            } else {
                showAlert("Cant download pokemon data.");
            }
        } catch (IOException ex) {
            showAlert("There was an error:" + ex.getMessage());
        }
    }

    private static String downloadPokemonData(String url) throws IOException {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                return outputStream.toString();
            } else {
                return null;
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private static Image getImageFromJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject sprites = jsonObject.getJSONObject("sprites");
            String imageUrl = sprites.getString("front_default");
            return new Image(imageUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
