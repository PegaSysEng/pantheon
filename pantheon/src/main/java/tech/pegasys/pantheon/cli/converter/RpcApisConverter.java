package tech.pegasys.pantheon.cli.converter;

import picocli.CommandLine;
import tech.pegasys.pantheon.cli.converter.exception.RpcApisConversionException;
import tech.pegasys.pantheon.consensus.clique.jsonrpc.CliqueRpcApis;
import tech.pegasys.pantheon.consensus.ibft.jsonrpc.IbftRpcApis;
import tech.pegasys.pantheon.ethereum.jsonrpc.RpcApi;
import tech.pegasys.pantheon.ethereum.jsonrpc.RpcApis;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class RpcApisConverter implements CommandLine.ITypeConverter<RpcApi> {

  @Override
  public RpcApi convert(final String name) throws RpcApisConversionException {
    final String uppercaseName = name.trim().toUpperCase();

    return Stream.<Function<String, Optional<RpcApi>>>of(
            RpcApis::valueOf, CliqueRpcApis::valueOf, IbftRpcApis::valueOf)
        .map(f -> f.apply(uppercaseName))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElseThrow(() -> new RpcApisConversionException("Invalid value: " + name));
  }
}
