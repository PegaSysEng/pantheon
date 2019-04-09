/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.eth;

import tech.pegasys.pantheon.util.number.PositiveNumber;

public class EthereumWireProtocolConfiguration {

  public static final PositiveNumber DEFAULT_MAX_GET_BLOCK_HEADERS = PositiveNumber.fromInt(192);
  public static final PositiveNumber DEFAULT_MAX_GET_BLOCK_BODIES = PositiveNumber.fromInt(128);
  public static final PositiveNumber DEFAULT_MAX_GET_RECEIPTS = PositiveNumber.fromInt(256);
  public static final PositiveNumber DEFAULT_MAX_GET_NODE_DATA = PositiveNumber.fromInt(384);

  private final int maxGetBlockHeaders;
  private final int maxGetBlockBodies;
  private final int maxGetReceipts;
  private final int maxGetNodeData;

  public EthereumWireProtocolConfiguration(
      final int maxGetBlockHeaders,
      final int maxGetBlockBodies,
      final int maxGetReceipts,
      final int maxGetNodeData) {
    this.maxGetBlockHeaders = maxGetBlockHeaders;
    this.maxGetBlockBodies = maxGetBlockBodies;
    this.maxGetReceipts = maxGetReceipts;
    this.maxGetNodeData = maxGetNodeData;
  }

  public static Builder builder() {
    return new Builder();
  }

  public int getMaxGetBlockHeaders() {
    return maxGetBlockHeaders;
  }

  public int getMaxGetBlockBodies() {
    return maxGetBlockBodies;
  }

  public int getMaxGetReceipts() {
    return maxGetReceipts;
  }

  public int getMaxGetNodeData() {
    return maxGetNodeData;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof EthereumWireProtocolConfiguration)) {
      return false;
    }
    EthereumWireProtocolConfiguration other = ((EthereumWireProtocolConfiguration) obj);
    return maxGetBlockHeaders == other.maxGetBlockHeaders
        && maxGetBlockBodies == other.maxGetBlockBodies
        && maxGetReceipts == other.maxGetReceipts
        && maxGetNodeData == other.maxGetNodeData;
  }

  public static class Builder {
    private int maxGetBlockHeaders = DEFAULT_MAX_GET_BLOCK_HEADERS.getValue();
    private int maxGetBlockBodies = DEFAULT_MAX_GET_BLOCK_BODIES.getValue();
    private int maxGetReceipts = DEFAULT_MAX_GET_RECEIPTS.getValue();
    private int maxGetNodeData = DEFAULT_MAX_GET_NODE_DATA.getValue();

    public Builder maxGetBlockHeaders(final int maxGetBlockHeaders) {
      this.maxGetBlockHeaders = maxGetBlockHeaders;
      return this;
    }

    public Builder maxGetBlockBodies(final int maxGetBlockBodies) {
      this.maxGetBlockBodies = maxGetBlockBodies;
      return this;
    }

    public Builder maxGetReceipts(final int maxGetReceipts) {
      this.maxGetReceipts = maxGetReceipts;
      return this;
    }

    public Builder maxGetNodeData(final int maxGetNodeData) {
      this.maxGetNodeData = maxGetNodeData;
      return this;
    }

    public EthereumWireProtocolConfiguration build() {
      return new EthereumWireProtocolConfiguration(
          maxGetBlockHeaders, maxGetBlockBodies, maxGetReceipts, maxGetNodeData);
    }
  }
}
