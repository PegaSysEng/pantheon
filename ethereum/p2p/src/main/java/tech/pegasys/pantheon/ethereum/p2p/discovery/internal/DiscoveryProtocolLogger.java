package tech.pegasys.pantheon.ethereum.p2p.discovery.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.ethereum.p2p.peers.Peer;

public class DiscoveryProtocolLogger {

  private static final Logger LOG = LogManager.getLogger();

  static void logSendingPacket(Peer peer, Packet packet) {
    LOG.trace(
        "<<< Sending  {} packet from peer {} ({}): {}",
        shortenPacketType(packet),
        peer.getId().slice(0, 16),
        peer.getEndpoint(),
        packet);
  }

  static void logReceivedPacket(Peer peer, Packet packet) {
    LOG.trace(
        ">>> Received {} packet from peer {} ({}): {}",
        shortenPacketType(packet),
        peer.getId().slice(0, 16),
        peer.getEndpoint(),
        packet);
  }

  private static String shortenPacketType(Packet packet) {
    switch (packet.getType()) {
      case PING:
        return "PING ";
      case PONG:
        return "PONG ";
      case FIND_NEIGHBORS:
        return "FINDN";
      case NEIGHBORS:
        return "NEIGH";
    }
    return null;
  }

}
