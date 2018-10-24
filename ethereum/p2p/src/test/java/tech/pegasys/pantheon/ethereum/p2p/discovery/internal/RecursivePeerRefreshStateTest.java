package tech.pegasys.pantheon.ethereum.p2p.discovery.internal;

import org.junit.Test;

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

    //System.out.println(Arrays.toString(data));
    @Test
    public void shouldOperateTestPeer() {

        TestPeer peer00 = new TestPeer(new byte[]{0x00, 0x00, 0x00, 0x00});
        TestPeer peer01 = new TestPeer(new byte[]{0x01, 0x01, 0x01, 0x01});
        TestPeer peer02 = new TestPeer(new byte[]{0x02, 0x02, 0x02, 0x02});
        TestPeer peer03 = new TestPeer(new byte[]{0x03, 0x03, 0x03, 0x03});
        TestPeer peer04 = new TestPeer(new byte[]{0x04, 0x04, 0x04, 0x04});
        TestPeer peer05 = new TestPeer(new byte[]{0x05, 0x05, 0x05, 0x05});
        TestPeer peer06 = new TestPeer(new byte[]{0x06, 0x06, 0x06, 0x06});
        TestPeer peer07 = new TestPeer(new byte[]{0x07, 0x07, 0x07, 0x07});
        TestPeer peer08 = new TestPeer(new byte[]{0x08, 0x08, 0x08, 0x08});

        System.out.println(peer00.toString());

        peer00.addToPeerTable(peer01);
        peer00.addToPeerTable(peer02);
        peer00.addToPeerTable(peer03);
        peer00.addToPeerTable(peer04);
        peer00.addToPeerTable(peer05);
        peer00.addToPeerTable(peer06);
        peer00.addToPeerTable(peer07);
        peer00.addToPeerTable(peer08);

        System.out.println(peer00.getPeerTable());

        TestPeer peer10 = new TestPeer(new byte[]{0x10, 0x10, 0x10, 0x10});
        TestPeer peer11 = new TestPeer(new byte[]{0x11, 0x11, 0x11, 0x11});
        TestPeer peer12 = new TestPeer(new byte[]{0x12, 0x12, 0x12, 0x12});
        TestPeer peer13 = new TestPeer(new byte[]{0x13, 0x13, 0x13, 0x13});
        TestPeer peer14 = new TestPeer(new byte[]{0x14, 0x14, 0x14, 0x14});
        TestPeer peer15 = new TestPeer(new byte[]{0x15, 0x15, 0x15, 0x15});
        TestPeer peer16 = new TestPeer(new byte[]{0x16, 0x16, 0x16, 0x16});
        TestPeer peer17 = new TestPeer(new byte[]{0x17, 0x17, 0x17, 0x17});
        TestPeer peer18 = new TestPeer(new byte[]{0x18, 0x18, 0x18, 0x18});

        System.out.println(peer10.toString());

        peer10.addToPeerTable(peer11);
        peer10.addToPeerTable(peer12);
        peer10.addToPeerTable(peer13);
        peer10.addToPeerTable(peer14);
        peer10.addToPeerTable(peer15);
        peer10.addToPeerTable(peer16);
        peer10.addToPeerTable(peer17);
        peer10.addToPeerTable(peer18);

        System.out.println(peer10.getPeerTable());
    }


    class TestPeer {
        byte[] id;
        List<TestPeer> peerTable = new ArrayList<>();

        TestPeer(byte[] id) {
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
            return Arrays.toString(id);
        }
    }
}
