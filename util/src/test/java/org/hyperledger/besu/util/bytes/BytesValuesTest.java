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
package org.hyperledger.besu.util.bytes;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BytesValuesTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  private static BytesValue h(final String hex) {
    return BytesValue.fromHexString(hex);
  }

  private static BigInteger bi(final String decimal) {
    return new BigInteger(decimal);
  }

  @Test
  public void shouldTrimLeadingZeroes() {
    Assert.assertEquals(h("0x"), BytesValues.trimLeadingZeros(h("0x")));
    Assert.assertEquals(h("0x"), BytesValues.trimLeadingZeros(h("0x00")));
    Assert.assertEquals(h("0x"), BytesValues.trimLeadingZeros(h("0x00000000")));

    Assert.assertEquals(h("0x01"), BytesValues.trimLeadingZeros(h("0x01")));
    Assert.assertEquals(h("0x01"), BytesValues.trimLeadingZeros(h("0x00000001")));

    Assert.assertEquals(h("0x3010"), BytesValues.trimLeadingZeros(h("0x3010")));
    Assert.assertEquals(h("0x3010"), BytesValues.trimLeadingZeros(h("0x00003010")));

    Assert.assertEquals(h("0xFFFFFFFF"), BytesValues.trimLeadingZeros(h("0xFFFFFFFF")));
    Assert.assertEquals(h("0xFFFFFFFF"), BytesValues.trimLeadingZeros(h("0x000000000000FFFFFFFF")));
  }

  @Test
  public void minimalBytes() {
    Assert.assertEquals(h("0x"), BytesValues.toMinimalBytes(0));

    Assert.assertEquals(h("0x01"), BytesValues.toMinimalBytes(1));
    Assert.assertEquals(h("0x04"), BytesValues.toMinimalBytes(4));
    Assert.assertEquals(h("0x10"), BytesValues.toMinimalBytes(16));
    Assert.assertEquals(h("0xFF"), BytesValues.toMinimalBytes(255));

    Assert.assertEquals(h("0x0100"), BytesValues.toMinimalBytes(256));
    Assert.assertEquals(h("0x0200"), BytesValues.toMinimalBytes(512));

    Assert.assertEquals(h("0x010000"), BytesValues.toMinimalBytes(1L << 16));
    Assert.assertEquals(h("0x01000000"), BytesValues.toMinimalBytes(1L << 24));
    Assert.assertEquals(h("0x0100000000"), BytesValues.toMinimalBytes(1L << 32));
    Assert.assertEquals(h("0x010000000000"), BytesValues.toMinimalBytes(1L << 40));
    Assert.assertEquals(h("0x01000000000000"), BytesValues.toMinimalBytes(1L << 48));
    Assert.assertEquals(h("0x0100000000000000"), BytesValues.toMinimalBytes(1L << 56));
    Assert.assertEquals(h("0xFFFFFFFFFFFFFFFF"), BytesValues.toMinimalBytes(-1L));
  }

  @Test
  public void hexToInteger() {
    Assert.assertEquals(0, BytesValues.extractInt(h("0x")));
    Assert.assertEquals(0, BytesValues.extractInt(h("0x00")));
    Assert.assertEquals(0, BytesValues.extractInt(h("0x00000000")));

    Assert.assertEquals(1, BytesValues.extractInt(h("0x01")));
    Assert.assertEquals(1, BytesValues.extractInt(h("0x0001")));
    Assert.assertEquals(1, BytesValues.extractInt(h("0x000001")));
    Assert.assertEquals(1, BytesValues.extractInt(h("0x00000001")));

    Assert.assertEquals(256, BytesValues.extractInt(h("0x0100")));
    Assert.assertEquals(256, BytesValues.extractInt(h("0x000100")));
    Assert.assertEquals(256, BytesValues.extractInt(h("0x00000100")));

    Assert.assertEquals(-1, BytesValues.extractInt(h("0xFFFFFFFF")));
  }

  @Test
  public void hexToIntegerInvalid() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Cannot extract an int");
    BytesValues.extractInt(h("0x0100000000"));
  }

  @Test
  public void hexToLong() {
    Assert.assertEquals(0L, BytesValues.extractLong(h("0x")));
    Assert.assertEquals(0L, BytesValues.extractLong(h("0x00")));
    Assert.assertEquals(0L, BytesValues.extractLong(h("0x00000000")));

    Assert.assertEquals(1L, BytesValues.extractLong(h("0x01")));
    Assert.assertEquals(1L, BytesValues.extractLong(h("0x0001")));
    Assert.assertEquals(1L, BytesValues.extractLong(h("0x000001")));
    Assert.assertEquals(1L, BytesValues.extractLong(h("0x00000001")));
    Assert.assertEquals(1L, BytesValues.extractLong(h("0x00000001")));
    Assert.assertEquals(1L, BytesValues.extractLong(h("0x0000000001")));
    Assert.assertEquals(1L, BytesValues.extractLong(h("0x000000000001")));

    Assert.assertEquals(256L, BytesValues.extractLong(h("0x0100")));
    Assert.assertEquals(256L, BytesValues.extractLong(h("0x000100")));
    Assert.assertEquals(256L, BytesValues.extractLong(h("0x00000100")));
    Assert.assertEquals(256L, BytesValues.extractLong(h("0x0000000100")));
    Assert.assertEquals(256L, BytesValues.extractLong(h("0x000000000100")));
    Assert.assertEquals(256L, BytesValues.extractLong(h("0x00000000000100")));
    Assert.assertEquals(256L, BytesValues.extractLong(h("0x0000000000000100")));

    Assert.assertEquals((1L << 32) - 1, BytesValues.extractLong(h("0xFFFFFFFF")));

    Assert.assertEquals(-1, BytesValues.extractLong(h("0xFFFFFFFFFFFFFFFF")));
  }

  @Test
  public void hexToLongInvalid() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Cannot extract a long");
    BytesValues.extractLong(h("0x010000000000000000"));
  }

  @Test
  public void unsignedShort() {
    Assert.assertEquals(h("0x0000"), BytesValues.ofUnsignedShort(0));
    Assert.assertEquals(h("0x0001"), BytesValues.ofUnsignedShort(1));

    Assert.assertEquals(h("0x0100"), BytesValues.ofUnsignedShort(256));
    Assert.assertEquals(h("0xFFFF"), BytesValues.ofUnsignedShort(65535));
  }

  @Test
  public void unsignedShortNegative() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Value -1 cannot be represented as an unsigned short");
    BytesValues.ofUnsignedShort(-1);
  }

  @Test
  public void unsignedShortTooBig() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Value 65536 cannot be represented as an unsigned short");
    BytesValues.ofUnsignedShort(65536);
  }

  @Test
  public void hexConcatenate() {
    Assert.assertEquals(h("0x"), BytesValues.concatenate(h("0x"), h("0x")));

    Assert.assertEquals(h("0x1234"), BytesValues.concatenate(h("0x1234"), h("0x")));
    Assert.assertEquals(h("0x1234"), BytesValues.concatenate(h("0x"), h("0x1234")));

    Assert.assertEquals(h("0x12345678"), BytesValues.concatenate(h("0x1234"), h("0x5678")));

    final int valCount = 10;
    final BytesValue[] values = new BytesValue[valCount];
    final StringBuilder res = new StringBuilder();
    for (int i = 0; i < valCount; i++) {
      final String hex = "1234";
      values[i] = h(hex);
      res.append(hex);
    }
    Assert.assertEquals(h(res.toString()), BytesValues.concatenate(values));
  }

  @Test
  public void unsignedBigInteger() {
    Assert.assertEquals(bi("0"), BytesValues.asUnsignedBigInteger(BytesValue.EMPTY));
    Assert.assertEquals(bi("1"), BytesValues.asUnsignedBigInteger(BytesValue.of(1)));

    // Make sure things are interpreted unsigned.
    Assert.assertEquals(bi("255"), BytesValues.asUnsignedBigInteger(h("0xFF")));

    // Try 2^100 + Long.MAX_VALUE, as an easy to define a big not too special big integer.
    final BigInteger expected =
        BigInteger.valueOf(2).pow(100).add(BigInteger.valueOf(Long.MAX_VALUE));

    // 2^100 is a one followed by 100 zeros, that's 12 bytes of zeros (=96) plus 4 more zeros (so
    // 0x10 == 16).
    final MutableBytesValue v = MutableBytesValue.create(13);
    v.set(0, (byte) 16);
    v.setLong(v.size() - 8, Long.MAX_VALUE);
    Assert.assertEquals(expected, BytesValues.asUnsignedBigInteger(v));
  }

  @Test
  public void signedBigInteger() {
    Assert.assertEquals(bi("0"), BytesValues.asSignedBigInteger(BytesValue.EMPTY));
    Assert.assertEquals(bi("1"), BytesValues.asSignedBigInteger(BytesValue.of(1)));

    // Make sure things are interpreted signed.
    Assert.assertEquals(bi("-1"), BytesValues.asSignedBigInteger(h("0xFF")));

    // Try 2^100 + Long.MAX_VALUE, as an easy to define a big but not too special big integer.
    BigInteger expected = BigInteger.valueOf(2).pow(100).add(BigInteger.valueOf(Long.MAX_VALUE));

    // 2^100 is a one followed by 100 zeros, that's 12 bytes of zeros (=96) plus 4 more zeros (so
    // 0x10 == 16).
    MutableBytesValue v = MutableBytesValue.create(13);
    v.set(0, (byte) 16);
    v.setLong(v.size() - 8, Long.MAX_VALUE);
    Assert.assertEquals(expected, BytesValues.asSignedBigInteger(v));

    // And for a large negative one, we use -(2^100 + Long.MAX_VALUE), which is:
    //  2^100 + Long.MAX_VALUE = 0x10(4 bytes of 0)7F(  7 bytes of 1)
    //                 inverse = 0xEF(4 bytes of 1)80(  7 bytes of 0)
    //                      +1 = 0xEF(4 bytes of 1)80(6 bytes of 0)01
    expected = expected.negate();
    v = MutableBytesValue.create(13);
    v.set(0, (byte) 0xEF);
    for (int i = 1; i < 5; i++) v.set(i, (byte) 0xFF);
    v.set(5, (byte) 0x80);
    // 6 bytes of 0
    v.set(12, (byte) 1);
    Assert.assertEquals(expected, BytesValues.asSignedBigInteger(v));
  }
}
