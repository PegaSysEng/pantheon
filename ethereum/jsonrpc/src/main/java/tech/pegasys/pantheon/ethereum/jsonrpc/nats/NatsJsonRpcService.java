package tech.pegasys.pantheon.ethereum.jsonrpc.nats;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequestId;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NatsJsonRpcService {

  private static final Logger LOG = LogManager.getLogger();

  private final NatsJsonRpcConfiguration config;
  private final Map<String, JsonRpcMethod> jsonRpcMethods;

  private Connection nc;

  public NatsJsonRpcService(
      NatsJsonRpcConfiguration config, Map<String, JsonRpcMethod> jsonRpcMethods) {
    this.config = config;
    this.jsonRpcMethods = jsonRpcMethods;
  }

  public void start() throws IOException, InterruptedException {
    LOG.info("Starting NATS JsonRpc service.");
    nc = Nats.connect(new Options.Builder().server(config.getNatsUrl()).maxReconnects(-1).build());
    Dispatcher dispatcher =
        nc.createDispatcher(
            (msg) -> {
              String message = new String(msg.getData(), StandardCharsets.UTF_8);
              LOG.debug("Received message: {}", message);
              this.handleMessage(message);
            });
    dispatcher.subscribe(config.getRequestTopic());
  }

  private void handleMessage(final String message) {
    JsonRpcResponse jsonRpcResponse = processCall(message);
    String response = Json.encodePrettily(jsonRpcResponse);
    LOG.debug("jsonrpc response: {}", response);
    nc.publish(config.getResponseTopic(), response.getBytes(StandardCharsets.UTF_8));
  }

  private JsonRpcResponse processCall(final String jsonBody) {
    JsonRpcRequestId id = null;
    try {
      LOG.debug("Parsing json body");
      JsonObject requestJson = new JsonObject(jsonBody);
      id = new JsonRpcRequestId(requestJson.getValue("id"));
      JsonRpcRequest request = requestJson.mapTo(JsonRpcRequest.class);
      // Find method handler
      LOG.debug("Looking for method handler");
      final JsonRpcMethod method = jsonRpcMethods.get(request.getMethod());
      if (method == null) {
        LOG.error("Method not found");
        return new JsonRpcErrorResponse(id, JsonRpcError.METHOD_NOT_FOUND);
      }
      return method.response(request);
    } catch (final IllegalArgumentException exception) {
      return new JsonRpcErrorResponse(id, JsonRpcError.INVALID_REQUEST);
    }
  }
}
