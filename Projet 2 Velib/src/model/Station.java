package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Représente une station Vélib avec une capacité fixe de places.
 * Une station ne contient que des vélos disponibles.
 */
public class Station {
    private final int id;
    private final String name;
    private final int totalSlots;
    // Représente les vélos physiquement présents dans la station.
    private final List<Bike> bikes;
    // Compteur maintenu séparément pour éviter de rescanner la liste à chaque fois.
    private int availableBikeCount;

    // Champs issus de l'API Vélib
    private String stationCode;
    private boolean isInstalled;
    private boolean isRenting;
    private int numBikesAvailable;
    private int numDocksAvailable;
    private int mechanical;
    private int ebike;
    private String arrondissement;
    private String codeInseeCommune;

    /**
     * Crée une station valide.
     *
     * @param id         identifiant unique de la station (strictement positif)
     * @param name       nom de la station (non nul, non vide)
     * @param totalSlots nombre total de places (strictement positif)
     */
    public Station(int id, String name, int totalSlots) {
        if (id <= 0) {
            throw new IllegalArgumentException("L'id de la station doit etre strictement positif.");
        }
        if (totalSlots <= 0) {
            throw new IllegalArgumentException("Le nombre de places doit etre strictement positif.");
        }

        String normalizedName = Objects.requireNonNull(name, "Le nom de la station ne peut pas etre null.").trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Le nom de la station ne peut pas etre vide.");
        }

        this.id = id;
        this.name = normalizedName;
        this.totalSlots = totalSlots;
        this.bikes = new ArrayList<>(totalSlots);
        // Aucune insertion au démarrage.
        this.availableBikeCount = 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    /**
     * Retourne une copie immuable des vélos de la station.
     */
    public List<Bike> getBikes() {
        return List.copyOf(bikes);
    }

    public int getBikeCount() {
        return bikes.size();
    }

    public int getAvailableBikeCount() {
        return availableBikeCount;
    }

    public int getFreeSlotsCount() {
        return totalSlots - bikes.size();
    }

    public boolean hasFreeSlot() {
        return bikes.size() < totalSlots;
    }

    public boolean hasAvailableBike() {
        return availableBikeCount > 0;
    }

    public boolean addBike(Bike bike) {
        Objects.requireNonNull(bike, "Le velo ne peut pas etre null.");

        // Cas attendu quand la station est saturée: on ne jette pas d'exception.
        if (!hasFreeSlot()) {
            return false;
        }

        // Validation d'intégrité de la collection interne.
        if (containsBikeId(bike.getId())) {
            throw new IllegalArgumentException("Un velo avec cet id est deja present dans la station.");
        }
        if (!bike.isAvailable()) {
            throw new IllegalArgumentException("Un velo loue ne peut pas etre ajoute dans une station.");
        }

        bikes.add(bike);
        availableBikeCount++;
        return true;
    }

    public Optional<Bike> removeFirstAvailableBike() {
        if (bikes.isEmpty() || availableBikeCount == 0) {
            return Optional.empty();
        }

        // Supprime le premier vélo disponible pour un retrait O(n) simple et lisible.
        for (int i = 0; i < bikes.size(); i++) {
            Bike bike = bikes.get(i);
            if (bike.isAvailable()) {
                bikes.remove(i);
                availableBikeCount--;
                return Optional.of(bike);
            }
        }
        return Optional.empty();
    }

    private boolean containsBikeId(int bikeId) {
        for (Bike bike : bikes) {
            if (bike.getId() == bikeId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Constructeur utilisé pour créer une station depuis l'API Vélib.
     */
    public Station(String stationCode, String name, boolean isInstalled, boolean isRenting,
                   int capacity, int numBikesAvailable, int numDocksAvailable,
                   int mechanical, int ebike, String arrondissement, String codeInseeCommune) {
        // id=0 car l'API fournit son propre identifiant (stationCode) utilisé côté UI.
        this.id = 0;
        this.name = (name != null) ? name.trim() : "Inconnue";
        this.totalSlots = capacity;
        // Dans le mode API, on n'instancie pas chaque vélo individuellement.
        this.bikes = new ArrayList<>();
        // Ce compteur reflète directement la valeur de l'API temps réel.
        this.availableBikeCount = numBikesAvailable;

        this.stationCode = stationCode;
        this.isInstalled = isInstalled;
        this.isRenting = isRenting;
        this.numBikesAvailable = numBikesAvailable;
        this.numDocksAvailable = numDocksAvailable;
        this.mechanical = mechanical;
        this.ebike = ebike;
        this.arrondissement = (arrondissement != null) ? arrondissement : "";
        this.codeInseeCommune = (codeInseeCommune != null) ? codeInseeCommune : "";
    }

    public String getStationCode() { return stationCode; }
    public boolean isInstalled() { return isInstalled; }
    public boolean isRenting() { return isRenting; }
    public int getNumBikesAvailable() { return numBikesAvailable; }
    public int getNumDocksAvailable() { return numDocksAvailable; }
    public int getMechanical() { return mechanical; }
    public int getEbike() { return ebike; }
    public String getArrondissement() { return arrondissement; }
    public String getCodeInseeCommune() { return codeInseeCommune; }

    public String getDepartement() {
        if (codeInseeCommune.length() >= 2) {
            String code = codeInseeCommune.substring(0, 2);
            // Mapping volontairement restreint à l'aire Vélib principale.
            return switch (code) {
                case "75" -> "Paris (75)";
                case "92" -> "Hauts-de-Seine (92)";
                case "93" -> "Seine-Saint-Denis (93)";
                case "94" -> "Val-de-Marne (94)";
                default -> "Autre";
            };
        }
        return "Inconnu";
    }

    @Override
    public String toString() {
        if (stationCode != null) {
            // Affichage pour données API
            return "[" + stationCode + "] " + name + " (" + arrondissement + ")";
        }
        // Affichage pour mode simulation locale (stations + objets Bike).
        return name + " - Velos disponibles: " + availableBikeCount + " / Places libres: " + getFreeSlotsCount();
    }
}
