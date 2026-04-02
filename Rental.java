package model;

import java.time.LocalDateTime;

/**
 * Représente une location active (ou historique selon usage).
 * Cet objet relie un vélo, un utilisateur, une station de départ et une date.
 */
public class Rental {
    private final Bike bike;
    private final User user;
    private final Station startStation;
    private final LocalDateTime rentalTime;

    public Rental(Bike bike, User user, Station startStation, LocalDateTime rentalTime) {
        this.bike = bike;
        this.user = user;
        this.startStation = startStation;
        this.rentalTime = rentalTime;
    }

    public Bike getBike() {
        return bike;
    }

    public User getUser() {
        return user;
    }

    public Station getStartStation() {
        return startStation;
    }

    public LocalDateTime getRentalTime() {
        return rentalTime;
    }
}
