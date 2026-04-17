package service;

import model.Bike;
import model.Rental;
import model.Station;
import model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VelibService {
    // LinkedHashMap: ordre d'insertion conservé pour un affichage stable.
    private final Map<Integer, Station> stations;
    // Une location active maximum par utilisateur (clé = userId).
    private final Map<Integer, Rental> activeRentalsByUserId;

    public VelibService() {
        this.stations = new LinkedHashMap<>();
        this.activeRentalsByUserId = new HashMap<>();
    }

    /**
     * Enregistre une station dans le service métier.
     * Si l'id existe déjà, la station est remplacée.
     */
    public void addStation(Station station) {
        stations.put(station.getId(), station);
    }

    /**
     * Retourne une copie pour éviter l'exposition directe de la map interne.
     */
    public List<Station> getStations() {
        return new ArrayList<>(stations.values());
    }

    /**
     * Ajoute un vélo disponible dans la station cible.
     * Échec si la station est pleine ou inexistante.
     */
    public void addBikeToStation(int stationId, Bike bike) {
        Station station = getStationOrThrow(stationId);
        boolean added = station.addBike(bike);
        if (!added) {
            throw new IllegalStateException("La station est pleine.");
        }
    }

    /**
     * Démarre une location:
     * 1) vérifie que l'utilisateur n'a pas déjà une location active
     * 2) prélève un vélo disponible dans la station
     * 3) marque le vélo comme loué et crée l'objet Rental.
     */
    public Rental rentBike(User user, int stationId) {
        if (activeRentalsByUserId.containsKey(user.getId())) {
            throw new IllegalStateException("Cet utilisateur a deja un velo en location.");
        }

        Station station = getStationOrThrow(stationId);
        Bike bike = station.removeFirstAvailableBike()
                .orElseThrow(() -> new IllegalStateException("Aucun velo disponible dans cette station."));

        bike.markAsRented();
        Rental rental = new Rental(bike, user, station, LocalDateTime.now());
        activeRentalsByUserId.put(user.getId(), rental);
        return rental;
    }

    /**
     * Termine une location:
     * 1) récupère la location active de l'utilisateur
     * 2) vérifie qu'il y a une place libre dans la station de retour
     * 3) remet le vélo en disponibilité et supprime la location active.
     */
    public Rental returnBike(User user, int stationId) {
        Rental rental = activeRentalsByUserId.get(user.getId());
        if (rental == null) {
            throw new IllegalStateException("Aucun velo en cours de location pour cet utilisateur.");
        }

        Station station = getStationOrThrow(stationId);
        if (!station.hasFreeSlot()) {
            throw new IllegalStateException("Impossible de rendre le velo: station pleine.");
        }

        Bike bike = rental.getBike();
        bike.markAsAvailable();
        station.addBike(bike);
        activeRentalsByUserId.remove(user.getId());
        return rental;
    }

    /**
     * Produit une vue texte synthétique des stations (debug / console).
     */
    public String getStationsState() {
        StringBuilder state = new StringBuilder();
        for (Station station : stations.values()) {
            state.append(station.getName())
                    .append(" -> velos: ")
                    .append(station.getAvailableBikeCount())
                    .append(", places libres: ")
                    .append(station.getFreeSlotsCount())
                    .append("\n");
        }
        return state.toString();
    }

    /**
     * Permet à l'UI ou aux contrôles métier de savoir si un utilisateur
     * est déjà en location.
     */
    public boolean hasActiveRental(User user) {
        return activeRentalsByUserId.containsKey(user.getId());
    }

    /**
     * Centralise la validation d'existence d'une station.
     */
    private Station getStationOrThrow(int stationId) {
        Station station = stations.get(stationId);
        if (station == null) {
            throw new IllegalArgumentException("Station introuvable (id=" + stationId + ").");
        }
        return station;
    }
}
