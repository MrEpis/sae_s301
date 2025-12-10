package app.service;

public class JsonUtils {

    public static String buildRequest(String actionName, String dataJson) {
        return String.format(
                "{\"type\":\"request\", \"nom\":\"%s\", \"data\":%s}",
                actionName,
                dataJson
        );
    }

    public static String buildCardCreationData(String nom, int pv, int atk) {
        return String.format(
                "{\"nomCarte\":\"%s\", \"pv\":%d, \"attaque\":%d}",
                nom, pv, atk
        );
    }

    // NOUVEAU : Formatage pour le LOGIN
    public static String buildLoginData(int idClient, String username) {
        return String.format(
                "{\"id_client\": %d, \"username\": \"%s\"}",
                idClient,
                username
        );
    }
}