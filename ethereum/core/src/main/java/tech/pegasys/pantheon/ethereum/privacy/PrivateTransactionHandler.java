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
package tech.pegasys.pantheon.ethereum.privacy;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.orion.Orion;
import tech.pegasys.pantheon.orion.types.SendRequest;
import tech.pegasys.pantheon.orion.types.SendResponse;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrivateTransactionHandler {
  private static final Logger LOG = LogManager.getLogger();
  private final Orion orion;
  private final Address privacyPrecompileAddress;

  public PrivateTransactionHandler(final PrivacyParameters privacyParameters) {

    this.orion = new Orion(privacyParameters.getUrl());
    this.privacyPrecompileAddress =
        Address.privacyPrecompiled(privacyParameters.getPrivacyAddress());
  }

  public Transaction handle(final PrivateTransaction privateTransaction) {
    final SendRequest sendRequest = createSendRequest(privateTransaction);
    SendResponse sendResponse = null;
    try {
      sendResponse = orion.send(sendRequest);
    } catch (IOException e) {
      LOG.error("Error sending Request to Orion ");
    }

    return createPrivacyMarkerTransaction(sendResponse.getKey(), privateTransaction);
  }

  private SendRequest createSendRequest(final PrivateTransaction privateTransaction) {
    final String[] privateFor =
        (String[]) privateTransaction.getPrivateFor().stream().map(BytesValue::toString).toArray();

    return new SendRequest(
        Base64.getEncoder().encodeToString(privateTransaction.getPayload().extractArray()),
        privateTransaction.getPrivateFrom().toString(),
        privateFor);
  }

  private Transaction createPrivacyMarkerTransaction(
      final String transactionEnclaveKey, final PrivateTransaction privateTransaction) {

    return new Transaction(
        privateTransaction.getNonce(),
        privateTransaction.getGasPrice(),
        privateTransaction.getGasLimit(),
        Optional.of(privacyPrecompileAddress),
        privateTransaction.getValue(),
        privateTransaction.getSignature(),
        BytesValue.wrap(transactionEnclaveKey.getBytes(Charset.defaultCharset())),
        privateTransaction.getSender(),
        privateTransaction.getChainId().getAsInt());
  }
}
