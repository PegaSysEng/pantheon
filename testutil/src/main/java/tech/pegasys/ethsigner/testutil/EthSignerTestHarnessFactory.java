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
package tech.pegasys.ethsigner.testutil;

import static net.consensys.cava.io.file.Files.copyResource;

import tech.pegasys.ethsigner.core.signing.ConfigurationChainId;
import tech.pegasys.ethsigner.signer.filebased.CredentialTransactionSigner;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;

public class EthSignerTestHarnessFactory {

  private static final Logger LOG = LogManager.getLogger();
  protected static final String HOST = "127.0.0.1";

  public static EthSignerTestHarness create(
      final Path tempDir,
      final String keyPath,
      final Integer pantheonPort,
      final Integer ethsignerPort,
      final long chainId)
      throws IOException, CipherException {

    Path keyFilePath = copyResource(keyPath, tempDir.resolve(keyPath));

    final EthSignerConfig config =
        new EthSignerConfig(
            Level.DEBUG,
            InetAddress.getByName("localhost"),
            pantheonPort,
            Duration.ofSeconds(10),
            InetAddress.getLocalHost(),
            ethsignerPort,
            new ConfigurationChainId(chainId),
            tempDir);

    final tech.pegasys.ethsigner.core.EthSigner ethSigner =
        new tech.pegasys.ethsigner.core.EthSigner(
            config,
            new CredentialTransactionSigner(
                WalletUtils.loadCredentials("", keyFilePath.toAbsolutePath().toFile())));
    ethSigner.run();

    // FIXME: Figure out how to start ethsigner synchronously.
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    LOG.info("EthSigner port: {}", config.getHttpListenPort());

    return new EthSignerTestHarness(ethSigner, config);
  }
}
