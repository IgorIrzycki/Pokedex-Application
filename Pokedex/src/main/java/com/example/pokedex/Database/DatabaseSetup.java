package com.example.pokedex.Database;

import com.example.pokedex.Config.ConfigPropertiesReader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

import java.sql.*;

public class DatabaseSetup {
    Connection connection;
    private final String URL;
    private final String USER;
    private final String PASSWORD;


    public DatabaseSetup() {
        ConfigPropertiesReader configReader = new ConfigPropertiesReader();
        URL = configReader.getProperty("db.url");
        USER = configReader.getProperty("db.user");
        PASSWORD = configReader.getProperty("db.password");
    }

    public void connectToDB() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Could not connect to database");
            }
        }
    }

    public void addPokemon(int teamId, String pokemonName) {
        connectToDB();

        String query = "INSERT INTO pokemon (teamId, pokemonName) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, teamId);
            statement.setString(2, pokemonName);

            statement.executeUpdate();
            System.out.println("Pokemon added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to add Pokemon.");
        }
    }

    public int addTeam(String teamName) {
        connectToDB();
        int id = -1;
        String query = "INSERT INTO team (teamName) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, teamName);

            int affectedRows = statement.executeUpdate();
            System.out.println("Team added successfully.");

            if (affectedRows > 0) {
                ResultSet ids = statement.getGeneratedKeys();
                if (ids.next()) {
                    id = ids.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to add Team.");
        }

        return id;
    }

    public void loadTeamNames(ComboBox<String> comboBoxTeams) {
        connectToDB();
        String selectTeamsQuery = "SELECT teamName FROM team";

        try (PreparedStatement selectTeamsStatement = connection.prepareStatement(selectTeamsQuery)) {
            ResultSet resultSet = selectTeamsStatement.executeQuery();

            comboBoxTeams.getItems().clear();

            while (resultSet.next()) {
                String teamName = resultSet.getString("teamName");
                comboBoxTeams.getItems().add(teamName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadTeamPokemon(String selectedTeamName, ListView<String> listViewPokemonTeam) {
        connectToDB();
        String selectPokemonQuery = "SELECT pokemonName FROM pokemon WHERE teamId = (SELECT teamId FROM team WHERE teamName = ?)";

        try (PreparedStatement selectPokemonStatement = connection.prepareStatement(selectPokemonQuery)) {
            selectPokemonStatement.setString(1, selectedTeamName);
            ResultSet resultSet = selectPokemonStatement.executeQuery();

            while (resultSet.next()) {
                String pokemonName = resultSet.getString("pokemonName");
                listViewPokemonTeam.getItems().add(pokemonName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to load team pokemon.");
        }
    }


    public void deletePokemon(String pokemonName) {
        connectToDB();

        String query = "DELETE FROM pokemon WHERE pokemonName = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, pokemonName);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Pokemon deleted successfully.");
            } else {
                System.out.println("Pokemon not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to delete Pokemon.");
        }
    }

    public void deleteAllTeamPokemon(int teamId) {
        connectToDB();

        String query = "DELETE FROM pokemon WHERE teamId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, teamId);

            statement.executeUpdate();
            System.out.println("All team Pokemon deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to delete team Pokemon.");
        }
    }

    public void deleteTeam(int teamId) {
        connectToDB();

        String query = "DELETE FROM team WHERE teamId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, teamId);

            statement.executeUpdate();
            System.out.println("Team deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to delete Team.");
        }
    }

    public int getTeamId(String teamName) {
        connectToDB();

        String query = "SELECT teamId FROM team WHERE teamName = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, teamName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("teamId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}

