package com.janboerman.invsee.mojangapi;

import com.janboerman.invsee.utils.UUIDHelper;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.json.simple.JSONObject;

import static com.janboerman.invsee.mojangapi.ResponseUtils.*;

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
        //no longer use name history api: https://wiki.vg/Mojang_API#UUID_to_Name_History_.28Removed.29,
        //use profile api instead:
        CompletableFuture<HttpResponse<InputStream>> future = httpClient.sendAsync(HttpRequest
                .newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + UUIDHelper.unDashed(uniqueId)))
                .header("Accept", "application/json")
                .header("User-Agent", "InvSee++/MojangAPI")
                .timeout(Duration.ofSeconds(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());

        return future.thenApply((HttpResponse<InputStream> response) -> {
           int statusCode = response.statusCode();

           if (statusCode == 200) {
               //ok!
               JSONObject profileJson = readJSONObject(response);
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



}
