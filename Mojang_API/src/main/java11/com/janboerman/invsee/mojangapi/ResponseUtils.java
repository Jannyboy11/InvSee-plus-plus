package com.janboerman.invsee.mojangapi;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

final class ResponseUtils {

    private ResponseUtils() {}

    static JSONObject readJSONObject(HttpResponse<InputStream> response) {
        Charset charset = charsetFromHeaders(response.headers());
        try (InputStream inputStream = response.body();
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

    /*
    static JSONArray readJSONArray(HttpResponse<InputStream> response) {
        Charset charset = charsetFromHeaders(response.headers());
        try (InputStream inputStream = response.body();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset)) {

            Object json = new JSONParser().parse(inputStreamReader);
            if (json instanceof JSONArray) {
                return (JSONArray) json;
            } else {
                throw new RuntimeException("Expected response to be represented as a JSON Array, instead we got: " + json);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Could not read http response body", ioe);
        } catch (ParseException pe) {
            throw new RuntimeException("Invalid JSON from Mojang api", pe);
        }
    }
    */

    private static Charset charsetFromHeaders(HttpHeaders headers) {
        Optional<String> optionalContentType = headers.firstValue("Content-Type");
        if (optionalContentType.isPresent()) {
            String contentType = optionalContentType.get();
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

        //fallback
        return StandardCharsets.UTF_8;
    }

}
