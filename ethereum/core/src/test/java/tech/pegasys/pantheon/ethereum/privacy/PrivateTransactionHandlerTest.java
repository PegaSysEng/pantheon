package tech.pegasys.pantheon.ethereum.privacy;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.math.BigInteger;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.PrivacyParameters;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.mainnet.ValidationResult;
import tech.pegasys.pantheon.util.bytes.BytesValue;

@RunWith(MockitoJUnitRunner.class)
public class PrivateTransactionHandlerTest {

  @Mock PrivateTransactionHandler privateTransactionHandler;
  @Mock
  PrivacyParameters privacyParameters;

  private static final PrivateTransaction VALID_PRIVATE_TRANSACTION =
      new PrivateTransaction(
          0L,
          Wei.of(1),
          21000L,
          Optional.of(
              Address.wrap(BytesValue.fromHexString("0x095e7baea6a6c7c4c2dfeb977efac326af552d87"))),
          Wei.of(
              new BigInteger(
                  "115792089237316195423570985008687907853269984665640564039457584007913129639935")),
          SECP256K1.Signature.create(
              new BigInteger(
                  "32886959230931919120748662916110619501838190146643992583529828535682419954515"),
              new BigInteger(
                  "14473701025599600909210599917245952381483216609124029382871721729679842002948"),
              Byte.valueOf("0")),
          BytesValue.fromHexString("0x"),
          Address.wrap(BytesValue.fromHexString("0x8411b12666f68ef74cace3615c9d5a377729d03f")),
          0,
          BytesValue.wrap("A1aVtMxLCUHmBVHXoZzzBgPbW/wj5axDpW9X8l91SGo=".getBytes(UTF_8)),
          Lists.newArrayList(
              BytesValue.wrap("A1aVtMxLCUHmBVHXoZzzBgPbW/wj5axDpW9X8l91SGo=".getBytes(UTF_8)),
              BytesValue.wrap("Ko2bVqD+nNlNYL5EE7y3IdOnviftjiizpjRt+HTuFBs=".getBytes(UTF_8))),
          BytesValue.wrap("restricted".getBytes(UTF_8)));

  private static final Transaction PUBLIC_TRANSACTION =
      new Transaction(
          0L,
          Wei.of(1),
          21000L,
          Optional.of(
              Address.wrap(BytesValue.fromHexString("0x095e7baea6a6c7c4c2dfeb977efac326af552d87"))),
          Wei.of(
              new BigInteger(
                  "115792089237316195423570985008687907853269984665640564039457584007913129639935")),
          SECP256K1.Signature.create(
              new BigInteger(
                  "32886959230931919120748662916110619501838190146643992583529828535682419954515"),
              new BigInteger(
                  "14473701025599600909210599917245952381483216609124029382871721729679842002948"),
              Byte.valueOf("0")),
          BytesValue.fromHexString("0x"),
          Address.wrap(BytesValue.fromHexString("0x8411b12666f68ef74cace3615c9d5a377729d03f")),
          0);

  @Before
  public void setUp() {
    privateTransactionHandler = new PrivateTransactionHandler(privacyParameters);
  }


  @Test
  public void validTransactionThroughHandler() {
    when(privateTransactionHandler.handle(VALID_PRIVATE_TRANSACTION)).thenReturn(PUBLIC_TRANSACTION);

    final Transaction transactionRespose = privateTransactionHandler.handle(VALID_PRIVATE_TRANSACTION);

    assertThat(transactionRespose).isEqualToComparingFieldByField(PUBLIC_TRANSACTION);
    verify(privateTransactionHandler).handle(any(PrivateTransaction.class));
  }

  @Test
  public void invalidTransactionThroughHandler() {
  }

  @Test
  public void requestMissingParametersToSendTransactionToOrion() {
  }

  @Test
  public void validResponseFromOrion() {
  }

}