/*
 * Copyright 2018 ConsenSys AG.
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
package tech.pegasys.pantheon.tests.acceptance.dsl.node;

import static java.util.Collections.singletonList;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;

public class Clique {

  private Web3jService web3jService;

  public Clique(final Web3jService web3j) {
    this.web3jService = web3j;
  }

  public static class ProposeResponse extends Response<Boolean> {}

  public static class DiscardResponse extends Response<Boolean> {}

  public static class SignersBlockResponse extends Response<List<Address>> {}

  public ProposeResponse propose(final Address address, final Boolean auth) throws IOException {
    Request<?, ProposeResponse> request =
        new Request<>(
            "clique_propose",
            Arrays.asList(address.toString(), auth.toString()),
            web3jService,
            ProposeResponse.class);
    return request.send();
  }

  public DiscardResponse discard(final Address address) throws IOException {
    Request<?, DiscardResponse> request =
        new Request<>(
            "clique_discard",
            singletonList(address.toString()),
            web3jService,
            DiscardResponse.class);
    return request.send();
  }

  public SignersBlockResponse signersAtBlock(final Optional<String> blockNumber)
      throws IOException {
    Request<?, SignersBlockResponse> request =
        new Request<>(
            "clique_getSigners",
            singletonList(blockNumber.map(Object::toString).orElse("latest")),
            web3jService,
            SignersBlockResponse.class);
    return request.send();
  }

  public SignersBlockResponse signersAtHash(final Hash hash) throws IOException {
    Request<?, SignersBlockResponse> request =
        new Request<>(
            "clique_getSigners",
            singletonList(hash.toString()),
            web3jService,
            SignersBlockResponse.class);
    return request.send();
  }
}
