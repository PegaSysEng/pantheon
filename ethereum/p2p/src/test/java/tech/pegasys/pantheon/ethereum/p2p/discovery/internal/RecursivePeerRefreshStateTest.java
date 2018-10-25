package tech.pegasys.pantheon.ethereum.p2p.discovery.internal;

import org.junit.Test;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A 'lookup' locates the k closest nodes to a node ID.
 * The lookup initiator starts by picking α closest nodes to the target it knows of.
 * The initiator then sends concurrent FindNode packets to those nodes. α is a system-wide concurrency parameter, such as 3.
 * In the recursive step, the initiator resends FindNode to nodes it has learned about from previous queries.
 * Of the k nodes the initiator has heard of closest to the target, it picks α that it has not yet queried and resends FindNode to them.
 * Nodes that fail to respond quickly are removed from consideration until and unless they do respond.
 * If a round of FindNode queries fails to return a node any closer than the closest already seen,
 * the initiator resends the find node to all of the k closest nodes it has not already queried.
 * The lookup terminates when the initiator has queried and gotten responses from the k closest nodes it has seen.
 **/
public class RecursivePeerRefreshStateTest {

    @Test
    public void shouldOperateTestPeer() {
        BytesValue target = BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");

        TestPeer peer00 = new TestPeer(BytesValue.fromHexString("0x11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"));
        /* * */
        TestPeer peer01 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001"));
        peer00.addToPeerTable(peer01);
        /* * */
        TestPeer peer11 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000"));
        TestPeer peer12 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000110000"));
        TestPeer peer13 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001110000"));
        peer01.addToPeerTable(peer11);
        peer01.addToPeerTable(peer12);
        peer01.addToPeerTable(peer13);
        /* * */
        TestPeer peer02 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011"));
        peer00.addToPeerTable(peer02);
        /* * */
        TestPeer peer21 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000"));
        TestPeer peer22 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001100000000"));
        TestPeer peer23 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011100000000"));
        peer02.addToPeerTable(peer21);
        peer02.addToPeerTable(peer22);
        peer02.addToPeerTable(peer23);
        /* * */
        TestPeer peer03 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111"));
        peer00.addToPeerTable(peer03);
        /* * */
        TestPeer peer31 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000"));
        TestPeer peer32 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011000000000000"));
        TestPeer peer33 = new TestPeer(BytesValue.fromHexString("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111000000000000"));
        peer03.addToPeerTable(peer31);
        peer03.addToPeerTable(peer32);
        peer03.addToPeerTable(peer33);

        /* * */
        /* * */
        /* * */

        // TODO: Implement a bespoke container for these, or some kind of comparator?
        HashMap<TestPeer, Integer> initialPeerMap = new HashMap<>();

        for(TestPeer peer : peer00.getPeerTable()){
            initialPeerMap.put(peer, distance(target, peer.id));
        }
        for(TestPeer peer : peer01.getPeerTable()){
            initialPeerMap.put(peer, distance(target, peer.id));
        }
        for(TestPeer peer : peer02.getPeerTable()){
            initialPeerMap.put(peer, distance(target, peer.id));
        }
        for(TestPeer peer : peer03.getPeerTable()){
            initialPeerMap.put(peer, distance(target, peer.id));
        }

        LinkedHashMap<TestPeer, Integer> distanceSortedPeerMap = sortHashMap(initialPeerMap);

        for(Map.Entry entry : distanceSortedPeerMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public LinkedHashMap<TestPeer, Integer> sortHashMap(HashMap<TestPeer, Integer> unsortedMap)  {
        LinkedHashMap<TestPeer, Integer> result = new LinkedHashMap<>();

        // Add TestPeer keys to an array: the array would get sorted, based on those keys' values
        ArrayList<TestPeer> sortedKeys = new ArrayList<>(unsortedMap.keySet());

        // Sort the ArrayList<TestPeer> of keys
        for (int i=0; i<unsortedMap.size(); i++)  {
            for (int j=1; j<sortedKeys.size(); j++)  {
                if (unsortedMap.get(sortedKeys.get(j)) > unsortedMap.get(sortedKeys.get(j-1))) {
                    TestPeer temp = sortedKeys.get(j);
                    sortedKeys.set(j, sortedKeys.get(j-1));
                    sortedKeys.set(j-1, temp);

                }
            }
        }

        // Construct the result Map
        for (TestPeer key: sortedKeys)  {
            result.put(key, unsortedMap.get(key));
        }

        return result;
    }



    class TestPeer {
        BytesValue id;
        List<TestPeer> peerTable = new ArrayList<>();

        TestPeer(BytesValue id) {
            this.id = id;
        }

        void addToPeerTable(TestPeer testPeer) {
            this.peerTable.add(testPeer);
        }

        List<TestPeer> getPeerTable() {
            return this.peerTable;
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    static int distance(final BytesValue v1, final BytesValue v2) {
        assert (v1.size() == v2.size());
        final byte[] v1b = v1.extractArray();
        final byte[] v2b = v2.extractArray();

        if (Arrays.equals(v1b, v2b)) {
            return 0;
        }

        int distance = v1b.length * 8;
        for (int i = 0; i < v1b.length; i++) {
            final byte xor = (byte) (0xff & (v1b[i] ^ v2b[i]));
            if (xor == 0) {
                distance -= 8;
            } else {
                int p = 7;
                while (((xor >> p--) & 0x01) == 0) {
                    distance--;
                }
                break;
            }
        }
        return distance;
    }
}
