package com.example.pokedex.Controller;

import org.apache.commons.lang3.StringUtils;
import com.example.pokedex.Database.DatabaseSetup;
import com.example.pokedex.Model.Team;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class TeamsManagementController {

    public ImageView ivPokemonImage;
    public ComboBox<String> comboBoxGenerations;
    public ListView<String> listViewAllPokemon;
    public Button btnListAll;
    public ListView<String> listViewTeamPokemon;
    public Button btnShowTeams;
    public Button btnRemoveFromTeam;
    public ListView<String> listViewTeams;
    public TextField txtTeamName;
    public Button btnCreateTeam;
    public Button btnSendToDb;
    public Button btnAddToTeam;
    public Button btnGoBack;
    private final List<Team> teams = new ArrayList<>();
    private final DatabaseSetup databaseSetup = new DatabaseSetup();

    @FXML
    public void initialize() {
        btnListAll.setOnAction(this::handleListAllButtonClick);
        listViewAllPokemon.getSelectionModel().selectedItemProperty().addListener(this::handlePokemonSelectionChange);
        comboBoxGenerations.setOnAction(this::handleGenerationSelectionChange);
        btnAddToTeam.setOnAction(this::handleAddToTeamButtonClick);
        btnCreateTeam.setOnAction(this::handleCreateTeamButtonClick);
        btnRemoveFromTeam.setOnAction(this::handleRemoveFromTeamButtonClick);
        listViewTeams.getSelectionModel().selectedItemProperty().addListener(this::listViewTeams_SelectedIndexChanged);
        btnSendToDb.setOnAction(this::handleSendToDbButtonClick);
        txtTeamName.setOnMouseClicked(this::onTextFieldClicked);
        btnShowTeams.setOnAction(this::handleShowTeamButtonClick);


        for (int i = 1; i <= 9; i++) {
            comboBoxGenerations.getItems().add("Generation " + i);
        }
    }

    private void onTextFieldClicked(MouseEvent mouseEvent) {
        txtTeamName.setText("");
    }

    private void listViewTeams_SelectedIndexChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        listViewTeamPokemon.getItems().clear();

        int teamIndex = listViewTeams.getSelectionModel().getSelectedIndex();

        if (teamIndex >= 0) {
            Team selectedTeam = teams.get(teamIndex);

            for (String pokemon : selectedTeam.getPokemon()) {
                listViewTeamPokemon.getItems().add(pokemon);
            }
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

                String pokemonName = data.getString("name");
                for (int i = 0; i < listViewAllPokemon.getItems().size(); i++) {
                    if (listViewAllPokemon.getItems().get(i).equals(pokemonName)) {
                        listViewAllPokemon.getSelectionModel().select(i);
                        break;
                    }
                }
            });
        } else {
            Platform.runLater(() -> ivPokemonImage.setImage(null));
        }
    }

    private void displayPokemonData(JSONObject data) {
        String imageUrl = data.getJSONObject("sprites").getString("front_default");
        if (!imageUrl.isEmpty()) {
            Image image = new Image(imageUrl);
            ivPokemonImage.setImage(image);
        } else {
            ivPokemonImage.setImage(null);
        }
    }

    private void handleCreateTeamButtonClick(ActionEvent event) {
        String teamName = txtTeamName.getText();

        if (StringUtils.trim(teamName).isEmpty() || StringUtils.trim(teamName).equals("Type team name...")) {
            showAlert();
            return;
        }

        Team newTeam = new Team(teamName);
        teams.add(newTeam);
        listViewTeams.getItems().add(newTeam.getName());

        txtTeamName.setText("Type team name...");
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Type team name!");
        alert.showAndWait();
    }

    private void handleRemoveFromTeamButtonClick(ActionEvent event) {
        String selectedPokemon = listViewTeamPokemon.getSelectionModel().getSelectedItem();
        int teamIndex = listViewTeams.getSelectionModel().getSelectedIndex();

        if (selectedPokemon != null && teamIndex >= 0) {
            Team selectedTeam = teams.get(teamIndex);
            selectedTeam.getPokemon().remove(selectedPokemon);
            listViewTeamPokemon.getItems().remove(selectedPokemon);
        }
    }

    private void handleAddToTeamButtonClick(ActionEvent event) {
        String selectedPokemon = listViewAllPokemon.getSelectionModel().getSelectedItem();
        String selectedTeamString = listViewTeams.getSelectionModel().getSelectedItem();

        if (selectedPokemon != null && selectedTeamString != null) {
            int teamIndex = listViewTeams.getSelectionModel().getSelectedIndex();
            Team selectedTeam = teams.get(teamIndex);

            if (selectedTeam.getPokemon().size() >= 6) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("A team can consist of a maximum of 6 Pokemon.");
                alert.showAndWait();
                return;
            }

            if (!selectedTeam.getPokemon().contains(selectedPokemon)) {
                selectedTeam.getPokemon().add(selectedPokemon);
                listViewTeamPokemon.getItems().add(selectedPokemon);
            }
        }
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

    public String sendHttpRequest(String url) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        HttpResponse response = httpClient.execute(httpGet);

        return EntityUtils.toString(response.getEntity());
    }

    public void navigateToPokedexView(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/pokedex/pokedex-view.fxml")));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void handleShowTeamButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pokedex/team-show-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Show Teams");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendToDbButtonClick(ActionEvent event) {
        if (listViewTeams.getSelectionModel().isEmpty()) {
            showAlert("Select a team from the list.");
            return;
        }

        String teamName = listViewTeams.getSelectionModel().getSelectedItem();
        ObservableList<String> pokemonNames = listViewTeamPokemon.getItems();

        if (teamName == null || pokemonNames.isEmpty()) {
            showAlert("Enter a team name and at least one Pokemon.");
            return;
        }


        int teamId = databaseSetup.addTeam(teamName);

        for (String pokemon : pokemonNames) {
            databaseSetup.addPokemon(teamId, pokemon);
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
