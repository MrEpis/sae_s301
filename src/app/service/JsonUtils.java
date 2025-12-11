package app.service;

public class JsonUtils {

    public static String buildRequest(String actionName, String dataJson) {
        return String.format(
                "{\"type\":\"request\", \"nom\":\"%s\", \"data\":%s}",
                actionName,
                dataJson
        );
    }

    public static String buildCardCreationData(int id_client, String nom, int pv, int atk, int def, String imagePath) {
        return String.format(
                "{\"id_client\":\"%d\", \"nomCarte\":\"%s\", \"pv\":%d, \"attaque\":%d, \"defense\":%d, \"image\":\"%s\"}",
                id_client, nom, pv, atk, def, imagePath
        );
    }

    public static String buildLoginData(int idClient, String username) {
        if (username == null) {
            return String.format("" + idClient);
        } else {
            return String.format(
                    "{\"id_client\": %d, \"username\": \"%s\"}",
                    idClient,
                    username
            );
        }
    }
}