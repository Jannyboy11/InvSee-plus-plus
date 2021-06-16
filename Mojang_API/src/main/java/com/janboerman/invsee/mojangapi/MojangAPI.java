package com.janboerman.invsee.mojangapi;

import com.janboerman.invsee.utils.UUIDHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MojangAPI {
    //https://wiki.vg/Mojang_API

    private final HttpClient httpClient;

    /**
     * Creates the Mojang API instance using the given HTTP client for all requests.
     * @param httpClient the client to use for HTTP requests
     */
    public MojangAPI(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * Creates the instance of the Mojang API using {@link HttpClient#newHttpClient()}.
     */
    public MojangAPI() {
        this(HttpClient.newHttpClient());
    }

    public CompletableFuture<Optional<UUID>> lookupUniqueId(String userName) {
        CompletableFuture<HttpResponse<InputStream>> future = httpClient.sendAsync(HttpRequest
                .newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + userName))
                .header("Accept", "application/json")
                .header("User-Agent", "InvSee++/MojangAPI")
                .timeout(Duration.ofSeconds(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());

        return future.thenApply((HttpResponse<InputStream> response) -> {
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                //ok!
                JSONObject json = readJSONObject(response);
                String id = (String) json.get("id");
                UUID uuid = UUIDHelper.dashed(id);
                return Optional.of(uuid);
            }

            else {
                //not ok
                return handleNotOk(response);
            }
        });
    }

    public CompletableFuture<Optional<String>> lookupUserName(UUID uniqueId) {
        CompletableFuture<HttpResponse<InputStream>> future = httpClient.sendAsync(HttpRequest
                .newBuilder(URI.create("https://api.mojang.com/user/profiles/" + UUIDHelper.unDashed(uniqueId) + "/names"))
                .header("Accept", "application/json")
                .header("User-Agent", "InvSee++/MojangAPI")
                .timeout(Duration.ofSeconds(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());

        return future.thenApply((HttpResponse<InputStream> response) -> {
           int statusCode = response.statusCode();

           if (statusCode == 200) {
               //ok!
               JSONArray json = readJSONArray(response);
               JSONObject profileJson = (JSONObject) json.get(0);
               String userName = (String) profileJson.get("name");
               return Optional.of(userName);
           }

           else {
               //not ok
               return handleNotOk(response);
           }
        });
    }

    private static <T> Optional<T> handleNotOk(HttpResponse<InputStream> response) {
        int statusCode = response.statusCode();

        if (statusCode == 204) {
            //no content - a player with that username does not exist.
            return handleNoContent(response);
        }

        else if (statusCode == 400) {
            //bad request
            return handleBadRequest(response);
        }

        else {
            //unknown response code - undocumented behaviour from mojang's api
            return handleUnknownStatusCode(response);
        }
    }

    private static JSONObject readJSONObject(HttpResponse<InputStream> response) {
        Charset charset = charsetFromHeaders(response.headers());
        InputStream inputStream = response.body();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);

        try {
            Object json = new JSONParser().parse(inputStreamReader);
            if (json instanceof JSONObject) {
                return (JSONObject) json;
            } else {
                throw new RuntimeException("Expected response to be represented as a JSON Object, instead we got: " + json);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Could not read http response body", ioe);
        } catch (ParseException pe) {
            throw new RuntimeException("Invalid JSON from Mojang api", pe);
        }
    }

    private static JSONArray readJSONArray(HttpResponse<InputStream> response) {
        Charset charset = charsetFromHeaders(response.headers());
        InputStream inputStream = response.body();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);

        try {
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

    private static <T> Optional<T> handleNoContent(HttpResponse<InputStream> response) {
        return Optional.empty();
    }

    private static <T> Optional<T> handleBadRequest(HttpResponse<InputStream> response) {
        JSONObject jsonObject = readJSONObject(response);

        String error = (String) jsonObject.get("error");
        String errorMessage = (String) jsonObject.get("errorMessage");

        throw new RuntimeException("We sent a bad request to Mojang. We got a(n) " + error + " with the following message: " + errorMessage);
    }

    private static <T> Optional<T> handleUnknownStatusCode(HttpResponse<InputStream> response) {
        throw new RuntimeException("Unexpected status code from Mojang API: " + response.statusCode());
    }

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
