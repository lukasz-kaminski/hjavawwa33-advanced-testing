package pl.sda.testing.fortuneWatcher;

import java.math.BigDecimal;

class Notifier {
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";

    void warnAboutStalePrice() {
        System.out.println(ANSI_BLUE + "EMAIL SENT: The price of gold couldn't be updated" + ANSI_RESET);
    }

    void warnAboutLowFortune() {
        System.out.println(ANSI_BLUE + "EMAIL SENT: YOUR FORTUNE DROPPED BELOW 1 MILLION PLN" + ANSI_RESET);
    }

    public void notifyAboutDroppingPrice(BigDecimal yesterdaysFortune) {
        System.out.println(ANSI_BLUE + "EMAIL SENT: Your gold was worth more yesterday: " + yesterdaysFortune + ANSI_RESET);
    }
}
