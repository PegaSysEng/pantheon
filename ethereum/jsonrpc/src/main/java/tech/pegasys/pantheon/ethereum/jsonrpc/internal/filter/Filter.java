package tech.pegasys.pantheon.ethereum.jsonrpc.internal.filter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

abstract class Filter {

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
    this.expireDate = Instant.now().plus(15, ChronoUnit.MINUTES);
  }

  boolean isExpired() {
    return expireDate.isAfter(Instant.now());
  }
}
