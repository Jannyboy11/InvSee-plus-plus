package com.janboerman.invsee.mojangapi;

import java.io.BufferedReader;
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
import java.util.regex.Pattern;

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

    public CompletableFuture<Optional<UUID>> lookupUniqueId(String username) {
        CompletableFuture<HttpResponse<InputStream>> future = httpClient.sendAsync(HttpRequest
                .newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                .header("Accept", "application/json")
                .header("User-Agent", "InvSee++/MojangAPI")
                .timeout(Duration.ofSeconds(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());

        return future.thenApply((HttpResponse<InputStream> inputStreamResponse) -> {
            int statusCode = inputStreamResponse.statusCode();

            if (statusCode == 200) {
                //ok!
                Charset charset = charsetFromHeaders(inputStreamResponse.headers());
                InputStream inputStream = inputStreamResponse.body();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);

                try {
                    Object json = new JSONParser().parse(inputStreamReader);
                    if (json instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) json;
                        String id = (String) jsonObject.get("id");
                        UUID uuid = dashed(id);
                        return Optional.of(uuid);
                    } else {
                        throw new RuntimeException("Expected player profile to be represented as a JSON Object, instead we got: " + json);
                    }
                } catch (IOException ioe) {
                    throw new RuntimeException("Could not read http response body", ioe);
                } catch (ParseException pe) {
                    throw new RuntimeException("Invalid JSON from Mojang api", pe);
                }
            }

            else if (statusCode == 204) {
                //no content - a player with that username does not exist.
                return Optional.empty();
            }

            else if (statusCode == 400) {
                //bad request
                Charset charset = charsetFromHeaders(inputStreamResponse.headers());
                InputStream inputStream = inputStreamResponse.body();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);

                try {
                    Object json = new JSONParser().parse(inputStreamReader);
                    if (json instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) json;
                        String error = (String) jsonObject.get("error");
                        String errorMessage = (String) jsonObject.get("errorMessage");

                        throw new RuntimeException("We sent a bad request to Mojang. We a(n) " + error + " with the following message: " + errorMessage);
                    } else {
                        throw new RuntimeException("Expected bad request response json to be a JSON Object, instead we got: " + json);
                    }
                } catch (IOException ioe) {
                    throw new RuntimeException("Could not read http response body", ioe);
                } catch (ParseException pe) {
                    throw new RuntimeException("We sent a bad request to Mojang, but Mojang gave us something else than a JSON Object.", pe);
                }
            }

            else {
                //unknown response code - undocumented behaviour from mojang's api
                throw new RuntimeException("Unexpected status code from Mojang API: " + statusCode);
            }
        });
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

    private static UUID dashed(String id) {
        return UUID.fromString(id.substring(0, 8) + '-' +
                id.substring(8, 12) + '-' +
                id.substring(12, 16) + '-' +
                id.substring(16, 20) + '-' +
                id.substring(20, 32));
    }
}
