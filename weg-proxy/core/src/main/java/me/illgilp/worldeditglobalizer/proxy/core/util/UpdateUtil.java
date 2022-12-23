package me.illgilp.worldeditglobalizer.proxy.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class UpdateUtil {

    private static final String GITHUB_RELEASE_URL = "https://api.github.com/repos/illgilp/worldeditglobalizer/releases";

    public static Optional<GithubRelease> getNewerRelease(String currentTag) {
        Optional<List<GithubRelease>> optionalGithubReleases = fetchReleases(Logger.getLogger("a"));
        if (optionalGithubReleases.isPresent()) {
            List<GithubRelease> releases = optionalGithubReleases.get().stream()
                .sorted(Comparator.comparing(o -> o.publishedAt))
                .collect(Collectors.toList());
            int index = 0;

            for (int i = 0; i < releases.size(); i++) {
                if (releases.get(i).tagName.equals(currentTag)) {
                    index = i;
                    break;
                }
            }

            return releases.stream()
                .skip(index + 1)
                .sorted((o1, o2) -> o2.publishedAt.compareTo(o1.publishedAt))
                .filter(githubRelease -> !githubRelease.preRelease)
                .findFirst();
        }
        return Optional.empty();
    }

    private static Optional<List<GithubRelease>> fetchReleases(Logger logger) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(GITHUB_RELEASE_URL).openConnection();
            connection.setRequestMethod("GET");

            Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class,
                    (JsonDeserializer<Instant>) (json, typeOfT, context) -> Instant.parse(json.getAsString()))
                .create();
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                return Optional.ofNullable(gson.fromJson(reader, new TypeToken<List<GithubRelease>>() {
                }.getType()));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to fetch releases from github", e);
        }
        return Optional.empty();
    }


    @Getter
    @NoArgsConstructor
    @ToString
    public static final class GithubRelease {

        @SerializedName("tag_name")
        private String tagName;

        private String name;

        @SerializedName("prerelease")
        private boolean preRelease;

        @SerializedName("published_at")
        private Instant publishedAt;

    }

}
