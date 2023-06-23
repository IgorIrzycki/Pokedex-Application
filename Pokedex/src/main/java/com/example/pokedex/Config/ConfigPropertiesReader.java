package com.example.pokedex.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigPropertiesReader {
    private final Properties properties;

    public ConfigPropertiesReader() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "\\src\\main\\resources\\com\\example\\pokedex\\config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error during reading config file.");
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}

