package tech.pegasys.pantheon.cli.converter.exception;

import static java.lang.String.format;

public final class RpcApisConversionException extends Exception {

  public RpcApisConversionException(final String name) {
    super(format("Invalid value: %s", name));
  }
}
