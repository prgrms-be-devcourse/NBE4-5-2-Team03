package com.example.Flicktionary.domain.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbMovieResponseWithDetail(
        @JsonProperty("id") Long tmdbId,
        @JsonProperty("title") String title,
        @JsonProperty("overview") String overview,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("status") String status,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("runtime") Integer runtime,
        @JsonProperty("production_countries") List<TmdbProductionCountry> productionCountries,
        @JsonProperty("production_companies") List<TmdbProductionCompany> productionCompanies,
        @JsonProperty("genres") List<TmdbGenre> genres,
        @JsonProperty("credits") TmdbCredits credits
) {
    public record TmdbProductionCountry(@JsonProperty("name") String name) {
    }

    public record TmdbProductionCompany(@JsonProperty("name") String name) {
    }

    public record TmdbGenre(@JsonProperty("id") Long id,
                            @JsonProperty("name") String name) {
    }

    public record TmdbCredits(@JsonProperty("cast") List<TmdbActor> cast,
                              @JsonProperty("crew") List<TmdbCrew> crew) {
    }

    public record TmdbActor(@JsonProperty("id") Long id,
                            @JsonProperty("name") String name,
                            @JsonProperty("profile_path") String profilePath,
                            @JsonProperty("character") String character) {
    }

    public record TmdbCrew(@JsonProperty("id") Long id,
                           @JsonProperty("name") String name,
                           @JsonProperty("profile_path") String profilePath,
                           @JsonProperty("job") String job) {
    }

}
