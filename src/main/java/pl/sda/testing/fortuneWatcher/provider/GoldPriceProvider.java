package pl.sda.testing.fortuneWatcher.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Optional;

public class GoldPriceProvider {
    private static final URI NBP_TODAY_URI = URI.create("http://api.nbp.pl/api/cenyzlota/");
    private static final URI NBP_LATEST_URI = URI.create("http://api.nbp.pl/api/cenyzlota/last/1");
    private static final URI NBP_LATEST_2_URI = URI.create("http://api.nbp.pl/api/cenyzlota/last/2");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient;

    public GoldPriceProvider(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Optional<BigDecimal> getPriceForDate(LocalDate date) {
        return fetchGoldPrice(NBP_LATEST_2_URI);
    }

    public Optional<BigDecimal> getTodaysPrice() {
        return fetchGoldPrice(NBP_TODAY_URI);
    }

    public BigDecimal getLastAvailableGoldPrice() {
        return fetchGoldPrice(NBP_LATEST_URI).orElseThrow(NoPriceAvailableException::new);
    }

    private Optional<BigDecimal> fetchGoldPrice(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            PriceResponse[] priceResponses = OBJECT_MAPPER.readValue(response.body(), PriceResponse[].class);
            return Optional.of(priceResponses[0].getPrice());
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PriceResponse {
        @JsonProperty("cena")
        private BigDecimal price;

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
