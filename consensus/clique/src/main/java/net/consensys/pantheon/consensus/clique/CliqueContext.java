package net.consensys.pantheon.consensus.clique;

import net.consensys.pantheon.consensus.common.EpochManager;
import net.consensys.pantheon.consensus.common.VoteProposer;

/**
 * Holds the data which lives "in parallel" with the importation of blocks etc. when using the
 * Clique consensus mechanism.
 */
public class CliqueContext {

  private final VoteTallyCache voteTallyCache;
  private final VoteProposer voteProposer;
  private final EpochManager epochManager;

  public CliqueContext(
      final VoteTallyCache voteTallyCache,
      final VoteProposer voteProposer,
      final EpochManager epochManager) {
    this.voteTallyCache = voteTallyCache;
    this.voteProposer = voteProposer;
    this.epochManager = epochManager;
  }

  public VoteTallyCache getVoteTallyCache() {
    return voteTallyCache;
  }

  public VoteProposer getVoteProposer() {
    return voteProposer;
  }

  public EpochManager getEpochManager() {
    return epochManager;
  }
}
