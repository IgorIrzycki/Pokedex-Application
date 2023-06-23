package com.example.pokedex.Model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final String name;
    private final List<String> pokemon;

    public Team(String name) {
        this.name = name;
        this.pokemon = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<String> getPokemon() {
        return pokemon;
    }
}
