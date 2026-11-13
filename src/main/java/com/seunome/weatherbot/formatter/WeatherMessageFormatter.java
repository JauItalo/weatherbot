package com.seunome.weatherbot.formatter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.seunome.weatherbot.dto.CurrentConditions;
import com.seunome.weatherbot.dto.DailyForecastItem;
import com.seunome.weatherbot.dto.GeocodingResult;
import com.seunome.weatherbot.dto.LocationInfo;
import com.seunome.weatherbot.dto.WeatherResponse;

@Component
public class WeatherMessageFormatter {

    public static final String WELCOME = """
            Olá! 🌤️ Sou seu assistente meteorológico!
            Posso consultar a temperatura atual e a previsão do tempo de qualquer cidade do mundo.
            📍 Qual cidade você deseja consultar?""";

    public String formatWeather(WeatherResponse weather) {
        LocationInfo loc = weather.location();
        CurrentConditions cur = weather.current();

        StringBuilder sb = new StringBuilder();
        sb.append("🌤️ Previsão do tempo — ").append(formatLocation(loc)).append("\n\n");
        sb.append("🌡️ Temperatura: ").append(temp(cur.temperature())).append("\n");
        sb.append("🤔 Sensação térmica: ").append(temp(cur.apparentTemperature())).append("\n");
        sb.append("💧 Umidade: ").append(value(cur.humidity(), "%")).append("\n");
        sb.append("💨 Vento: ").append(value(cur.windSpeed(), " km/h")).append("\n");
        sb.append("🌧️ Chuva: ").append(value(cur.rainProbability(), "%")).append("\n");
        sb.append(cur.emoji()).append(" Condição: ").append(cur.description()).append("\n");
        sb.append("🌅 Nascer do sol: ").append(orDash(cur.sunrise())).append("\n");
        sb.append("🌇 Pôr do sol: ").append(orDash(cur.sunset())).append("\n\n");

        sb.append("📅 Previsão para os próximos dias:\n");
        List<DailyForecastItem> days = weather.daily();
        for (DailyForecastItem day : days) {
            sb.append("• ").append(day.dayOfWeek()).append(": ")
              .append(day.emoji()).append(" ")
              .append(temp(day.temperatureMax())).append(" / ")
              .append(temp(day.temperatureMin()))
              .append("  🌧️ ").append(value(day.rainProbability(), "%"))
              .append("\n");
        }
        return sb.toString().trim();
    }

    public String formatDisambiguation(String query, List<GeocodingResult> options) {
        StringBuilder sb = new StringBuilder();
        sb.append("📍 Encontrei várias cidades chamadas \"").append(query).append("\":\n\n");

        for (int i = 0; i < options.size(); i++) {
            sb.append(i + 1).append(". ").append(describe(options.get(i))).append("\n");
        }

        sb.append("\nDigite novamente incluindo o estado ou país. Exemplo: \"")
          .append(query).append(", ").append(exampleHint(options.get(0))).append("\"");
        return sb.toString();
    }

    public String formatNotFound(String query) {
        return "😕 Não encontrei nenhuma cidade com o nome \"" + query
                + "\". Verifique a grafia e tente novamente.";
    }

    // ---------- auxiliares ----------

    private String formatLocation(LocationInfo loc) {
        return joinNonBlank(loc.city(), loc.state(), loc.country());
    }

    private String describe(GeocodingResult r) {
        return joinNonBlank(r.name(), r.admin1(), r.country());
    }

    private String exampleHint(GeocodingResult r) {
        return (r.admin1() != null && !r.admin1().isBlank()) ? r.admin1() : r.country();
    }

    private String joinNonBlank(String... parts) {
        return java.util.Arrays.stream(parts)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.joining(", "));
    }

    private String temp(Integer value) {
        return value == null ? "--" : value + "°C";
    }

    private String value(Integer value, String suffix) {
        return value == null ? "--" : value + suffix;
    }

    private String orDash(String value) {
        return value == null ? "--" : value;
    }
}