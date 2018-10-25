package tech.pegasys.pantheon.ethereum.p2p.discovery.internal;

import org.junit.Test;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


        System.out.println(peer00.toString());
        System.out.println(peer00.getPeerTable());
        System.out.println(distance(target, peer01.id));
        System.out.println(distance(target, peer02.id));
        System.out.println(distance(target, peer03.id));
        System.out.println("/n");
        System.out.println(peer01.toString());
        System.out.println(peer01.getPeerTable());
        System.out.println(distance(target, peer11.id));
        System.out.println(distance(target, peer12.id));
        System.out.println(distance(target, peer13.id));
        System.out.println("/n");
        System.out.println(peer02.toString());
        System.out.println(peer02.getPeerTable());
        System.out.println(distance(target, peer21.id));
        System.out.println(distance(target, peer22.id));
        System.out.println(distance(target, peer23.id));
        System.out.println("/n");
        System.out.println(peer03.toString());
        System.out.println(peer03.getPeerTable());
        System.out.println(distance(target, peer31.id));
        System.out.println(distance(target, peer32.id));
        System.out.println(distance(target, peer33.id));
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
}
