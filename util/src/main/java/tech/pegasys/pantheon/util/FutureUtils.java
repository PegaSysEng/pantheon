package tech.pegasys.pantheon.util;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class FutureUtils {
  public static <T> CompletableFuture<T> completedExceptionally(final Throwable error) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    future.completeExceptionally(error);
    return future;
  }

  public static <T> CompletableFuture<T> exceptionallyCompose(
      final CompletableFuture<T> future,
      final Function<Throwable, CompletableFuture<T>> errorHandler) {
    final CompletableFuture<T> result = new CompletableFuture<>();
    future.whenComplete(
        (value, error) -> {
          final CompletableFuture<T> nextStep =
              error != null ? errorHandler.apply(error) : completedFuture(value);
          propagateResult(nextStep, result);
        });
    return result;
  }

  public static <T> void propagateResult(
      final CompletableFuture<T> from, final CompletableFuture<T> to) {
    from.whenComplete(
        (value, error) -> {
          if (error != null) {
            to.completeExceptionally(error);
          } else {
            to.complete(value);
          }
        });
  }
}
