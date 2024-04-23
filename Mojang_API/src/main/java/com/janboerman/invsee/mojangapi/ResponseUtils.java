package com.janboerman.invsee.mojangapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class ResponseUtils {

    private ResponseUtils() {}

    static JSONObject readJSONObject(HttpURLConnection connection) {
        Charset charset = charsetFromHeader(connection.getHeaderField("Content-Type"));
        try (InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset)) {

            Object json = new JSONParser().parse(inputStreamReader);
            if (json instanceof JSONObject) {
                return (JSONObject) json;
            } else {
                throw new RuntimeException("Expected response to be represented as a JSON Object, instead we got: " + json);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Could not read http response body", ioe);
        } catch (ParseException pe) {
            throw new RuntimeException("Invalid JSON from api", pe);
        }
    }


    private static Charset charsetFromHeader(String contentType) {
        if (contentType != null) {
            int indexOfSemi = contentType.indexOf(';');
            if (indexOfSemi != -1) {
                String charsetPart = contentType.substring(indexOfSemi + 1).trim();
                String[] charSetKeyAndValue = charsetPart.split("=", 2);
                if (charSetKeyAndValue.length == 2 && "charset".equalsIgnoreCase(charSetKeyAndValue[0])) {
                    String charsetName = charSetKeyAndValue[1];
                    return Charset.forName(charsetName);
                }
            }
        }

        return StandardCharsets.UTF_8;
    }
}
