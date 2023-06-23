package com.example.pokedex.Controller;

import com.example.pokedex.Api.PokeApiImageDownloader;
import com.example.pokedex.Database.DatabaseSetup;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.example.pokedex.Api.PokemonData.downloadPokemonData;

public class TeamShowController {
    public ComboBox<String> comboBoxTeams;
    public BarChart<String, Number> chartStats;
    public ListView<String> listViewTeamPokemon2;
    private final DatabaseSetup databaseSetup = new DatabaseSetup();
    public ImageView ivPokemonImage;
    public Button btnDeletePokemon;
    public Button btnDeleteTeam;

    @FXML
    public void initialize() {
        comboBoxTeams.setOnAction(this::handleComboBoxTeamsSelectionChange);
        databaseSetup.loadTeamNames(comboBoxTeams);
        btnDeleteTeam.setOnAction(this::handleDeleteTeam);
        btnDeletePokemon.setOnAction(this::handleDeletePokemon);
        listViewTeamPokemon2.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                PokeApiImageDownloader.downloadAndDisplayImage(newValue, ivPokemonImage);
                displayPokemonStats(newValue);
            }
        });
    }

    private void displayPokemonStats(String pokemonName) {
        try {
            String url = "https://pokeapi.co/api/v2/pokemon/" + pokemonName;
            String jsonData = downloadPokemonData(url);
            Map<String, Integer> pokemonStats = parsePokemonStats(jsonData);

            if (pokemonStats != null) {
                chartStats.getData().clear();

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Stats");

                for (Map.Entry<String, Integer> entry : pokemonStats.entrySet()) {
                    String statName = entry.getKey().replace('-', ' ');
                    Integer statValue = entry.getValue();
                    series.getData().add(new XYChart.Data<>(statName, statValue));
                }

                chartStats.getData().add(series);
            } else {
                System.out.println("Cant download pokemon data.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Map<String, Integer> parsePokemonStats(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            JSONArray statsArray = jsonObject.getJSONArray("stats");

            Map<String, Integer> pokemonStats = new LinkedHashMap<>();

            for (int i = 0; i < statsArray.length(); i++) {
                JSONObject statObject = statsArray.getJSONObject(i);
                String statName = statObject.getJSONObject("stat").getString("name");
                int statValue = statObject.getInt("base_stat");
                pokemonStats.put(statName, statValue);
            }

            return pokemonStats;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void handleComboBoxTeamsSelectionChange(ActionEvent actionEvent) {
        listViewTeamPokemon2.getItems().clear();

        String selectedTeamName = comboBoxTeams.getValue();

        databaseSetup.loadTeamPokemon(selectedTeamName, listViewTeamPokemon2);
    }

    public void handleDeletePokemon(ActionEvent actionEvent) {
        String selectedPokemon = listViewTeamPokemon2.getSelectionModel().getSelectedItem();
        if (selectedPokemon != null) {
            databaseSetup.deletePokemon(selectedPokemon);
            listViewTeamPokemon2.getItems().remove(selectedPokemon);
        }
    }

    public void handleDeleteTeam(ActionEvent actionEvent) {
        String selectedTeamName = comboBoxTeams.getValue();
        if (selectedTeamName != null) {
            int teamId = databaseSetup.getTeamId(selectedTeamName);
            if (teamId != -1) {
                databaseSetup.deleteAllTeamPokemon(teamId);
                databaseSetup.deleteTeam(teamId);
                comboBoxTeams.getItems().remove(selectedTeamName);
                comboBoxTeams.getSelectionModel().clearSelection();
            }
        }
    }

}
