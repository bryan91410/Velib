package ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import model.Station;
import service.ConnectAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class MainController {

    // Widgets injectés depuis MainView.fxml.
    @FXML private ListView<Station> stationListView;
    @FXML private Label availableBikesLabel;
    @FXML private TextArea infoArea;
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> filterValueComboBox;

    // Copie complète des stations reçues de l'API (source de vérité du filtrage).
    private List<Station> allStations = new ArrayList<>();

    @FXML
    public void initialize() {
        // initialize() est appelé automatiquement après le chargement FXML.
        infoArea.setText("Chargement des stations en cours...");

        // Met à jour le panneau de droite quand la sélection de station change.
        stationListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> updateDetailPanel(newVal));

        // Quand on change le type de filtre, on met à jour les valeurs disponibles
        filterTypeComboBox.setItems(FXCollections.observableArrayList(
                "Commune", "Département", "Stations mobiles"
        ));
        filterTypeComboBox.getSelectionModel().selectFirst();
        filterTypeComboBox.setOnAction(e -> onFilterTypeChanged());

        filterValueComboBox.setOnAction(e -> applyFilter());

        loadStationsFromApi();
    }

    /**
     * Charge les stations sur un thread secondaire pour ne pas bloquer l'UI JavaFX.
     * Toute mise à jour visuelle est ensuite rapatriée via Platform.runLater.
     */
    private void loadStationsFromApi() {
        new Thread(() -> {
            ConnectAPI api = new ConnectAPI();
            List<Station> stations = api.fetchStations();

            Platform.runLater(() -> {
                if (stations.isEmpty()) {
                    infoArea.setText("Impossible de charger les stations. Vérifiez votre connexion.");
                    return;
                }

                allStations = stations;
                // Reconstruit immédiatement les options du filtre en fonction des données chargées.
                onFilterTypeChanged();
                infoArea.setText(stations.size() + " stations chargées. Sélectionnez une station.");
            });
        }).start();
    }

    /**
     * Recalcule les valeurs du second combo selon le type choisi:
     * - Commune: liste triée des communes trouvées
     * - Département: liste triée des départements calculés
     * - Stations mobiles: options booléennes (mobile/fixe).
     */
    private void onFilterTypeChanged() {
        String type = filterTypeComboBox.getSelectionModel().getSelectedItem();
        if (type == null) return;

        List<String> values = new ArrayList<>();
        values.add("Tous");

        if (type.equals("Commune")) {
            TreeSet<String> communes = new TreeSet<>();
            for (Station s : allStations) {
                if (!s.getArrondissement().isEmpty()) communes.add(s.getArrondissement());
            }
            values.addAll(communes);

        } else if (type.equals("Département")) {
            TreeSet<String> departements = new TreeSet<>();
            for (Station s : allStations) {
                departements.add(s.getDepartement());
            }
            values.addAll(departements);

        } else if (type.equals("Stations mobiles")) {
            values.clear();
            values.add("Toutes");
            values.add("Mobiles uniquement");
            values.add("Fixes uniquement");
        }

        filterValueComboBox.setItems(FXCollections.observableArrayList(values));
        filterValueComboBox.getSelectionModel().selectFirst();
        applyFilter();
    }

    /**
     * Filtre la liste visible à partir de allStations puis sélectionne
     * automatiquement la première station pour garder un panneau détail cohérent.
     */
    private void applyFilter() {
        String type = filterTypeComboBox.getSelectionModel().getSelectedItem();
        String value = filterValueComboBox.getSelectionModel().getSelectedItem();
        if (type == null || value == null) return;

        List<Station> filtered = new ArrayList<>();

        for (Station s : allStations) {
            if (matches(s, type, value)) {
                filtered.add(s);
            }
        }

        stationListView.setItems(FXCollections.observableArrayList(filtered));

        if (!filtered.isEmpty()) {
            stationListView.getSelectionModel().selectFirst();
        } else {
            availableBikesLabel.setText("");
        }
    }

    /**
     * Règles de correspondance d'une station avec le filtre actif.
     */
    private boolean matches(Station s, String type, String value) {
        return switch (type) {
            case "Commune" -> value.equals("Tous") || value.equals(s.getArrondissement());
            case "Département" -> value.equals("Tous") || value.equals(s.getDepartement());
            case "Stations mobiles" -> switch (value) {
                case "Mobiles uniquement" -> s.getStationCode().startsWith("SM");
                case "Fixes uniquement" -> !s.getStationCode().startsWith("SM");
                default -> true;
            };
            default -> true;
        };
    }

    /**
     * Construit le texte d'information détaillée affiché dans le panneau de droite.
     */
    private void updateDetailPanel(Station station) {
        if (station == null) {
            availableBikesLabel.setText("");
            return;
        }

        String statut = station.isInstalled() ? "Ouverte" : "Fermée";
        String borne  = station.isRenting() ? "Oui" : "Non";

        availableBikesLabel.setText(
            "N° : " + station.getStationCode() + "\n" +
            "Statut : " + statut + "\n" +
            "Borne de paiement : " + borne + "\n" +
            "Capacité : " + station.getTotalSlots() + "\n" +
            "Vélos disponibles : " + station.getNumBikesAvailable() +
                " (méca: " + station.getMechanical() + " / élec: " + station.getEbike() + ")\n" +
            "Places libres : " + station.getNumDocksAvailable()
        );
    }
}
