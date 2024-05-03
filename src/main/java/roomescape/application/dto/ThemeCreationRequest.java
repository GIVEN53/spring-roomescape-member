package roomescape.application.dto;

import roomescape.domain.theme.Theme;

public record ThemeCreationRequest(String name, String description, String thumbnail) {
    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
