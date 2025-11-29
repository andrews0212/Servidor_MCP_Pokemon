package com.example.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PokemonService {

    private final RestClient restClient;

    public PokemonService() {
        // 1. Cambiamos la URL base a la de PokeAPI (v2)
        this.restClient = RestClient.builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                // PokeAPI devuelve JSON por defecto, no necesitamos headers complejos
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "SpringAI-Agent/1.0")
                .build();
    }

    @Tool(description = "Get information about a Pokemon by its name (e.g., pikachu, charizard)")
    public String getPokemonInfo(
            @ToolParam(description = "The name of the Pokemon in lowercase") String name
    ) {
        // 2. Hacemos la llamada al endpoint /pokemon/{name}
        try {
            return restClient.get()
                    .uri("pokemon/{name}", name.toLowerCase()) // La API requiere minúsculas
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            // Es útil devolver un mensaje de error claro para que la IA sepa qué pasó
            return "Error: Could not find Pokemon with name '" + name + "'. Please check the spelling.";
        }
    }

    @Tool(description = "Get information about a Pokemon move or ability")
    public String getAbilityInfo(
            @ToolParam(description = "The name of the ability (e.g., static, overgrow)") String ability
    ) {
        try {
            return restClient.get()
                    .uri("ability/{ability}", ability.toLowerCase())
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            return "Error: Could not find ability details.";
        }
    }
}