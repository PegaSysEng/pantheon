package tech.pegasys.pantheon.ethereum.p2p.netty;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import tech.pegasys.pantheon.ethereum.p2p.api.PeerConnection;
import tech.pegasys.pantheon.ethereum.p2p.rlpx.framing.Framer;
import tech.pegasys.pantheon.ethereum.p2p.rlpx.framing.FramingException;
import tech.pegasys.pantheon.ethereum.p2p.wire.PeerInfo;
import tech.pegasys.pantheon.ethereum.p2p.wire.messages.DisconnectMessage.DisconnectReason;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import org.junit.Test;

public class DeFramerTest {

  private final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
  private final Framer framer = mock(Framer.class);
  private final Callbacks callbacks = mock(Callbacks.class);
  private final PeerConnection peerConnection = mock(PeerConnection.class);
  private final CompletableFuture<PeerConnection> connectFuture = new CompletableFuture<>();
  private final DeFramer deFramer = new DeFramer(
      framer,
      Collections.emptyList(),
      new PeerInfo(5, "abc", Collections.emptyList(), 0, BytesValue.fromHexString("0x01")),
      callbacks,
      connectFuture);

  @Test
  public void shouldDisconnectForBreachOfProtocolWhenFramingExceptionThrown() throws Exception {
    connectFuture.complete(peerConnection);

    deFramer.exceptionCaught(ctx, new DecoderException(new FramingException("Test")));

    verify(peerConnection).disconnect(DisconnectReason.BREACH_OF_PROTOCOL);
  }
}