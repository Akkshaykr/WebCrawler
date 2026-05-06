package com.udacity.webcrawler.profiler;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;

  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = Objects.requireNonNull(delegate);
    this.state = Objects.requireNonNull(state);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // Check if this method has @Profiled annotation
    boolean isProfiled = method.isAnnotationPresent(Profiled.class);

    // Record start time if profiled
    Instant start = isProfiled ? clock.instant() : null;

    try {
      // Actually call the real method on the real object
      return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      // Throw the REAL exception, not a wrapper
      throw e.getTargetException();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } finally {
      // Record how long it took if profiled
      if (isProfiled) {
        state.record(delegate.getClass(), method, Duration.between(start, clock.instant()));
      }
    }
  }
}
