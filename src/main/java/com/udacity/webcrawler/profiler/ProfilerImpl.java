package com.udacity.webcrawler.profiler;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    // Check if klass has any @Profiled methods
    // If not, just return delegate as-is
    boolean hasProfiled = Arrays.stream(klass.getMethods())
            .anyMatch(m -> m.isAnnotationPresent(Profiled.class));
    if (!hasProfiled) {
      throw new IllegalArgumentException(klass.getName() + " has no @Profiled methods");
    }

    // Create and return a dynamic proxy
    return (T) Proxy.newProxyInstance(
            klass.getClassLoader(),
            new Class[]{klass},
            new ProfilingMethodInterceptor(clock, delegate, state));
  }
  @Override
  public void writeData(Path path) throws IOException {
    Objects.requireNonNull(path);
    try (Writer writer = Files.newBufferedWriter(path,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND)) {
      writeData(writer);
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
