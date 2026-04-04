package com.janboerman.invsee.faststats;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Java8HttpClient {

    private final int connectTimeoutMs;

    private Java8HttpClient(
            Duration connectTimeout
    ) {
        this.connectTimeoutMs = (int)Objects.requireNonNull(connectTimeout).toMillis();
    }

    public Response send(Request request) throws IOException {
        URL url = request.uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(request.method);
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout((int)request.timeout.toMillis());
        for (Map.Entry<String, String> headerEntry : request.headers.entrySet()) {
            connection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
        }

        try {
            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = request.body;
                outputStream.write(input, 0, input.length);
            }

            int statusCode = connection.getResponseCode();
            InputStream inputStream = (statusCode < HttpURLConnection.HTTP_BAD_REQUEST)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                return new Response(statusCode, response.toString());
            }
        } finally {
            connection.disconnect();
        }
    }

    public static final class Response {
        private final int statusCode;
        private final String body;

        private Response(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int statusCode() {
            return statusCode;
        }

        public String body() {
            return body;
        }
    }

    public static final class Request {
        private final String method;
        private final Map<String, String> headers;
        private final Duration timeout;
        private final URI uri;
        private final byte[] body;

        private Request(
                String method,
                Map<String, String> headers,
                Duration timeout,
                URI uri,
                byte[] body
        ) {
            this.method = method;
            this.headers = headers;
            this.timeout = timeout;
            this.uri = uri;
            this.body = body;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public static final class Builder {
            private String method = "GET";
            private Map<String, String> headers = new HashMap<>();
            private Duration timeout = Duration.ofSeconds(30);
            private URI uri;
            private byte[] body = {};

            public Builder method(String method) {
                this.method = method;
                return this;
            }

            public Builder header(String key, String value) {
                headers.put(key, value);
                return this;
            }

            public Builder timeout(Duration duration) {
                this.timeout = timeout;
                return this;
            }

            public Builder uri(URI uri) {
                this.uri = uri;
                return this;
            }

            public Builder body(byte[] body) {
                this.body = body;
                return this;
            }

            public Builder POST(byte[] body) {
                method("POST");
                body(body);
                return this;
            }

            public Builder PUT(byte[] body) {
                method("PUT");
                body(body);
                return this;
            }

            public Request build() {
                return new Request(
                        method,
                        Collections.unmodifiableMap(headers),
                        timeout,
                        uri,
                        body
                );
            }
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Duration connectTimeout = Duration.ofSeconds(30);

        public Builder connectTimeout(Duration duration) {
            this.connectTimeout = duration;
            return this;
        }

        public Java8HttpClient build() {
            return new Java8HttpClient(
                    connectTimeout
            );
        }
    }
}
