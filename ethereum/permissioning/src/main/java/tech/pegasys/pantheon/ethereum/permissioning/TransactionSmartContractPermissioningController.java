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
package tech.pegasys.pantheon.ethereum.permissioning;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.InetAddress;
import java.util.Optional;
import tech.pegasys.pantheon.crypto.Hash;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.permissioning.node.NodePermissioningProvider;
import tech.pegasys.pantheon.ethereum.transaction.CallParameter;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulatorResult;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.bytes.BytesValues;
import tech.pegasys.pantheon.util.enode.EnodeURL;

/**
 * Controller that can read from a smart contract that exposes the permissioning call
 * transactionAllowed(address,address,uint256,uint256,uint256,bytes)
 */
public class TransactionSmartContractPermissioningController {
  private final Address contractAddress;
  private final TransactionSimulator transactionSimulator;

  // full function signature for connection allowed call
  private static final String FUNCTION_SIGNATURE =
      "transactionAllowed(address,address,uint256,uint256,uint256,bytes)";
  // hashed function signature for connection allowed call
  private static final BytesValue FUNCTION_SIGNATURE_HASH = hashSignature(FUNCTION_SIGNATURE);

  // The first 4 bytes of the hash of the full textual signature of the function is used in
  // contract calls to determine the function being called
  private static BytesValue hashSignature(final String signature) {
    return Hash.keccak256(BytesValue.of(signature.getBytes(UTF_8))).slice(0, 4);
  }

  // True from a contract is 1 filled to 32 bytes
  private static final BytesValue TRUE_RESPONSE =
      BytesValue.fromHexString(
          "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
  private static final BytesValue FALSE_RESPONSE =
      BytesValue.fromHexString(
          "0x0000000000000000000000000000000000000000000000000000000000000000");

  /**
   * Creates a permissioning controller attached to a blockchain
   *
   * @param contractAddress The address at which the permissioning smart contract resides
   * @param transactionSimulator A transaction simulator with attached blockchain and world state
   */
  public TransactionSmartContractPermissioningController(
      final Address contractAddress, final TransactionSimulator transactionSimulator) {
    this.contractAddress = contractAddress;
    this.transactionSimulator = transactionSimulator;
  }

  /**
   * Check whether a given transaction should be permitted for the current head
   *
   * @param transaction The transaction to be examined
   * @return boolean of whether or not to permit the connection to occur
   */
  public boolean isPermitted(final Transaction transaction) {
    final BytesValue payload = createPayload(transaction);
    final CallParameter callParams =
        new CallParameter(null, contractAddress, -1, null, null, payload);

    final Optional<Boolean> contractExists =
        transactionSimulator.doesAddressExistAtHead(contractAddress);

    if (contractExists.isPresent() && !contractExists.get()) {
      throw new IllegalStateException("Permissioning contract does not exist");
    }

    final Optional<TransactionSimulatorResult> result =
        transactionSimulator.processAtHead(callParams);

    if (result.isPresent()) {
      switch (result.get().getResult().getStatus()) {
        case INVALID:
          throw new IllegalStateException("Permissioning transaction found to be Invalid");
        case FAILED:
          throw new IllegalStateException("Permissioning transaction failed when processing");
        default:
          break;
      }
    }

    return result.map(r -> checkTransactionResult(r.getOutput())).orElse(false);
  }

  // Checks the returned bytes from the permissioning contract call to see if it's a value we
  // understand
  public static Boolean checkTransactionResult(final BytesValue result) {
    // booleans are padded to 32 bytes
    if (result.size() != 32) {
      throw new IllegalArgumentException("Unexpected result size");
    }

    // 0 is false
    if (result.compareTo(FALSE_RESPONSE) == 0) {
      return false;
      // 32 bytes of 1's is true
    } else if (result.compareTo(TRUE_RESPONSE) == 0) {
      return true;
      // Anything else is wrong
    } else {
      throw new IllegalStateException("Unexpected result form");
    }
  }

  // Assemble the bytevalue payload to call the contract
  public static BytesValue createPayload(final Transaction transaction) {
    return createPayload(FUNCTION_SIGNATURE_HASH, transaction);
  }

  public static BytesValue createPayload(
      final BytesValue signature, final Transaction transaction) {
    return BytesValues.concatenate(
        signature,
        encodeTransaction(transaction)
    );
  }

  private static BytesValue encodeTransaction(final Transaction transaction) {
    return BytesValues.concatenate(
        encodeAddress(transaction.getSender()),
        encodeAddress(transaction.getTo()),
        transaction.getValue().getBytes(),
        transaction.getGasPrice().getBytes(),
        encodeLong(transaction.getGasLimit()),
        transaction.getPayload()
    );
  }

  private static BytesValue encodeAddress(final Optional<Address> address) {
    return encodeAddress(address.orElse(Address.wrap(BytesValue.wrap(new byte[20]))));
  }

  private static BytesValue encodeAddress(final Address address) {
    return BytesValues.concatenate(
        BytesValue.wrap(new byte[12]),
        address
    );
  }

  private static BytesValue encodeLong(final long l) {
    return null;
  }
}
