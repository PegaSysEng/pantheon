package tech.pegasys.pantheon.ethereum.p2p.discovery.internal;

import tech.pegasys.pantheon.ethereum.p2p.discovery.DiscoveryPeer;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.List;
import java.util.TreeMap;

/*
A 'lookup' locates the k closest nodes to a node ID.
 The lookup initiator starts by picking α closest nodes to the target it knows of.
The initiator then sends concurrent FindNode packets to those nodes. α is a system-wide concurrency parameter, such as 3.
In the recursive step, the initiator resends FindNode to nodes it has learned about from previous queries.
Of the k nodes the initiator has heard of closest to the target, it picks α that it has not yet queried and resends FindNode to them.
Nodes that fail to respond quickly are removed from consideration until and unless they do respond.
 If a round of FindNode queries fails to return a node any closer than the closest already seen,
the initiator resends the find node to all of the k closest nodes it has not already queried.
The lookup terminates when the initiator has queried and gotten responses from the k closest nodes it has seen.
 */
class RecursivePeerRefreshState {
    // A 'lookup' locates the k closest nodes to a node ID, i.e. target.
    private BytesValue target;
    List<DiscoveryPeer> kClosestNodes;
    static List<DiscoveryPeer> previouslyQueriedNodes;
    static TreeMap<DiscoveryPeer, Integer> distanceSortedList;
}
