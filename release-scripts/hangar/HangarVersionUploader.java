//| mvnDeps:
//|   - com.google.code.gson:gson:2.13.2
//|   - org.apache.httpcomponents.client5:httpclient5:5.6
//|   - org.slf4j:slf4j-api:2.0.17
//|   - org.slf4j:slf4j-jdk14:2.0.17
//|   - org.checkerframework:checker-qual:4.0.0
//|   - org.yaml:snakeyaml:2.6

// Adapted from: https://gist.github.com/kennytv/a227d82249f54e0ad35005330256fee2

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class HangarVersionUploader {

    private static final String HANGAR_API_PRODUCTION = "https://hangar.papermc.io/api/v1";
    private static final String HANGAR_API_STAGING = "https://hangar.papermc.io/api/v1";
    private static final String HANGAR_API_LOCAL = "http://localhost:3333/api/v1";
    private static final Logger LOGGER = LoggerFactory.getLogger(HangarVersionUploader.class);
    private static final String HANGAR_API_URL = HANGAR_API_PRODUCTION;
    private static final Gson GSON = new Gson();
    private final String apiKey;
    private ActiveJWT activeJWT;

    public HangarVersionUploader(final String apiKey) {
        this.apiKey = apiKey;
    }

    private static String readPluginVersion(Path pluginFile) {
        try (ZipFile zipFile = new ZipFile(pluginFile.toFile())) {

            ZipEntry entry = zipFile.getEntry("plugin.yml");

            if (entry != null) {
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(inputStream);
                    return (String) data.get("version");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("plugin.yml file inside plugin jar missing.");
    }

    private static String getApiKey() {
        return System.getenv("HANGAR_TOKEN");
    }

    private static String[] getMinecraftVersions() {
        return System.getenv("MINECRAFT_VERSIONS").split(" ");
    }

    public static void main(final String[] args) throws IOException {
        final String projectName = "InvSee-plus-plus";
        final String apiKey = getApiKey();
        final List<Path> filePaths = List.of(
                Path.of("plugins/InvSee++.jar"),
                Path.of("plugins/InvSee++_Give.jar"),
                Path.of("plugins/InvSee++_Clear.jar")
        );

        String pluginVersion = readPluginVersion(filePaths.get(0));
        List<String> minecraftVersions = List.of(getMinecraftVersions());
        String channel = pluginVersion.endsWith("-SNAPSHOT") ? "Alpha" : "Release";

        final List<MultipartFileOrUrl> fileInfo = List.of(
                new MultipartFileOrUrl(List.of(Platform.PAPER), null)
                // TODO are these wrong? should they be here for InvSee++_Give and InvSee++_Clear? Hangar rejects them when I use these.
//                new MultipartFileOrUrl(List.of(Platform.PAPER), null),
//                new MultipartFileOrUrl(List.of(Platform.PAPER), null)
        );
        final VersionUpload versionUpload = new VersionUpload(
                pluginVersion,
                Map.of(
                        Platform.PAPER, List.of()
                ),
                Map.of(
                        Platform.PAPER, minecraftVersions
                ),
                "Automated deployment for InvSee++ v" + pluginVersion,
                fileInfo,
                channel
        );
        LOGGER.info("VersionUpload: {}.", versionUpload);

        final HangarVersionUploader uploader = new HangarVersionUploader(apiKey);
        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            uploader.uploadVersion(client, projectName, versionUpload, filePaths);
        }
    }

    /**
     * Uploads a new version to Hangar.
     *
     * @param client        http client to use
     * @param project       unique project name
     * @param versionUpload version upload data
     * @param filePaths     paths to the files to upload for platforms without external urls
     * @throws IOException if an error occurs while uploading
     */
    public void uploadVersion(
            final HttpClient client,
            final String project,
            final VersionUpload versionUpload,
            final List<Path> filePaths
    ) throws IOException {
        // The data needs to be sent as multipart form data
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("versionUpload", new StringBody(GSON.toJson(versionUpload), ContentType.APPLICATION_JSON));

        // Attach files (one file for each platform where no external url is defined in the version upload data)
        for (final Path filePath : filePaths) {
            LOGGER.info("Adding file {} as multipart.", filePath.toAbsolutePath());
            builder.addPart("files", new FileBody(filePath.toFile(), ContentType.DEFAULT_BINARY));
        }

        final HttpPost post = new HttpPost("%s/projects/%s/upload".formatted(HANGAR_API_URL, project));
        post.setEntity(builder.build());
        this.addAuthorizationHeader(client, post);

        final boolean success = client.execute(post, response -> {
            if (response.getCode() != 200) {
                LOGGER.error("Error uploading version {}: {}", response.getCode(), response.getReasonPhrase());
                if (response instanceof org.apache.hc.core5.http.HttpEntityContainer entityResponse) {
                    var httpEntity = response.getEntity();
                    var contents = new String(httpEntity.getContent().readAllBytes(), StandardCharsets.UTF_8);
                    LOGGER.error("Response body: {}", contents);
                }
                return false;
            }
            return true;
        });
        if (!success) {
            throw new RuntimeException("Error uploading version");
        }
    }

    private synchronized void addAuthorizationHeader(final HttpClient client, final HttpMessage message) throws IOException {
        if (this.activeJWT != null && !this.activeJWT.hasExpired()) {
            // Add the active JWT
            message.addHeader("Authorization", this.activeJWT.jwt());
            return;
        }

        // Request a new JWT
        final ActiveJWT jwt = client.execute(new HttpPost("%s/authenticate?apiKey=%s".formatted(HANGAR_API_URL, this.apiKey)), response -> {
            if (response.getCode() == 400) {
                LOGGER.error("Bad JWT request; is the API key correct?");
                return null;
            } else if (response.getCode() != 200) {
                LOGGER.error("Error requesting JWT {}: {}", response.getCode(), response.getReasonPhrase());
                return null;
            }

            final String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            final JsonObject object = GSON.fromJson(json, JsonObject.class);
            final String token = object.getAsJsonPrimitive("token").getAsString();
            final long expiresIn = object.getAsJsonPrimitive("expiresIn").getAsLong();
            return new ActiveJWT(token, System.currentTimeMillis() + expiresIn);
        });

        if (jwt == null) {
            throw new RuntimeException("Error getting JWT");
        }

        this.activeJWT = jwt;
        message.addHeader("Authorization", jwt.jwt());
    }

    /**
     * Represents an active JSON Web Token used for authentication with Hangar.
     *
     * @param jwt       Active JWT
     * @param expiresAt time in milliseconds when the JWT expires
     */
    private record ActiveJWT(String jwt, long expiresAt) {

        public boolean hasExpired() {
            // Make sure we request a new one before it expires
            return System.currentTimeMillis() < this.expiresAt + TimeUnit.SECONDS.toMillis(3);
        }
    }
}

record VersionUpload(String version, Map<Platform, List<PluginDependency>> pluginDependencies,
                     Map<Platform, List<String>> platformDependencies, String description,
                     List<MultipartFileOrUrl> files, String channel) {
}

record PluginDependency(String name, boolean required, @Nullable String externalUrl) {

    /**
     * Creates a new PluginDependency with the given name, whether the dependency is required, and the namespace of the dependency.
     *
     * @param hangarProjectName name of the dependency, being its Hangar project id
     * @param required          whether the dependency is required
     * @return a new PluginDependency
     */
    static PluginDependency createWithHangarNamespace(final String hangarProjectName, final boolean required) {
        return new PluginDependency(hangarProjectName, required, null);
    }

    /**
     * Creates a new PluginDependency with the given name, external url, and whether the dependency is required.
     *
     * @param name        name of the dependency
     * @param required    whether the dependency is required
     * @param externalUrl url to the dependency
     * @return a new PluginDependency
     */
    static PluginDependency createWithUrl(final String name, final String externalUrl, final boolean required) {
        return new PluginDependency(name, required, externalUrl);
    }

}

enum Platform {
    PAPER,
    WATERFALL,
    VELOCITY
}

/**
 * Represents a file that is either uploaded or downloaded from an external url.
 *
 * @param platforms   platforms the download is compatible with
 * @param externalUrl external url of the download, or null if the download is a file
 */
record MultipartFileOrUrl(List<Platform> platforms, @Nullable String externalUrl) {
}