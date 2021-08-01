package pl.sda.testing.fortuneWatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sda.testing.fortuneWatcher.provider.GoldPriceProvider;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FortuneWatcherTest {

    @Mock
    Notifier notifier;

    @Mock
    GoldPriceProvider goldPriceProvider;

    @InjectMocks
    FortuneWatcher fortuneWatcher;

    @Test
    void shouldCalculateFortuneAmount() {
        //given
        when(goldPriceProvider.getTodaysPrice())
                .thenReturn(Optional.of(new BigDecimal(100)));

        //when
        BigDecimal fortune = fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100)));
        //then
        assertEquals(new BigDecimal(100 * 100), fortune);
    }

    @Test
    void shouldSendEmailWhenFortuneIsLow() {
        //given
        when(goldPriceProvider.getTodaysPrice())
                .thenReturn(Optional.of(new BigDecimal(100)));
        //when
        fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100)));
        //then
        verify(notifier).warnAboutLowFortune();
        verifyNoMoreInteractions(notifier);
    }

    @Test
    void shouldNotNotifyWhenFortuneOK() {
        //given
        when(goldPriceProvider.getTodaysPrice())
                .thenReturn(Optional.of(new BigDecimal(100)));
        //when
        fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100000)));
        //then
        verifyNoInteractions(notifier);
    }

    @Test
    void shouldSendEmailWhenFortuneIsLowAndPriceIsStale() {
        //given
        when(goldPriceProvider.getTodaysPrice()).thenReturn(Optional.empty());
        when(goldPriceProvider.getLastAvailableGoldPrice()).thenReturn(new BigDecimal(100));
        //when
        fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100)));
        //then
        InOrder inOrder = inOrder(notifier);
        inOrder.verify(notifier).warnAboutStalePrice();
        inOrder.verify(notifier).warnAboutLowFortune();
        inOrder.verifyNoMoreInteractions();

    }

    @Test
    void shouldSendEmailWhenYesterdaysFortuneIsBigger() {
        //given
        when(goldPriceProvider.getPriceForDate(argThat(arg -> arg.isBefore(LocalDate.now()))))
                .thenReturn(Optional.of(new BigDecimal("100")));
        when(goldPriceProvider.getTodaysPrice())
                .thenReturn(Optional.of(new BigDecimal(50)));
        //when
        fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100)));
        //then
        verify(notifier).notifyAboutDroppingPrice(eq(new BigDecimal(10000)));

    }

    @Test
    void should() {
        //given
        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);

        when(goldPriceProvider.getPriceForDate(LocalDate.now().minusDays(1)))
                .thenReturn(Optional.of(new BigDecimal("100")));
        when(goldPriceProvider.getTodaysPrice())
                .thenReturn(Optional.of(new BigDecimal(50)));
        //when
        fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100)));
        //then
        verify(notifier).notifyAboutDroppingPrice(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new BigDecimal(10000));
    }

    @Test
    void shouldNotThrowExceptionWhenNoPriceForToday() {
        //given
        when(goldPriceProvider.getTodaysPrice()).thenReturn(Optional.empty());
        when(goldPriceProvider.getLastAvailableGoldPrice()).thenReturn(new BigDecimal(100));

        //expect
        assertDoesNotThrow(() -> fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100))));
        try {
            fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100)));
        } catch (NullPointerException e) {
            fail();
        }
    }

    @Test
    void shouldThrowExceptionWhenNoPriceForYesterday() {
        //given
        when(goldPriceProvider.getTodaysPrice()).thenReturn(Optional.empty());

        //expect
        assertThrows(NullPointerException.class, () -> fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100))));

        try {
            fortuneWatcher.assessFortune(Fortune.ofGoldKgs(new BigDecimal(100)));
            fail();
        } catch (NullPointerException e) {
        }
    }

}