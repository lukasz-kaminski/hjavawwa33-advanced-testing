package pl.sda.testing.fortuneWatcher.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoldPriceProviderTest {
    @Mock
    HttpClient httpClient;

    @InjectMocks
    GoldPriceProvider goldPriceProvider;

    @Test
    void shouldReadPriceFromServerResponse() throws IOException, InterruptedException {
        //given
        HttpResponse response = mock(HttpResponse.class);
        when(response.body()).thenReturn("[{\"cena\": \"100\"}]");

        when(httpClient.send(any(), any())).thenReturn(response);

        //when
        Optional<BigDecimal> todaysPrice = goldPriceProvider.getTodaysPrice();

        //then
        assertTrue(todaysPrice.isPresent());
        assertEquals(new BigDecimal(100), todaysPrice.get());
    }

    @Test
    void shouldReturnEmptyOptionalOnTodaysPriceAndNotEmptyOnLastPrice() throws IOException, InterruptedException {
        //given
        HttpResponse todaysPriceResponse = mock(HttpResponse.class);
        when(todaysPriceResponse.body()).thenReturn("");

        HttpResponse lastAvailablePriceResponse = mock(HttpResponse.class);
        when(lastAvailablePriceResponse.body()).thenReturn("[{\"cena\": \"200\"}]");

        when(httpClient.send(
                eq(HttpRequest.newBuilder().uri(URI.create("http://api.nbp.pl/api/cenyzlota/")).build()),
                any()
        )).thenReturn(todaysPriceResponse);

        when(httpClient.send(
                eq(HttpRequest.newBuilder().uri(URI.create("http://api.nbp.pl/api/cenyzlota/last/1")).build()),
                any()
        )).thenReturn(lastAvailablePriceResponse);

        //when
        Optional<BigDecimal> todaysPrice = goldPriceProvider.getTodaysPrice();
        BigDecimal lastAvailableGoldPrice = goldPriceProvider.getLastAvailableGoldPrice();
        //then

        assertTrue(todaysPrice.isEmpty());
        assertEquals(new BigDecimal(200), lastAvailableGoldPrice);

    }


}