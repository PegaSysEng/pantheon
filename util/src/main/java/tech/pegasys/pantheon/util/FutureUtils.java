/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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
