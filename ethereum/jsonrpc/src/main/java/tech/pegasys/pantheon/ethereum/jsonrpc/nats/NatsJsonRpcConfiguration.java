package tech.pegasys.pantheon.ethereum.jsonrpc.nats;

public class NatsJsonRpcConfiguration {
  private static final String DEFAULT_NATS_URL = "nats://127.0.0.1:4222";
  private static final String DEFAULT_REQUEST_TOPIC = "jsonrpc.request";
  private static final String DEFAULT_RESPONSE_TOPIC = "jsonrpc.response";
  private final String natsUrl;
  private final String requestTopic;
  private final String responseTopic;

  public NatsJsonRpcConfiguration(String natsUrl, String requestTopic, String responseTopic) {
    this.natsUrl = natsUrl;
    this.requestTopic = requestTopic;
    this.responseTopic = responseTopic;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getNatsUrl() {
    return natsUrl;
  }

  public String getRequestTopic() {
    return requestTopic;
  }

    public String getResponseTopic() {
        return responseTopic;
    }

    public static class Builder {
    private String natsUrl = DEFAULT_NATS_URL;
    private String requestTopic = DEFAULT_REQUEST_TOPIC;
    private String responseTopic = DEFAULT_RESPONSE_TOPIC;

    public Builder natsUrl(final String natsUrl) {
      this.natsUrl = natsUrl;
      return this;
    }

    public Builder requestTopic(final String requestTopic) {
      this.requestTopic = requestTopic;
      return this;
    }

    public Builder responseTopic(final String responseTopic) {
      this.responseTopic = responseTopic;
      return this;
    }

    public NatsJsonRpcConfiguration build() {
      return new NatsJsonRpcConfiguration(natsUrl, requestTopic, responseTopic);
    }
  }
}
