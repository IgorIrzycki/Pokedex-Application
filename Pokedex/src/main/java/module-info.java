module com.example.pokedex {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.google.gson;
    requires org.json;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires mysql.connector.java;
    requires java.sql;
    requires com.google.common;
    requires org.apache.commons.lang3;

    opens com.example.pokedex to javafx.fxml;
    exports com.example.pokedex;
    exports com.example.pokedex.Model;
    opens com.example.pokedex.Model to javafx.fxml;
    exports com.example.pokedex.Controller;
    opens com.example.pokedex.Controller to javafx.fxml;
    exports com.example.pokedex.Api;
    opens com.example.pokedex.Api to javafx.fxml;
}