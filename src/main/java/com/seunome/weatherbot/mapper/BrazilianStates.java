package com.seunome.weatherbot.mapper;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Converte a sigla de um estado brasileiro (ex.: "PB") no nome por extenso ("Paraíba"). */
public final class BrazilianStates {

    private static final Map<String, String> UF_TO_NAME = Map.ofEntries(
            Map.entry("AC", "Acre"),
            Map.entry("AL", "Alagoas"),
            Map.entry("AP", "Amapá"),
            Map.entry("AM", "Amazonas"),
            Map.entry("BA", "Bahia"),
            Map.entry("CE", "Ceará"),
            Map.entry("DF", "Distrito Federal"),
            Map.entry("ES", "Espírito Santo"),
            Map.entry("GO", "Goiás"),
            Map.entry("MA", "Maranhão"),
            Map.entry("MT", "Mato Grosso"),
            Map.entry("MS", "Mato Grosso do Sul"),
            Map.entry("MG", "Minas Gerais"),
            Map.entry("PA", "Pará"),
            Map.entry("PB", "Paraíba"),
            Map.entry("PR", "Paraná"),
            Map.entry("PE", "Pernambuco"),
            Map.entry("PI", "Piauí"),
            Map.entry("RJ", "Rio de Janeiro"),
            Map.entry("RN", "Rio Grande do Norte"),
            Map.entry("RS", "Rio Grande do Sul"),
            Map.entry("RO", "Rondônia"),
            Map.entry("RR", "Roraima"),
            Map.entry("SC", "Santa Catarina"),
            Map.entry("SP", "São Paulo"),
            Map.entry("SE", "Sergipe"),
            Map.entry("TO", "Tocantins"));

    private BrazilianStates() {}

    public static Optional<String> nameOf(String uf) {
        if (uf == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(UF_TO_NAME.get(uf.trim().toUpperCase(Locale.ROOT)));
    }
}
