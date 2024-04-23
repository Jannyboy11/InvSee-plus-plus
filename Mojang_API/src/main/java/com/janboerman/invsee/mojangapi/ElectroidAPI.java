package com.janboerman.invsee.mojangapi;

import static com.janboerman.invsee.mojangapi.ResponseUtils.readJSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.janboerman.invsee.utils.Rethrow;

import org.json.simple.JSONObject;

public class ElectroidAPI {

    private final Executor asyncExecutor;

    public ElectroidAPI(Executor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    public CompletableFuture<Optional<UUID>> lookupUniqueId(String userName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://api.ashcon.app/mojang/v2/user/" + userName);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5 * 1000); //5 seconds
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "InvSee++/MojangAPI");

                int statusCode = connection.getResponseCode();
                switch (statusCode) {
                    case HttpURLConnection.HTTP_OK:
                        JSONObject json = readJSONObject(connection);
                        String uuid = (String) json.get("uuid");
                        return Optional.of(UUID.fromString(uuid));
                    case HttpURLConnection.HTTP_NO_CONTENT:
                        return Optional.empty();
                    default:
                        throw new RuntimeException("Unexpected response from Electroid mojang api, status code=" + statusCode + ".");
                }
            } catch (IOException e) {
                return Rethrow.unchecked(e);
            }
        }, asyncExecutor);
    }

    public CompletableFuture<Optional<String>> lookupUserName(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://api.ashcon.app/mojang/v2/user/" + uniqueId.toString());
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5 * 1000); //5 seconds
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "InvSee++/MojangAPI");

                int statusCode = connection.getResponseCode();
                switch (statusCode) {
                    case HttpURLConnection.HTTP_OK:
                        JSONObject json = readJSONObject(connection);
                        String userName = (String) json.get("username");
                        return Optional.of(userName);
                    case HttpURLConnection.HTTP_NO_CONTENT:
                        return Optional.empty();
                    default:
                        throw new RuntimeException("Unexpected response from Electroid mojang api, status code=" + statusCode + ".");
                }
            } catch (IOException e) {
                return Rethrow.unchecked(e);
            }
        }, asyncExecutor);
    }

}