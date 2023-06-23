package com.example.pokedex;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class PokedexApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PokedexApplication.class.getResource("pokedex-view.fxml"));
        Pane pane = fxmlLoader.load();

        Scene scene = new Scene(pane, pane.getPrefWidth(), pane.getPrefHeight());

        stage.setTitle("Pokedex");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();

    }
}