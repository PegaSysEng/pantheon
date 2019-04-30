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
package tech.pegasys.pantheon.ethereum.graphql.internal.methods;

public enum GraphQLMethodType {
  BLOCK("Query", "block"),
  BLOCKS("Query", "blocks"),
  PENDING("Query", "pending"),
  TRANSACTION("Query", "transaction"),
  LOGS("Query", "logs"),
  GAS_PRICE("Query", "gasPrice"),
  PROTOCOL_VERSION("Query", "protocolVersion"),
  SYNCING("Query", "syncing"),
  PARENT("Block", "parent"),
  OMMERS("Block", "ommers"),
  OMMER_AT("Block", "ommerAt"),
  TRANSACTIONS("Block", "transactions"),
  TRANSACTION_AT("Block", "transactionAt"),
  ACCOUNT("Block", "account"),
  CALL("Block", "call"),
  ESTIMATE_GAS("Block", "estimateGas"),
  MINER("Block", "miner"),
  BLOCK_LOG("Block", "logs"),
  STORAGE("Account", "storage"),
  SEND_RAW_TRANSACTION("Mutation", "sendRawTransaction"),
  TRANSACTION_FROM("Transaction", "from"),
  TRANSACTION_TO("Transaction", "to"),
  TRANSACTION_CREATED_CONTRACT("Transaction", "createdContract"),
  TRANSACTION_BLOCK("Transaction", "block"),
  TRANSACTION_LOGS("Transaction", "logs"),
  LOG_ACCOUNT("Log", "account");

  private final String type;
  private final String field;

  GraphQLMethodType(final String type, final String field) {
    this.type = type;
    this.field = field;
  }

  public String getType() {
    return type;
  }

  public String getField() {
    return field;
  }
}
