package api.utils.enums;

import lombok.Getter;

@Getter
public enum GraphQlPaths {

    CHARACTERS_RESULTS("data.characters.results"),
    CHARACTERS_RESULTS_ID("data.characters.results.id"),
    CHARACTERS_INFO_PAGES("data.characters.info.pages"),
    DATA_CHARACTER("data.character"),
    DATA_CHARACTER_ID("data.character.id"),
    DATA_CHARACTER_NAME("data.character.name"),
    DATA_EPISODE_NAME("data.episode.name"),
    DATA_EPISODE_CHARACTERS("data.episode.characters"),
    DATA_EPISODE_CHARACTERS_NAME("data.episode.characters[%d].name"),
    ERRORS("errors"),
    ERRORS_MESSAGE("errors[%d].message");

    private final String path;

    GraphQlPaths(String path) {
        this.path = path;
    }
}
