package pl.sda.testing.fortuneWatcher;

import java.math.BigDecimal;

class Fortune {
    private final BigDecimal goldKgs;

    private Fortune(BigDecimal goldKgs) {
        this.goldKgs = goldKgs;
    }

    static Fortune ofGoldKgs(BigDecimal goldKgs) {
        return new Fortune(goldKgs);
    }

    BigDecimal getGoldKgs() {
        return goldKgs;
    }
}
