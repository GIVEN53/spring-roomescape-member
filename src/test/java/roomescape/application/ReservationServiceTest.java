package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import roomescape.application.dto.ReservationCreationRequest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.ReservationTime;
import roomescape.domain.time.repository.ReservationTimeRepository;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.support.annotation.FixedClock;
import roomescape.support.extension.MockClockExtension;
import roomescape.support.extension.TableTruncateExtension;

@SpringBootTest
@ExtendWith({TableTruncateExtension.class, MockClockExtension.class})
@FixedClock(date = "2024-04-20")
public class ReservationServiceTest {
    private final ReservationTime time = ReservationFixture.reservationTime();
    private final Theme theme = ThemeFixture.theme();

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
    }

    @Test
    void 예약을_성공한다() {
        LocalDate date = LocalDate.of(2024, 4, 21);
        ReservationCreationRequest request = new ReservationCreationRequest("prin", date, time.getId(), theme.getId());

        Reservation reservation = reservationService.reserve(request);

        assertThat(reservation.getName()).isEqualTo("prin");
        assertThat(reservation.getDate()).isEqualTo(date);
        assertThat(reservation.getTime()).isEqualTo(time.getStartAt());
    }

    @Test
    void 최소_1일_전에_예약하지_않으면_예약을_실패한다() {
        LocalDate invalidDate = LocalDate.of(2024, 4, 20);
        ReservationCreationRequest request = new ReservationCreationRequest("liv", invalidDate, time.getId(),
                theme.getId());

        assertThatThrownBy(() -> reservationService.reserve(request))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약은 최소 1일 전에 해야합니다.");
    }

    @Test
    void 중복된_예약이_있으면_예약을_실패한다() {
        LocalDate date = LocalDate.of(2024, 4, 21);
        ReservationCreationRequest request = new ReservationCreationRequest("sudal", date, time.getId(), theme.getId());
        reservationService.reserve(request);

        assertThatThrownBy(() -> reservationService.reserve(request))
                .isExactlyInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 예약을_취소한다() {
        LocalDate date = LocalDate.of(2024, 4, 21);
        ReservationCreationRequest request = new ReservationCreationRequest("prin", date, time.getId(), theme.getId());
        Reservation reservation = reservationService.reserve(request);

        reservationService.cancel(reservation.getId());

        assertThat(reservationService.findReservations()).isEmpty();
    }

    @Test
    void 존재하지_않는_예약을_취소하면_예외가_발생한다() {
        assertThatThrownBy(() -> reservationService.cancel(1L))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 예약입니다.");
    }
}
