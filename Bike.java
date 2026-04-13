package model;

/**
 * Entité vélo minimale:
 * - id immuable
 * - état mutable (disponible / loué)
 */
public class Bike {
    private final int id;
    private BikeStatus status;

    public Bike(int id) {
        this.id = id;
        this.status = BikeStatus.AVAILABLE;
    }

    public int getId() {
        return id;
    }

    public BikeStatus getStatus() {
        return status;
    }

    // Raccourci de lecture utilisé dans les contrôles métier.
    public boolean isAvailable() {
        return status == BikeStatus.AVAILABLE;
    }

    // Marque le vélo comme indisponible dans une station.
    public void markAsRented() {
        this.status = BikeStatus.RENTED;
    }

    // Marque le vélo comme réintégrable dans une station.
    public void markAsAvailable() {
        this.status = BikeStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "Velo #" + id + " (" + status + ")";
    }
}
