package roomescape.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.dto.ThemeCreationRequest;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.ReservationTime;
import roomescape.domain.time.repository.ReservationTimeRepository;
import roomescape.dto.theme.AvailableTimeResponse;

@Service
public class ThemeService {
    private static final int INVALID_DELETED_COUNT = 0;

    private final Clock clock;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ThemeService(Clock clock, ThemeRepository themeRepository, ReservationRepository reservationRepository,
                        ReservationTimeRepository reservationTimeRepository) {
        this.clock = clock;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public Theme save(ThemeCreationRequest request) {
        Theme theme = request.toTheme();
        return themeRepository.save(theme);
    }

    public List<Theme> findThemes() {
        return themeRepository.findAll();
    }

    public void delete(long id) {
        validateReservedTheme(id);
        int deletedCount = themeRepository.deleteById(id);
        if (deletedCount == INVALID_DELETED_COUNT) {
            throw new IllegalArgumentException("존재하지 않는 테마입니다.");
        }
    }

    private void validateReservedTheme(long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalArgumentException("해당 테마를 사용하는 예약이 존재합니다.");
        }
    }

    public List<Theme> findPopularThemes() {
        return themeRepository.findPopularThemesForWeekLimit10(LocalDate.now(clock));
    }

    public List<AvailableTimeResponse> getAvailableTimes(long id, LocalDate date) { // todo time으로 이동
        List<AvailableTimeResponse> responses = new ArrayList<>();
        for (ReservationTime reservationTime : reservationTimeRepository.findAll()) {
            boolean alreadyBooked = reservationRepository.existsByReservationDateTimeAndTheme(date,
                    reservationTime.getId(), id);
            AvailableTimeResponse response = AvailableTimeResponse.from(reservationTime, alreadyBooked);
            responses.add(response);
        }
        return responses;
    }
}
