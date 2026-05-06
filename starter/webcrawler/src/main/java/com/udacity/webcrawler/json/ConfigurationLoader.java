package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


public final class ConfigurationLoader {

  private final Path path;


  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  public CrawlerConfiguration load() throws IOException {
    try (Reader reader = Files.newBufferedReader(path)) {
      return read(reader);
    }
  }

  public static CrawlerConfiguration read(Reader reader) {
    Objects.requireNonNull(reader);
    try {
      ObjectMapper mapper = new ObjectMapper();
      // Prevent Jackson from closing the Reader
      mapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
      return mapper.readValue(reader, CrawlerConfiguration.class);
    } catch (IOException e) {
      // Wrap checked exception into unchecked so test doesn't need to catch it
      throw new RuntimeException(e);
    }
  }
}