package com.janboerman.invsee.mojangapi;

import org.json.simple.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.janboerman.invsee.mojangapi.ResponseUtils.*;

/**
 * API to resolve players' usernames and unique IDs via <a href="https://github.com/Electroid/mojang-api">Electroid/mojang-api</a>.
 */
public class ElectroidAPI {
    //https://github.com/Electroid/mojang-api

    private final HttpClient httpClient;

    public ElectroidAPI(Executor asyncExecutor) {
        this.httpClient = HttpClient.newBuilder()
                .executor(asyncExecutor)
                .build();
    }

    @Deprecated
    public ElectroidAPI(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Deprecated
    public ElectroidAPI() {
        this(HttpClient.newHttpClient());
    }

    public CompletableFuture<Optional<UUID>> lookupUniqueId(String userName) {
        CompletableFuture<HttpResponse<InputStream>> future = httpClient.sendAsync(HttpRequest
                .newBuilder(URI.create("https://api.ashcon.app/mojang/v2/user/" + userName))
                .header("Accept", "application/json")
                .header("User-Agent", "InvSee++/ElectroidAPI")
                .timeout(Duration.ofSeconds(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());

        return future.thenApply(response -> {
            if (response.statusCode() == 200) {
                //ok!
                JSONObject json = readJSONObject(response);
                String uuid = (String) json.get("uuid");        // already contains dashes.
                return Optional.of(UUID.fromString(uuid));
            } else if (response.statusCode() == 204) {
                //no content
                return Optional.empty();
            } else {
                //unexpected
                throw new RuntimeException("Unexpected response from Electroid mojang api, status code=" + response.statusCode() + ".");
            }
        });
    }

    public CompletableFuture<Optional<String>> lookupUserName(UUID uniqueId) {
        CompletableFuture<HttpResponse<InputStream>> future = httpClient.sendAsync(HttpRequest
                .newBuilder(URI.create("https://api.ashcon.app/mojang/v2/user/" + uniqueId.toString()))     // uuid with dashes allowed
                .header("Accept", "application/json")
                .header("User-Agent", "InvSee++/ElectroidAPI")
                .timeout(Duration.ofSeconds(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());

        return future.thenApply(response -> {
            if (response.statusCode() == 200) {
                //ok!
                JSONObject json = readJSONObject(response);
                String userName = (String) json.get("username");
                return Optional.of(userName);
            } else if (response.statusCode() == 204) {
                //no content
                return Optional.empty();
            } else {
                //unexpected
                throw new RuntimeException("Unexpected response from Electroid mojang api, status code=" + response.statusCode() + ".");
            }
        });
    }
}
