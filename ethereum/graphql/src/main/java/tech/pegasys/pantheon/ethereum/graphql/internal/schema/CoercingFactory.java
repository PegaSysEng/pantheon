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
package tech.pegasys.pantheon.ethereum.graphql.internal.schema;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.math.BigInteger;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

public class CoercingFactory {
  public static Coercing<Bytes32, String> getBytes32() {
    return new Coercing<Bytes32, String>() {
      @Override
      public String serialize(final Object dataFetcherResult) throws CoercingSerializeException {
        return dataFetcherResult.toString();
      }

      @Override
      public Bytes32 parseValue(final Object input) throws CoercingParseValueException {
        if (input instanceof String) {
          return Bytes32.fromHexString((String) input);
        }
        throw new CoercingParseValueException(
            "Unable to parse variable value " + input + " as Bytes32");
      }

      @Override
      public Bytes32 parseLiteral(final Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
          String value = ((StringValue) input).getValue();
          try {
            return Bytes32.fromHexString(value);
          } catch (IllegalArgumentException e) {
            throw new CoercingParseLiteralException(
                "Invalid odd-length hex binary representation 0x0 : '" + input + "'");
          }
        }
        throw new CoercingParseLiteralException("Value is not Bytes32 : '" + input + "'");
      }
    };
  }

  public static Coercing<BytesValue, String> getBytes() {
    return new Coercing<BytesValue, String>() {
      @Override
      public String serialize(final Object dataFetcherResult) throws CoercingSerializeException {
        return dataFetcherResult.toString();
      }

      @Override
      public BytesValue parseValue(final Object input) throws CoercingParseValueException {
        if (input instanceof String) {
          return BytesValue.fromHexString((String) input);
        }
        throw new CoercingParseValueException(
            "Unable to parse variable value " + input + " as Bytes");
      }

      @Override
      public BytesValue parseLiteral(final Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
          String value = ((StringValue) input).getValue();
          return BytesValue.fromHexString(value);
        }
        throw new CoercingParseLiteralException("Value is not Bytes : '" + input + "'");
      }
    };
  }

  public static Coercing<Address, String> getAddress() {
    return new Coercing<Address, String>() {
      @Override
      public String serialize(final Object dataFetcherResult) throws CoercingSerializeException {
        return dataFetcherResult.toString();
      }

      @Override
      public Address parseValue(final Object input) throws CoercingParseValueException {
        if (input instanceof String) {
          return Address.fromHexString((String) input);
        }
        throw new CoercingParseValueException(
            "Unable to parse variable value " + input + " as an Address");
      }

      @Override
      public Address parseLiteral(final Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
          String value = ((StringValue) input).getValue();
          return Address.fromHexString(value);
        }
        throw new CoercingParseLiteralException("Value is not Address : '" + input + "'");
      }
    };
  }

  public static Coercing<BigInteger, String> getBigInt() {
    return new Coercing<BigInteger, String>() {
      @Override
      public String serialize(final Object dataFetcherResult) throws CoercingSerializeException {
        return dataFetcherResult.toString();
      }

      @Override
      public BigInteger parseValue(final Object input) throws CoercingParseValueException {
        if (input instanceof String) {
          return new BigInteger((String) input, 16);
        }
        throw new CoercingParseValueException(
            "Unable to parse variable value " + input + " as BigInt");
      }

      @Override
      public BigInteger parseLiteral(final Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
          String value = ((StringValue) input).getValue();
          return new BigInteger(value, 16);
        }
        throw new CoercingParseLiteralException("Value is not BigInt : '" + input + "'");
      }
    };
  }

  public static Coercing<Long, String> getLong() {
    return new Coercing<Long, String>() {
      @Override
      public String serialize(final Object dataFetcherResult) throws CoercingSerializeException {
        if (dataFetcherResult instanceof String) {
          if (((String) dataFetcherResult).substring(0, 2).equals("0x")) {
            return new BigInteger(((String) dataFetcherResult).substring(2), 16).toString();
          }
          return (String) dataFetcherResult;
        }
        return dataFetcherResult.toString();
      }

      @Override
      public Long parseValue(final Object input) throws CoercingParseValueException {
        if (input instanceof String) {
          return Long.valueOf((String) input);
        }
        throw new CoercingParseValueException(
            "Unable to parse variable value " + input + " as Long");
      }

      @Override
      public Long parseLiteral(final Object input) throws CoercingParseLiteralException {
        if (input instanceof IntValue) {
          BigInteger value = ((IntValue) input).getValue();
          return value.longValue();
        }
        throw new CoercingParseLiteralException("Value is not Long : '" + input + "'");
      }
    };
  }
}
