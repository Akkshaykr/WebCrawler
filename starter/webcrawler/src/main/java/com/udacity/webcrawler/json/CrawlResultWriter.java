package com.udacity.webcrawler.json;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;



import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;


public final class CrawlResultWriter {
  private final CrawlResult result;


  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }


  public void write(Path path) throws IOException {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(path);

    try (Writer fileWriter = Files.newBufferedWriter(path,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND)) {
      write(fileWriter);
    }
  }


  public void write(Writer writer) throws IOException {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(writer);

    ObjectMapper mapper  = new ObjectMapper();
    mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    mapper.writeValue(writer,result);
  }
}
