package net.consensys.pantheon.consensus.common;

import net.consensys.pantheon.ethereum.core.Address;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Container for pending votes and selecting a vote for new blocks */
public class VoteProposer {

  private final Map<Address, Vote> proposals = new ConcurrentHashMap<>();

  /**
   * Identifies an address that should be voted into the validator pool
   *
   * @param address The address to be voted in
   */
  public void auth(final Address address) {
    proposals.put(address, Vote.authVote(address));
  }

  /**
   * Identifies an address that should be voted out of the validator pool
   *
   * @param address The address to be voted out
   */
  public void drop(final Address address) {
    proposals.put(address, Vote.dropVote(address));
  }

  /**
   * Discards a pending vote for an address if one exists
   *
   * @param address The address that should no longer be voted for
   */
  public void discard(final Address address) {
    proposals.remove(address);
  }

  /** Discards all pending votes */
  public void clear() {
    proposals.clear();
  }

  public Vote get(final Address address) {
    return Optional.ofNullable(proposals.get(address)).orElse(Vote.emptyVote());
  }

  private boolean voteNotYetCast(
      final Address localAddress,
      final Vote vote,
      final Collection<Address> validators,
      final VoteTally tally) {

    // Pre evaluate if we have a vote outstanding to auth or drop the target address
    final boolean votedAuth =
        tally.getOutstandingAddVotesFor(vote.getAddress()).contains(localAddress);
    final boolean votedDrop =
        tally.getOutstandingRemoveVotesFor(vote.getAddress()).contains(localAddress);

    // if they're a validator, we want to see them dropped, and we haven't voted to drop them yet
    if (validators.contains(vote.getAddress()) && !votedDrop && vote.isDrop()) {
      return true;
      // or if we've previously voted to auth them and we want to drop them
    } else if (votedAuth && vote.isDrop()) {
      return true;
      // if they're not currently a validator and we want to see them authed and we haven't voted to
      // auth them yet
    } else if (!validators.contains(vote.getAddress()) && !votedAuth && vote.isAuth()) {
      return true;
      // or if we've previously voted to drop them and we want to see them authed
    } else if (votedDrop && vote.isAuth()) {
      return true;
    }

    return false;
  }

  /**
   * Gets a valid vote from our list of pending votes
   *
   * @param localAddress The address of this validator node
   * @param tally the vote tally at the height of the chain we need a vote for
   * @return Either an address with the vote (auth or drop) or no vote if we have no valid pending
   *     votes
   */
  public Vote getVote(final Address localAddress, final VoteTally tally) {
    final Collection<Address> validators = tally.getCurrentValidators();

    // search through the proposals for the first valid vote we haven't cast yet
    for (final Vote vote : proposals.values()) {
      if (voteNotYetCast(localAddress, vote, validators, tally)) {
        // soon as we find one jump out
        return vote;
      }
    }

    // if we didn't find a valid uncast vote, return no vote
    return Vote.emptyVote();
  }
}
