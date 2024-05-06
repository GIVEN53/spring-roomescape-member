package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationTimeService;
import roomescape.application.ThemeService;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationtime.AvailableTimeResponse;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;

@RestController
@RequestMapping("/themes")
@Validated
public class ThemeController {
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public ThemeController(ThemeService themeService, ReservationTimeService reservationTimeService) {
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody ThemeRequest request) {
        Theme theme = themeService.save(request.toThemeCreationRequest());
        ThemeResponse response = ThemeResponse.from(theme);
        URI location = URI.create("/themes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<ThemeResponse> getThemes() {
        List<Theme> themes = themeService.findThemes();
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTheme(@PathVariable @Positive long id) {
        themeService.delete(id);
    }

    @GetMapping("/popular")
    public List<ThemeResponse> getPopularThemes() {
        List<Theme> popularThemes = themeService.findPopularThemes();
        return popularThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public List<AvailableTimeResponse> getAvailableTimes(@PathVariable @Positive long id,
                                                         @RequestParam LocalDate date) {
        return reservationTimeService.findAvailableTimes(id, date);
    }
}
