package net.consensys.pantheon.consensus.common;

import net.consensys.pantheon.ethereum.core.Address;

import java.util.Optional;

import com.google.common.base.Objects;

public class Vote {
  private final Optional<Address> address;
  private final Optional<VoteType> vote;

  private Vote(final Optional<Address> address, final Optional<VoteType> vote) {
    this.address = address;
    this.vote = vote;
  }

  public static Vote authVote(final Address address) {
    return new Vote(Optional.of(address), Optional.of(VoteType.ADD));
  }

  public static Vote dropVote(final Address address) {
    return new Vote(Optional.of(address), Optional.of(VoteType.DROP));
  }

  public static Vote emptyVote() {
    return new Vote(Optional.empty(), Optional.empty());
  }

  public Address getAddress() {
    return address.orElse(Address.fromHexString("0"));
  }

  public boolean isAuth() {
    return vote.equals(Optional.of(VoteType.ADD));
  }

  public boolean isDrop() {
    return vote.equals(Optional.of(VoteType.DROP));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Vote vote1 = (Vote) o;
    return Objects.equal(address, vote1.address) && Objects.equal(vote, vote1.vote);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(address, vote);
  }

  public Address getCoinbase() {
    return getAddress();
  }

  public long getNonce() {
    return vote.map(VoteType::getNonceValue).orElse(0L);
  }
}
