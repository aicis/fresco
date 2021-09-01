package dk.alexandra.fresco.framework.util;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

/**
 * Wraps parallel streams in try with resource in order to close them after use.
 */
public final class ParallelStreaming {

  public static <S, T extends BaseStream<S, T>> void call(
      T stream,
      Consumer<T> consumer
  ) {
    try (T parallel = stream.parallel()) {
      consumer.accept(parallel);
    }
  }

  public static <S, T extends Collection<S>> void call(
      T stream,
      Consumer<Stream<S>> consumer
  ) {
    try (Stream<S> parallel = stream.parallelStream()) {
      consumer.accept(parallel);
    }
  }

  public static <S, R, T extends BaseStream<S, T>> R apply(
      T stream,
      Function<T, R> consumer
  ) {
    try (T parallel = stream.parallel()) {
      return consumer.apply(parallel);
    }
  }

  public static <S, R, T extends Collection<S>> R apply(
      T stream,
      Function<Stream<S>, R> consumer
  ) {
    try (Stream<S> parallel = stream.parallelStream()) {
      return consumer.apply(parallel);
    }
  }
}
