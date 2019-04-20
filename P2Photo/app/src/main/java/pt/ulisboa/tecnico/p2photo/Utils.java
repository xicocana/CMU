package pt.ulisboa.tecnico.p2photo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;
import pt.ulisboa.tecnico.p2photo.exceptions.UtilsException;

public class Utils {

    public Utils() {
    }

    static void sendMessage(Communications communication, String message) throws UtilsException {
        try {
            communication.sendInChunks(message);
        } catch (CommunicationsException ce) {
            throw new UtilsException("sendMessage(): Communications module broke down.", ce);
        }
    }

    static String receiveMessage(Communications communication) throws UtilsException {
        String receivedString;
        try {
            receivedString = (String) communication.receiveInChunks();
        } catch (CommunicationsException ce) {
            throw new UtilsException("receiveMessage(): Communications module broke down...", ce);
        }
        return receivedString;
    }

    static JSONObject getJSONFromString(String jsonString) throws UtilsException {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch(JSONException je) {
            throw new UtilsException("convertStringToJSON(): String '" + jsonString + "' is not a valid JSON string.", je);
        }

        return jsonObject;
    }

    private static Boolean jsonKeyExists(JSONObject jsonObject, String key) {
        return jsonObject.has(key);
    }

    static Object getObjectByJSONKey(JSONObject jsonObject, String key) throws UtilsException {
        if (!jsonKeyExists(jsonObject, key)) {
            throw new UtilsException("jsonGetObjectByKey(): Key '" + key + "'  does not exist.");
        }
        try {
            Object object = jsonObject.get(key);
            return object;
        } catch(JSONException jsone) {
            throw new UtilsException("getObjectByJSONKey(): Something went wrong while accessing a JSON object...", jsone);
        }
    }

    static Object getObjectByJSONArrayAttribute(JSONArray jsonArray, String attribute) throws UtilsException {
        Object object = null;

        try {
            switch (attribute) {
                case "password":
                    object = jsonArray.get(0);
                    break;
                case "email":
                    object = jsonArray.get(1);
                    break;
                case "token":
                    object = jsonArray.get(2);
                    break;
                default:
                    throw new UtilsException("getObjectByJSONArrayObject(): Attribute '" + attribute + "'  does not exist.");

            }
            return object;
        } catch(JSONException jsone) {
            throw new UtilsException("getObjectByJSONArrayAttribute(): Something went wrong while accessing a JSON array...", jsone);
        }
    }

    static JSONArray changeJSONArrayAttributeByIndex(JSONArray jsonArray, String key, String attribute) throws UtilsException {
        try {
            switch (key) {
                case "password":
                    jsonArray.put(0, attribute);
                    break;
                case "email":
                    jsonArray.put(1, attribute);
                    break;
                case "token":
                    jsonArray.put(2, attribute);
                    break;
                default:
                    throw new UtilsException("changeJSONArrayAttributeByIndex(): Attribute '" + attribute + "'  does not exist.");
            }

            return jsonArray;
        } catch(JSONException jsone) {
            throw new UtilsException("changeJSONArrayAttributeByIndex(): Something went wrong while accessing a JSON array...", jsone);
        }
    }

    static JSONObject changeJSONObjectKeyAttribute(JSONObject jsonObject, String key, Object attribute) throws UtilsException {
        if (!jsonKeyExists(jsonObject, key)) {
            throw new UtilsException("changeJSONObjectKeyAttribute(): Key '" + key + "'  does not exist.");
        }
        jsonObject.remove(key);

        try {
            jsonObject.put(key, attribute);

            return jsonObject;
        } catch(JSONException jsone) {
            throw new UtilsException("changeJSONObjectKeyAttribute(): Something went wrong while accessing a JSON object...", jsone);
        }
    }

}

