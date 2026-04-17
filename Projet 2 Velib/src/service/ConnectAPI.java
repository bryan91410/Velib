package service;

import model.Station;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class ConnectAPI {

    // Endpoint OpenData Paris (jeu de données de disponibilité en temps réel).
    private static final String API_BASE =
            "https://opendata.paris.fr/api/explore/v2.1/catalog/datasets/" +
            "velib-disponibilite-en-temps-reel/records";

    // Taille de page utilisée pour limiter chaque requête HTTP.
    private static final int PAGE_SIZE = 100;

    /**
     * Appelle l'API Vélib avec pagination et retourne toutes les stations.
     * Retourne une liste vide en cas d'erreur réseau ou de parsing.
     */
    public List<Station> fetchStations() {
        List<Station> stations = new ArrayList<>();

        try {
            // Première requête pour connaître le nombre total
            String firstUrl = API_BASE + "?limit=" + PAGE_SIZE + "&offset=0";
            String firstResponse = callApi(firstUrl);
            JSONObject root = parseResponse(firstResponse);

            int totalCount = root.optInt("total_count", 0);
            parseAndAdd(root.getJSONArray("results"), stations);

            // Pages suivantes : on itère tant qu'il reste des stations à charger.
            int offset = PAGE_SIZE;
            while (offset < totalCount) {
                String url = API_BASE + "?limit=" + PAGE_SIZE + "&offset=" + offset;
                String response = callApi(url);
                JSONObject page = parseResponse(response);
                parseAndAdd(page.getJSONArray("results"), stations);
                offset += PAGE_SIZE;
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'API Vélib : " + e.getMessage());
        }

        return stations;
    }

    /**
     * Convertit chaque objet JSON en Station puis l'ajoute à la liste finale.
     * Les objets invalides sont ignorés (parseStation retourne null).
     */
    private void parseAndAdd(JSONArray results, List<Station> stations) {
        for (int i = 0; i < results.length(); i++) {
            Station station = parseStation(results.getJSONObject(i));
            if (station != null) {
                stations.add(station);
            }
        }
    }

    /**
     * Traduit une entrée JSON de l'API vers le modèle Station du projet.
     * Les valeurs manquantes sont remplacées par des valeurs par défaut.
     */
    private Station parseStation(JSONObject obj) {
        try {
            String stationCode      = obj.optString("stationcode", "");
            String name             = obj.optString("name", "");
            boolean isInstalled     = "OUI".equalsIgnoreCase(obj.optString("is_installed", "NON"));
            boolean isRenting       = "OUI".equalsIgnoreCase(obj.optString("is_renting", "NON"));
            int capacity            = obj.optInt("capacity", 0);
            int numBikesAvailable   = obj.optInt("numbikesavailable", 0);
            int numDocksAvailable   = obj.optInt("numdocksavailable", 0);
            int mechanical          = obj.optInt("mechanical", 0);
            int ebike               = obj.optInt("ebike", 0);
            String arrondissement   = obj.optString("nom_arrondissement_communes", "");
            String codeInsee        = obj.optString("code_insee_commune", "");

            return new Station(stationCode, name, isInstalled, isRenting, capacity,
                               numBikesAvailable, numDocksAvailable,
                               mechanical, ebike, arrondissement, codeInsee);
        } catch (Exception e) {
            System.err.println("Erreur parsing station : " + e.getMessage());
            return null;
        }
    }

    /**
     * Réalise une requête GET et renvoie le corps de réponse brut en String.
     * Lance une exception si le code HTTP n'est pas 200.
     */
    private String callApi(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        // Timeouts courts pour éviter de bloquer l'UI trop longtemps.
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Code HTTP inattendu : " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        InputStream rawStream = connection.getInputStream();
        String encoding = connection.getContentEncoding();
        InputStream decodedStream = decodeStream(rawStream, encoding);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(decodedStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    /**
     * Décompresse le flux HTTP si le serveur a appliqué gzip/deflate.
     */
    private InputStream decodeStream(InputStream rawStream, String encoding) throws Exception {
        if (encoding == null || encoding.isBlank()) {
            return rawStream;
        }

        String normalized = encoding.toLowerCase();
        if (normalized.contains("gzip")) {
            return new GZIPInputStream(rawStream);
        }
        if (normalized.contains("deflate")) {
            return new InflaterInputStream(rawStream);
        }
        return rawStream;
    }

    /**
     * Nettoie et parse la réponse JSON de façon robuste (BOM, format inattendu).
     */
    private JSONObject parseResponse(String rawResponse) throws Exception {
        String response = sanitizeJson(rawResponse);
        if (response.isEmpty()) {
            throw new Exception("Réponse API vide.");
        }
        if (response.charAt(0) == '<') {
            throw new Exception("Réponse HTML reçue à la place d'un JSON.");
        }

        Object parsed = new JSONTokener(response).nextValue();
        if (parsed instanceof JSONObject) {
            return (JSONObject) parsed;
        }
        if (parsed instanceof JSONArray) {
            JSONArray results = (JSONArray) parsed;
            return new JSONObject()
                    .put("total_count", results.length())
                    .put("results", results);
        }
        throw new Exception("Format JSON inattendu : " + parsed.getClass().getSimpleName());
    }

    /**
     * Supprime le BOM UTF-8 éventuel et les espaces parasites de début/fin.
     */
    private String sanitizeJson(String raw) {
        if (raw == null) {
            return "";
        }
        String sanitized = raw.strip();
        if (!sanitized.isEmpty() && sanitized.charAt(0) == '\uFEFF') {
            sanitized = sanitized.substring(1).stripLeading();
        }
        return sanitized;
    }
}
