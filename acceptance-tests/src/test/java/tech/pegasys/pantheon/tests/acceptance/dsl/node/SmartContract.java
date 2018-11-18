package tech.pegasys.pantheon.tests.acceptance.dsl.node;

import java.lang.reflect.Method;
import java.math.BigInteger;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.tx.Contract;

public class SmartContract {

  private static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(1000);
  private static final BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(3000000);
  private static final Credentials GENESIS_ACCOUNT_ONE_PRIVATE_KEY =
      Credentials.create("0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63");
  private static final Object METHOD_IS_STATIC = null;

  private final Web3j web3j;

  public SmartContract(final Web3j web3j) {
    this.web3j = web3j;
  }

  public <T extends Contract> T deploy(Class<T> clazz) throws Exception {

    final Method method = clazz
        .getMethod("deploy", Web3j.class, Credentials.class, BigInteger.class, BigInteger.class);

    final Object invoked = method
        .invoke(METHOD_IS_STATIC, web3j, GENESIS_ACCOUNT_ONE_PRIVATE_KEY, DEFAULT_GAS_PRICE,
            DEFAULT_GAS_LIMIT);

    return cast(invoked, clazz).send();
  }

  private <T extends Contract> RemoteCall<T> cast(final Object invokedMethod, Class<T> clazz) {
    return (RemoteCall<T>) invokedMethod;
  }
}
