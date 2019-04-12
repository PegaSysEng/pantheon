package tech.pegasys.pantheon.ethereum.jsonrpc.nats;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NatsJsonRpcService {

  private static final Logger LOG = LogManager.getLogger();

  private final NatsJsonRpcConfiguration config;
  private final Map<String, JsonRpcMethod> jsonRpcMethods;

  public NatsJsonRpcService(
      NatsJsonRpcConfiguration config, Map<String, JsonRpcMethod> jsonRpcMethods) {
    this.config = config;
    this.jsonRpcMethods = jsonRpcMethods;
  }

  public void start() throws IOException, InterruptedException {
    LOG.info("Starting NATS JsonRpc service.");
    final Connection nc =
        Nats.connect(new Options.Builder().server(config.getNatsUrl()).maxReconnects(-1).build());
    Dispatcher dispatcher =
        nc.createDispatcher(
            (msg) -> {
              String message = new String(msg.getData(), StandardCharsets.UTF_8);
              LOG.info("Received message: {}", message);
              this.handleMessage(message);
            });
    dispatcher.subscribe(config.getRequestTopic());
  }

  private void handleMessage(final String message) {
    LOG.info("Handle message: {}", message);
  }
}
