package com.example.pokedex.Controller;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.example.pokedex.Api.PokemonData.getPokemonImageUrl;

public class PokemonSearchController {
    public VBox vboxEvolutions;
    public ComboBox<String> comboBoxGenerations;
    public ListView<String> listViewAllPokemon;
    public Button btnListAll;
    public Button btnSearch;
    public Button btnSearchByNumber;
    public TextArea textAreaStats;
    public TextArea textAreaPokemonInfo;
    public TextArea textAreaAdditionalInfo;
    public Button btnGoBack;
    public Label lblPokemonWebsiteResult;
    public ImageView ivPokemonImage;
    private final HashMap<String, JSONObject> pokemonDataCache = new HashMap<>();

    @FXML
    public void initialize() {
        btnListAll.setOnAction(this::handleListAllButtonClick);
        listViewAllPokemon.getSelectionModel().selectedItemProperty().addListener(this::handlePokemonSelectionChange);
        comboBoxGenerations.setOnAction(this::handleGenerationSelectionChange);
        btnSearch.setOnAction(this::handleSearchButtonClick);
        btnSearchByNumber.setOnAction(this::handleSearchByNumberButtonClick);

        for (int i = 1; i <= 9; i++) {
            comboBoxGenerations.getItems().add("Generation " + i);
        }
    }

    private void handleListAllButtonClick(ActionEvent event) {
        String apiUrl = "https://pokeapi.co/api/v2/pokemon?limit=1000";

        CompletableFuture.supplyAsync(() -> {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(apiUrl);
            try {
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAcceptAsync(json -> {
            if (json != null) {
                Platform.runLater(() -> {
                    listViewAllPokemon.getItems().clear();
                    try {
                        JSONObject data = new JSONObject(json);
                        JSONArray results = data.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject pokemon = results.getJSONObject(i);
                            String name = pokemon.getString("name");
                            listViewAllPokemon.getItems().add(name);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }


    private void handlePokemonSelectionChange(ObservableValue<?> observableValue, Object oldValue, Object newValue) {
        if (newValue != null) {
            CompletableFuture.runAsync(() -> {
                String selectedPokemon = newValue.toString();
                try {
                    searchPokemonOnPokemonWebsite(selectedPokemon);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    private void searchPokemonOnPokemonWebsite(String name) throws IOException {
        String apiUrl = "https://pokeapi.co/api/v2/pokemon/" + name.toLowerCase();

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(apiUrl);
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);

        if (response.getStatusLine().getStatusCode() == 200) {
            JSONObject data = new JSONObject(json);
            Platform.runLater(() -> {
                displayPokemonData(data);
                try {
                    displayEvolutionInfo(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String pokemonName = data.getString("name");
                for (int i = 0; i < listViewAllPokemon.getItems().size(); i++) {
                    if (listViewAllPokemon.getItems().get(i).equals(pokemonName)) {
                        listViewAllPokemon.getSelectionModel().select(i);
                        break;
                    }
                }
            });
        } else {
            Platform.runLater(() -> {
                lblPokemonWebsiteResult.setText("Error during downloading the pokemon website data.");
                ivPokemonImage.setImage(null);
                textAreaPokemonInfo.setText("");
                textAreaAdditionalInfo.setText("");
                vboxEvolutions.getChildren().clear();
            });
        }
    }

    private void handleSearchByNumberButtonClick(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search Pokemon by Number");
        dialog.setHeaderText("Enter valid Pokemon number:");
        dialog.setContentText("Pokemon number:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String pokemonNumberInput = result.get();
            if (!pokemonNumberInput.isEmpty()) {
                try {
                    int pokemonNumber = Integer.parseInt(pokemonNumberInput);
                    searchPokemonByNumber(pokemonNumber);
                } catch (NumberFormatException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleSearchButtonClick(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search Pokemon by Name");
        dialog.setHeaderText("Enter valid Pokemon Name");
        dialog.setContentText("Pokemon Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String pokemonName = result.get();
            if (!pokemonName.isEmpty()) {
                try {
                    searchPokemonOnPokemonWebsite(pokemonName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void searchPokemonByNumber(int number) throws IOException {
        String apiUrl = "https://pokeapi.co/api/v2/pokemon/" + number;

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(apiUrl);
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);

        if (response.getStatusLine().getStatusCode() == 200) {
            JSONObject data = new JSONObject(json);
            Platform.runLater(() -> {
                displayPokemonData(data);
                try {
                    displayEvolutionInfo(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String pokemonName = data.getString("name");
                for (int i = 0; i < listViewAllPokemon.getItems().size(); i++) {
                    if (listViewAllPokemon.getItems().get(i).equals(pokemonName)) {
                        listViewAllPokemon.getSelectionModel().select(i);
                        break;
                    }
                }
            });
        } else {
            Platform.runLater(() -> {
                lblPokemonWebsiteResult.setText("Error during downloading the pokemon website data.");
                ivPokemonImage.setImage(null);
                textAreaPokemonInfo.setText("");
                textAreaAdditionalInfo.setText("");
                vboxEvolutions.getChildren().clear();
            });
        }
    }

    private void displayPokemonData(JSONObject data) {
        String pokemonName = data.getString("name");
        lblPokemonWebsiteResult.setText("Name: " + pokemonName.replace('-', ' '));
        Font font = Font.font("Roboto Light", FontWeight.NORMAL, 20);
        lblPokemonWebsiteResult.setFont(font);

        String imageUrl = getPokemonImageUrl(pokemonName);
        assert imageUrl != null;
        if (!imageUrl.isEmpty()) {
            Image image = new Image(imageUrl);
            ivPokemonImage.setImage(image);
        } else {
            ivPokemonImage.setImage(null);
        }

        StringBuilder pokemonInfo = new StringBuilder("Height: " + data.getInt("height") + System.lineSeparator() +
                "Weight: " + data.getInt("weight") + System.lineSeparator() +
                "Types: ");
        JSONArray types = data.getJSONArray("types");
        for (int i = 0; i < types.length(); i++) {
            JSONObject type = types.getJSONObject(i).getJSONObject("type");
            pokemonInfo.append(type.getString("name")).append(" ");
        }
        textAreaPokemonInfo.setText(pokemonInfo.toString());

        StringBuilder abilities = new StringBuilder();
        JSONArray abilitiesArray = data.getJSONArray("abilities");
        for (int i = 0; i < abilitiesArray.length(); i++) {
            JSONObject ability = abilitiesArray.getJSONObject(i).getJSONObject("ability");
            abilities.append(ability.getString("name").replace('-', ' ')).append(System.lineSeparator());
        }
        textAreaAdditionalInfo.setText(abilities.toString());

        StringBuilder statsBuilder = new StringBuilder();
        JSONArray stats = data.getJSONArray("stats");
        for (int i = 0; i < stats.length(); i++) {
            JSONObject stat = stats.getJSONObject(i);
            String statName = stat.getJSONObject("stat").getString("name");
            int statValue = stat.getInt("base_stat");
            String statBar = getStatBar(statValue);

            statsBuilder.append(statName).append(": ").append(statValue).append(System.lineSeparator());
            statsBuilder.append(statBar).append(System.lineSeparator());
        }
        textAreaStats.setText(statsBuilder.toString());
    }

    private void displayEvolutionInfo(JSONObject data) throws IOException {

        vboxEvolutions.getChildren().clear();

        String speciesName = data.getJSONObject("species").getString("name");
        JSONObject speciesData;

        if (pokemonDataCache.containsKey(speciesName)) {
            speciesData = pokemonDataCache.get(speciesName);
        } else {
            String speciesUrl = data.getJSONObject("species").getString("url");
            String speciesJson = null;

            try {
                speciesJson = sendHttpRequest(speciesUrl);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            assert speciesJson != null;
            speciesData = new JSONObject(speciesJson);
            pokemonDataCache.put(speciesName, speciesData);
        }

        if (speciesData != null && speciesData.has("evolution_chain")) {
            String evolutionChainUrl = speciesData.getJSONObject("evolution_chain").getString("url");
            String evolutionChainJson = null;

            try {
                evolutionChainJson = sendHttpRequest(evolutionChainUrl);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            assert evolutionChainJson != null;
            JSONObject chainData = new JSONObject(evolutionChainJson);

            displayEvolutionChain(chainData.getJSONObject("chain"));
        }

    }


    private void displayEvolutionChain(JSONObject chain) {
        vboxEvolutions.getChildren().clear();

        JSONObject currentEvolution = chain;

        while (currentEvolution != null) {
            String evolutionName = currentEvolution.getJSONObject("species").getString("name");

            String evolutionImageUrl = getPokemonImageUrl(evolutionName);

            ImageView ivEvolution = new ImageView();
            ivEvolution.setFitWidth(100);
            ivEvolution.setFitHeight(100);
            ivEvolution.setPreserveRatio(true);
            ivEvolution.setSmooth(true);
            ivEvolution.setCache(true);
            assert evolutionImageUrl != null;
            ivEvolution.setImage(new Image(evolutionImageUrl));
            ivEvolution.setUserData(evolutionName);
            ivEvolution.setOnMouseClicked(this::handleEvolutionClick);

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.getChildren().add(ivEvolution);

            vboxEvolutions.getChildren().add(hbox);

            if (currentEvolution.has("evolves_to") && currentEvolution.getJSONArray("evolves_to").length() > 0) {
                currentEvolution = currentEvolution.getJSONArray("evolves_to").getJSONObject(0);
            } else {
                break;
            }
        }
    }

    public String sendHttpRequest(String url) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        HttpResponse response = httpClient.execute(httpGet);

        return EntityUtils.toString(response.getEntity());
    }

    private void handleEvolutionClick(javafx.scene.input.MouseEvent mouseEvent) {
        try {
            ImageView imageView = (ImageView) mouseEvent.getTarget();
            String evolutionName = imageView.getUserData().toString();

            searchPokemonOnPokemonWebsite(evolutionName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getStatBar(int value) {
        final int maxStatValue = 255;
        final int maxBarLength = 20;

        int barLength = (int) Math.round((double) value / maxStatValue * maxBarLength);

        return "[" +
                "=".repeat(Math.max(0, barLength)) +
                "]";
    }

    private void handleGenerationSelectionChange(ActionEvent event) {
        CompletableFuture.runAsync(() -> {
            int selectedGeneration = comboBoxGenerations.getSelectionModel().getSelectedIndex() + 1;

            Platform.runLater(() -> {
                String apiUrl = "https://pokeapi.co/api/v2/generation/" + selectedGeneration;
                try {
                    String json = sendHttpRequest(apiUrl);
                    JSONObject data = new JSONObject(json);

                    if (data.has("pokemon_species")) {
                        listViewAllPokemon.getItems().clear();
                        JSONArray pokemonSpecies = data.getJSONArray("pokemon_species");
                        for (int i = 0; i < pokemonSpecies.length(); i++) {
                            String pokemonName = pokemonSpecies.getJSONObject(i).getString("name");
                            listViewAllPokemon.getItems().add(pokemonName);
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public void navigateToPokedexView(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/pokedex/pokedex-view.fxml")));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
