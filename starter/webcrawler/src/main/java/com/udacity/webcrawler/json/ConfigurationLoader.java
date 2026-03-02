package com.udacity.webcrawler.json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path.
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() {
    try (Reader reader = Files.newBufferedReader(path)) {
      return read(reader);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load configuration from " + path, e);
    }
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    Objects.requireNonNull(reader);

    Gson gson = new Gson();
    BuilderData data = gson.fromJson(reader, BuilderData.class);

    if (data == null) {
      throw new JsonParseException("Configuration JSON is empty or invalid");
    }

    CrawlerConfiguration.Builder builder = new CrawlerConfiguration.Builder();

    if (data.startPages != null) {
      builder.addStartPages(data.startPages.toArray(new String[0]));
    }

    if (data.ignoredUrls != null) {
      builder.addIgnoredUrls(data.ignoredUrls.toArray(new String[0]));
    }

    if (data.ignoredWords != null) {
      builder.addIgnoredWords(data.ignoredWords.toArray(new String[0]));
    }

    builder.setParallelism(data.parallelism);
    builder.setImplementationOverride(
        data.implementationOverride == null ? "" : data.implementationOverride);
    builder.setMaxDepth(data.maxDepth);
    builder.setTimeoutSeconds(data.timeoutSeconds);
    builder.setPopularWordCount(data.popularWordCount);
    builder.setProfileOutputPath(
        data.profileOutputPath == null ? "" : data.profileOutputPath);
    builder.setResultPath(
        data.resultPath == null ? "" : data.resultPath);

    return builder.build();
  }

  /**
   * Helper class used by Gson to deserialize JSON.
   */
  private static class BuilderData {
    java.util.List<String> startPages;
    java.util.List<String> ignoredUrls;
    java.util.List<String> ignoredWords;
    int parallelism;
    String implementationOverride;
    int maxDepth;
    int timeoutSeconds;
    int popularWordCount;
    String profileOutputPath;
    String resultPath;
  }
}

