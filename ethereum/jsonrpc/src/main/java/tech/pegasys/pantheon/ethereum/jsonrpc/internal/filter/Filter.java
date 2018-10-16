package tech.pegasys.pantheon.ethereum.jsonrpc.internal.filter;

import java.time.Duration;
import java.time.Instant;

import com.google.common.annotations.VisibleForTesting;

abstract class Filter {

  private static final Duration DEFAULT_EXPIRE_DURATION = Duration.ofMinutes(10);

  private final String id;
  private Instant expireDate;

  Filter(final String id) {
    this.id = id;
    resetExpireDate();
  }

  String getId() {
    return id;
  }

  void resetExpireDate() {
    this.expireDate = Instant.now().plus(DEFAULT_EXPIRE_DURATION);
  }

  boolean isExpired() {
    return Instant.now().isAfter(expireDate);
  }

  @VisibleForTesting
  void setExpireDate(final Instant expireDate) {
    this.expireDate = expireDate;
  }

  @VisibleForTesting
  Instant getExpireDate() {
    return expireDate;
  }
}
