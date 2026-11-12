package com.seunome.weatherbot.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResponse(List<GeocodingResult> results) {}