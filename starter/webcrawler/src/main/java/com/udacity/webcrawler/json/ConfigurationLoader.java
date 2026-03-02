package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    Objects.requireNonNull(reader, "Reader cannot be null");

    ObjectMapper mapper = new ObjectMapper();

    // Prevent automatic closing of the reader
    mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

    try {
      // Deserialize JSON into the Builder type, then build the config
      CrawlerConfiguration.Builder builder =
          mapper.readValue(reader, CrawlerConfiguration.Builder.class);

      return builder.build();

    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Invalid crawler configuration JSON", e);
    }
  }
}

