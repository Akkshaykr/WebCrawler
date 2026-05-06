package com.udacity.webcrawler.profiler;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.Objects;


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
    if (method.getName().equals("equals") && method.getParameterCount() == 1
            && method.getParameterTypes()[0] == Object.class) {
      return delegate.equals(args[0]);
    }
    boolean isProfiled = method.isAnnotationPresent(Profiled.class);
    Instant start = isProfiled ? clock.instant() : null;
    try {
      return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } finally {
      if (isProfiled) {
        state.record(delegate.getClass(), method, Duration.between(start, clock.instant()));
      }
    }
  }
}
