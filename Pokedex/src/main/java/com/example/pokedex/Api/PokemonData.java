package com.example.pokedex.Api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class PokemonData {
    private static final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";

    public static String getPokemonImageUrl(String pokemonName) {
        try {
            String apiUrl = BASE_URL + pokemonName.toLowerCase();
            String jsonData = downloadPokemonData(apiUrl);

            if (!jsonData.isEmpty()) {
                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
                return jsonObject.getAsJsonObject("sprites").get("front_default").getAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String downloadPokemonData(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");
            String jsonData = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            return jsonData;
        }

        return "";
    }
}

