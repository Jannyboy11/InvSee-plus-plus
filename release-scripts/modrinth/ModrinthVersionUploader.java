//| mvnDeps:
//|   - com.google.code.gson:gson:2.13.2
//|   - org.yaml:snakeyaml:2.6
//|   - org.apache.httpcomponents.client5:httpclient5:5.6

import com.google.gson.*;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.*;
import org.yaml.snakeyaml.Yaml;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.*;
import org.apache.hc.client5.http.classic.*;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.*;
import org.apache.hc.client5.http.impl.classic.*;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class ModrinthVersionUploader {

    private static final Logger LOGGER = Logger.getLogger(ModrinthVersionUploader.class.getName());

    public static void main(String[] args) throws Exception {
        try (var client = new ModrinthClient(getAccessToken())) {
            var plugins = getPluginFiles();
            var versionInfo = createVersion(plugins);
            LOGGER.log(Level.INFO, "Uploading version: {0}", versionInfo);
            var response = client.createVersion(versionInfo, plugins);
            LOGGER.log(Level.INFO, "Got response from Modrinth API: {0}", response);
        }
    }

    private static Version createVersion(List<Path> plugins) {
        String pluginVersion = readPluginVersion(plugins.getFirst());
        boolean isSnapshot = pluginVersion.endsWith("-SNAPSHOT");

        String versionName = "InvSee++ " + pluginVersion;
        String versionNumber = pluginVersion;
        String changeLog = "Automated deployment for InvSee++ v" + pluginVersion;
        List<String> dependencies = List.of();
        List<String> gameVersions = List.of(getMinecraftVersions());
        String versionType = isSnapshot ? "alpha" : "release";
        List<String> loaders = List.of("bukkit", "spigot", "paper", "purpur");
        boolean featured = !isSnapshot;
        String status = "listed";
        String projectId = "bYazc7fd"; // obtained from https://api.modrinth.com/v2/project/invsee++;
        List<String> fileParts = plugins.stream()
                .map(path -> getFilePartName(path))
                .toList();
        String primaryFile = getFilePartName(plugins.getFirst());

        return new Version(
                versionName,
                versionNumber,
                changeLog,
                dependencies,
                gameVersions,
                versionType,
                loaders,
                featured,
                status,
                projectId,
                fileParts,
                primaryFile
        );
    }

    private static List<Path> getPluginFiles() {
        return List.of(
                Path.of("plugins/InvSee++.jar"),
                Path.of("plugins/InvSee++_Give.jar"),
                Path.of("plugins/InvSee++_Clear.jar")
        );
    }

    static String getFilePartName(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.length() - 4); // omit ".jar" suffix
        // alternative implementation: read plugin name from 'name' or 'prefix' field from the plugin.yml in the jar file.
    }

    private static String getAccessToken() {
        return System.getenv("MODRINTH_TOKEN");
    }

    private static String[] getMinecraftVersions() {
        return System.getenv("MINECRAFT_VERSIONS").split(" ");
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
}

class ModrinthClient implements AutoCloseable {

    private static final String MODRINTH_BASE_URL = "https://api.modrinth.com/";
    private static final String USER_AGENT = "Jannyboy11/InvSee++/ModrinthVersionUploader";

    private final CloseableHttpClient client;
    private final String token;
    private final Gson gson;

    ModrinthClient(String token) {
        this.token = token;
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        this.client = HttpClients.createDefault();
    }

    // https://docs.modrinth.com/api/operations/createversion/
    VersionResponse createVersion(Version versionInfo, List<Path> files) throws Exception {
        HttpPost post = new HttpPost(MODRINTH_BASE_URL + "v2/version");
        post.addHeader("Authorization", token);
        post.addHeader("User-Agent", USER_AGENT);

        String json = gson.toJson(versionInfo);

        var entityBuilder = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.STRICT)
                .addTextBody("data", json, ContentType.APPLICATION_JSON);
        for (Path file : files) {
            String fileName = file.getFileName().toString();
            String name = ModrinthVersionUploader.getFilePartName(file);
            entityBuilder.addBinaryBody(name, file, ContentType.DEFAULT_BINARY, fileName);
        }
        var entity = entityBuilder.build();

        post.setEntity(entity);

        try (CloseableHttpResponse response = client.execute(post)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int status = response.getCode();
            if (status >= 400) {
                throw new RuntimeException("Modrinth API error (" + status + "): " + responseBody);
            }

            return gson.fromJson(responseBody, VersionResponse.class);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}

record Version(
        String name,
        String versionNumber,
        String changeLog,
        List<String> dependencies,
        List<String> gameVersions,
        String versionType,
        List<String> loaders,
        boolean featured,
        String status,
        String projectId,
        List<String> fileParts,
        String primaryFile
) { }

record VersionResponse(
        String id,
        String name,
        String versionNumber
) { }