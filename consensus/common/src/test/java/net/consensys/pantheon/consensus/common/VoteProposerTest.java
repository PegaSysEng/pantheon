package net.consensys.pantheon.consensus.common;

import static net.consensys.pantheon.consensus.common.VoteType.ADD;
import static net.consensys.pantheon.consensus.common.VoteType.DROP;
import static org.assertj.core.api.Assertions.assertThat;

import net.consensys.pantheon.ethereum.core.Address;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class VoteProposerTest {
  private final Address localAddress = Address.fromHexString("0");

  @Test
  public void emptyProposerReturnsNoVotes() {
    final VoteProposer proposer = new VoteProposer();

    assertThat(proposer.getVote(localAddress, new VoteTally(Collections.emptyList())))
        .isEqualTo(Vote.emptyVote());
    assertThat(
            proposer.getVote(
                localAddress,
                new VoteTally(
                    Arrays.asList(
                        Address.fromHexString("0"),
                        Address.fromHexString("1"),
                        Address.fromHexString("2")))))
        .isEqualTo(Vote.emptyVote());
  }

  @Test
  public void demoteVotes() {
    final VoteProposer proposer = new VoteProposer();
    final Address a1 = Address.fromHexString("1");
    final Address a2 = Address.fromHexString("2");
    final Address a3 = Address.fromHexString("3");

    proposer.drop(a1);

    assertThat(proposer.getVote(localAddress, new VoteTally(Collections.emptyList())))
        .isEqualTo(Vote.emptyVote());
    assertThat(proposer.getVote(localAddress, new VoteTally(Collections.singletonList(a1))))
        .isEqualTo(Vote.dropVote(a1));
    assertThat(proposer.getVote(localAddress, new VoteTally(Arrays.asList(a1, a2, a3))))
        .isEqualTo(Vote.dropVote(a1));
    assertThat(proposer.getVote(localAddress, new VoteTally(Arrays.asList(a2, a3))))
        .isEqualTo(Vote.emptyVote());
  }

  @Test
  public void promoteVotes() {
    final VoteProposer proposer = new VoteProposer();
    final Address a1 = Address.fromHexString("1");
    final Address a2 = Address.fromHexString("2");
    final Address a3 = Address.fromHexString("3");

    proposer.auth(a1);

    assertThat(proposer.getVote(localAddress, new VoteTally(Collections.emptyList())))
        .isEqualTo(Vote.authVote(a1));
    assertThat(proposer.getVote(localAddress, new VoteTally(Collections.singletonList(a1))))
        .isEqualTo(Vote.emptyVote());
    assertThat(proposer.getVote(localAddress, new VoteTally(Arrays.asList(a1, a2, a3))))
        .isEqualTo(Vote.emptyVote());
    assertThat(proposer.getVote(localAddress, new VoteTally(Arrays.asList(a2, a3))))
        .isEqualTo(Vote.authVote(a1));
  }

  @Test
  public void discardVotes() {
    final VoteProposer proposer = new VoteProposer();
    final Address a1 = Address.fromHexString("1");
    final Address a2 = Address.fromHexString("2");
    final Address a3 = Address.fromHexString("3");

    proposer.auth(a1);
    proposer.auth(a2);
    proposer.discard(a2);

    assertThat(proposer.getVote(localAddress, new VoteTally(Collections.emptyList())))
        .isEqualTo(Vote.authVote(a1));
    assertThat(proposer.getVote(localAddress, new VoteTally(Collections.singletonList(a1))))
        .isEqualTo(Vote.emptyVote());
    assertThat(proposer.getVote(localAddress, new VoteTally(Arrays.asList(a1, a2, a3))))
        .isEqualTo(Vote.emptyVote());
    assertThat(proposer.getVote(localAddress, new VoteTally(Arrays.asList(a2, a3))))
        .isEqualTo(Vote.authVote(a1));
  }

  @Test
  public void getVoteCyclesAllOptions() {
    final VoteProposer proposer = new VoteProposer();
    final Address a1 = Address.fromHexString("1");
    final Address a2 = Address.fromHexString("2");
    final Address a3 = Address.fromHexString("3");

    proposer.auth(a1);
    proposer.auth(a2);
    proposer.auth(a3);

    final List<Vote> possibleVotes =
        new ArrayList<>(Arrays.asList(Vote.authVote(a1), Vote.authVote(a2), Vote.authVote(a3)));
    final VoteTally voteTally = new VoteTally(Collections.emptyList());
    final Vote v1 = proposer.getVote(localAddress, voteTally);

    assertThat(possibleVotes).contains(v1);

    possibleVotes.remove(v1);
    voteTally.addVote(localAddress, v1.getAddress(), ADD);
    final Vote v2 = proposer.getVote(localAddress, voteTally);

    assertThat(possibleVotes).contains(v2);

    possibleVotes.remove(v2);
    voteTally.addVote(localAddress, v2.getAddress(), ADD);
    final Vote v3 = proposer.getVote(localAddress, voteTally);

    assertThat(possibleVotes).contains(v3);

    possibleVotes.remove(v3);
    voteTally.addVote(localAddress, v3.getAddress(), ADD);
    final Vote v4 = proposer.getVote(localAddress, voteTally);

    assertThat(v4).isEqualTo(Vote.emptyVote());
  }

  @Test
  public void getVoteSkipsInvalidVotes() {
    final VoteProposer proposer = new VoteProposer();
    final Address a1 = Address.fromHexString("1");
    final Address a2 = Address.fromHexString("2");
    final Address a3 = Address.fromHexString("3");
    final Address a4 = Address.fromHexString("4");

    proposer.auth(a1);
    proposer.auth(a2);
    proposer.auth(a3);
    proposer.drop(a4);

    final List<Vote> possibleVotes =
        new ArrayList<>(
            Arrays.asList(
                Vote.authVote(a1), Vote.authVote(a2), Vote.authVote(a3), Vote.dropVote(a4)));
    final VoteTally voteTally = new VoteTally(Collections.emptyList());
    final Vote v1 = proposer.getVote(localAddress, voteTally);

    assertThat(possibleVotes).contains(v1);
    assertThat(v1).isNotEqualTo(Vote.dropVote(a4));

    possibleVotes.remove(v1);
    voteTally.addVote(localAddress, v1.getAddress(), ADD);
    final Vote v2 = proposer.getVote(localAddress, voteTally);

    assertThat(possibleVotes).contains(v2);
    assertThat(v2).isNotEqualTo(Vote.dropVote(a4));

    possibleVotes.remove(v2);
    voteTally.addVote(localAddress, v2.getAddress(), ADD);
    final Vote v3 = proposer.getVote(localAddress, voteTally);

    assertThat(possibleVotes).contains(v3);
    assertThat(v3).isNotEqualTo(Vote.dropVote(a4));

    possibleVotes.remove(v3);
    voteTally.addVote(localAddress, v3.getAddress(), ADD);
    final Vote v4 = proposer.getVote(localAddress, voteTally);

    assertThat(v4).isEqualTo(Vote.emptyVote());
  }

  @Test
  public void revokesAuthVotesInTally() {
    final VoteProposer proposer = new VoteProposer();
    final Address a1 = Address.fromHexString("1");
    final Address a2 = Address.fromHexString("2");
    final Address a3 = Address.fromHexString("3");

    proposer.drop(a1);

    final VoteTally tally = new VoteTally(Arrays.asList(a2, a3));
    tally.addVote(localAddress, a1, ADD);

    assertThat(proposer.getVote(localAddress, tally)).isEqualTo(Vote.dropVote(a1));
  }

  @Test
  public void revokesDropVotesInTally() {
    final VoteProposer proposer = new VoteProposer();
    final Address a1 = Address.fromHexString("1");
    final Address a2 = Address.fromHexString("2");
    final Address a3 = Address.fromHexString("3");

    proposer.auth(a1);

    final VoteTally tally = new VoteTally(Arrays.asList(a2, a3));
    tally.addVote(localAddress, a1, DROP);

    assertThat(proposer.getVote(localAddress, tally)).isEqualTo(Vote.authVote(a1));
  }

  @Test
  public void revokesAuthVotesInTallyWhenValidatorIsInValidatorList() {
    final VoteProposer proposer = new VoteProposer();
    final Address a1 = Address.fromHexString("1");
    final Address a2 = Address.fromHexString("2");
    final Address a3 = Address.fromHexString("3");

    final VoteTally tally = new VoteTally(Arrays.asList(a1, a2, a3));
    tally.addVote(localAddress, a1, ADD);

    proposer.drop(a1);

    assertThat(proposer.getVote(localAddress, tally)).isEqualTo(Vote.dropVote(a1));
  }

  @Test
  public void revokesDropVotesInTallyWhenValidatorIsInValidatorList() {
    final VoteProposer proposer = new VoteProposer();
    final Address a1 = Address.fromHexString("1");
    final Address a2 = Address.fromHexString("2");
    final Address a3 = Address.fromHexString("3");

    final VoteTally tally = new VoteTally(Arrays.asList(a1, a2, a3));
    tally.addVote(localAddress, a1, DROP);

    proposer.auth(a1);

    assertThat(proposer.getVote(localAddress, tally)).isEqualTo(Vote.authVote(a1));
  }
}
