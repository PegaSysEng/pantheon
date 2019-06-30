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
package tech.pegasys.pantheon.ethereum.mainnet.precompiles;

import tech.pegasys.pantheon.ethereum.core.Gas;
import tech.pegasys.pantheon.ethereum.mainnet.AbstractPrecompiledContract;
import tech.pegasys.pantheon.ethereum.vm.GasCalculator;
import tech.pegasys.pantheon.ethereum.vm.MessageFrame;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Arrays;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.bouncycastle.util.Pack;

public class BLAKE2fPrecompileContract extends AbstractPrecompiledContract {
  public BLAKE2fPrecompileContract(final GasCalculator gasCalculator) {
    super("BLAKE2b", gasCalculator);
  }

  @Override
  public Gas gasRequirement(final BytesValue input) {
    return null;
  }

  @Override
  public BytesValue compute(final BytesValue input, final MessageFrame messageFrame) {

    byte[] in = input.extractArray();

    if (input.size() != 213) {
      throw new IllegalArgumentException(
          "input length for Blake2 F precompile should be exactly 213 bytes");
    }
    long[] h = new long[8];

    h[0] = Longs.fromByteArray(Arrays.copyOfRange(in, 0, 8));
    h[1] = Longs.fromByteArray(Arrays.copyOfRange(in, 8, 16));
    h[2] = Longs.fromByteArray(Arrays.copyOfRange(in, 16, 24));
    h[3] = Longs.fromByteArray(Arrays.copyOfRange(in, 24, 32));
    h[4] = Longs.fromByteArray(Arrays.copyOfRange(in, 32, 40));
    h[5] = Longs.fromByteArray(Arrays.copyOfRange(in, 40, 48));
    h[6] = Longs.fromByteArray(Arrays.copyOfRange(in, 48, 56));
    h[7] = Longs.fromByteArray(Arrays.copyOfRange(in, 56, 64));

    byte[] m = Arrays.copyOfRange(in, 64, 192);

    long[] t = new long[2];
    t[0] = Longs.fromByteArray(Arrays.copyOfRange(in, 192, 200));
    t[1] = Longs.fromByteArray(Arrays.copyOfRange(in, 200, 208));

    boolean f = in[208] != 0x00000000;

    int rounds = Ints.fromByteArray(Arrays.copyOfRange(in, 209, 213));

    byte[] output = blake2f(h, m, t, f, rounds);

    return BytesValue.wrap(output);
  }

  private int blockSize = 128;

  private static final long[] iv = {
    0x6a09e667f3bcc908L, 0xbb67ae8584caa73bL, 0x3c6ef372fe94f82bL,
    0xa54ff53a5f1d36f1L, 0x510e527fade682d1L, 0x9b05688c2b3e6c1fL,
    0x1f83d9abfb41bd6bL, 0x5be0cd19137e2179L
  };

  private static final byte[][] precomputed = {
    {0, 2, 4, 6, 1, 3, 5, 7, 8, 10, 12, 14, 9, 11, 13, 15},
    {14, 4, 9, 13, 10, 8, 15, 6, 1, 0, 11, 5, 12, 2, 7, 3},
    {11, 12, 5, 15, 8, 0, 2, 13, 10, 3, 7, 9, 14, 6, 1, 4},
    {7, 3, 13, 11, 9, 1, 12, 14, 2, 5, 4, 15, 6, 10, 0, 8},
    {9, 5, 2, 10, 0, 7, 4, 15, 14, 11, 6, 3, 1, 12, 8, 13},
    {2, 6, 0, 8, 12, 10, 11, 3, 4, 7, 15, 1, 13, 5, 14, 9},
    {12, 1, 14, 4, 5, 15, 13, 10, 0, 6, 9, 8, 7, 3, 2, 11},
    {13, 7, 12, 3, 11, 14, 1, 9, 5, 15, 8, 2, 0, 4, 6, 10},
    {6, 14, 11, 0, 15, 9, 3, 8, 12, 13, 1, 10, 2, 7, 4, 5},
    {10, 8, 7, 1, 2, 4, 6, 5, 15, 9, 3, 13, 11, 14, 12, 0}
  };

  private byte[] blake2f(
      final long[] h, final byte[] mb, final long[] t, final boolean f, final int rounds) {

    long[] m = new long[16];

    long t0 = t[0];
    long t1 = t[1];

    for (int i = 0; i < mb.length; ++i) {
      t0 += blockSize;
      if (t0 < blockSize) {
        t1++;
      }

      long v0 = h[0];
      long v1 = h[1];
      long v2 = h[2];
      long v3 = h[3];
      long v4 = h[4];
      long v5 = h[5];
      long v6 = h[6];
      long v7 = h[7];
      long v8 = iv[0];
      long v9 = iv[1];
      long v10 = iv[2];
      long v11 = iv[3];
      long v12 = iv[4];
      long v13 = iv[5];
      long v14 = iv[6];
      long v15 = iv[7];
      v12 ^= t0;
      v13 ^= t1;

      if (f) {
        v14 ^= 0xffffffffffffffffL;
      }

      for (int j = 0; j < m.length; ++j) {
        m[j] = Pack.littleEndianToLong(Arrays.copyOfRange(mb, i, i + 8), 0);
        i += 8;
      }

      for (int j = 0; j < rounds; ++j) {
        byte[] s = precomputed[j % 10];

        v0 += m[s[0]];
        v0 += v4;
        v12 ^= v0;
        v12 = Long.rotateLeft(v12, -32);
        v8 += v12;
        v4 ^= v8;
        v4 = Long.rotateLeft(v4, -24);
        v1 += m[s[1]];
        v1 += v5;
        v13 ^= v1;
        v13 = Long.rotateLeft(v13, -32);
        v9 += v13;
        v5 ^= v9;
        v5 = Long.rotateLeft(v5, -24);
        v2 += m[s[2]];
        v2 += v6;
        v14 ^= v2;
        v14 = Long.rotateLeft(v14, -32);
        v10 += v14;
        v6 ^= v10;
        v6 = Long.rotateLeft(v6, -24);
        v3 += m[s[3]];
        v3 += v7;
        v15 ^= v3;
        v15 = Long.rotateLeft(v15, -32);
        v11 += v15;
        v7 ^= v11;
        v7 = Long.rotateLeft(v7, -24);

        v0 += m[s[4]];
        v0 += v4;
        v12 ^= v0;
        v12 = Long.rotateLeft(v12, -16);
        v8 += v12;
        v4 ^= v8;
        v4 = Long.rotateLeft(v4, -63);
        v1 += m[s[5]];
        v1 += v5;
        v13 ^= v1;
        v13 = Long.rotateLeft(v13, -16);
        v9 += v13;
        v5 ^= v9;
        v5 = Long.rotateLeft(v5, -63);
        v2 += m[s[6]];
        v2 += v6;
        v14 ^= v2;
        v14 = Long.rotateLeft(v14, -16);
        v10 += v14;
        v6 ^= v10;
        v6 = Long.rotateLeft(v6, -63);
        v3 += m[s[7]];
        v3 += v7;
        v15 ^= v3;
        v15 = Long.rotateLeft(v15, -16);
        v11 += v15;
        v7 ^= v11;
        v7 = Long.rotateLeft(v7, -63);

        v0 += m[s[8]];
        v0 += v5;
        v15 ^= v0;
        v15 = Long.rotateLeft(v15, -32);
        v10 += v15;
        v5 ^= v10;
        v5 = Long.rotateLeft(v5, -24);
        v1 += m[s[9]];
        v1 += v6;
        v12 ^= v1;
        v12 = Long.rotateLeft(v12, -32);
        v11 += v12;
        v6 ^= v11;
        v6 = Long.rotateLeft(v6, -24);
        v2 += m[s[10]];
        v2 += v7;
        v13 ^= v2;
        v13 = Long.rotateLeft(v13, -32);
        v8 += v13;
        v7 ^= v8;
        v7 = Long.rotateLeft(v7, -24);
        v3 += m[s[11]];
        v3 += v4;
        v14 ^= v3;
        v14 = Long.rotateLeft(v14, -32);
        v9 += v14;
        v4 ^= v9;
        v4 = Long.rotateLeft(v4, -24);

        v0 += m[s[12]];
        v0 += v5;
        v15 ^= v0;
        v15 = Long.rotateLeft(v15, -16);
        v10 += v15;
        v5 ^= v10;
        v5 = Long.rotateLeft(v5, -63);
        v1 += m[s[13]];
        v1 += v6;
        v12 ^= v1;
        v12 = Long.rotateLeft(v12, -16);
        v11 += v12;
        v6 ^= v11;
        v6 = Long.rotateLeft(v6, -63);
        v2 += m[s[14]];
        v2 += v7;
        v13 ^= v2;
        v13 = Long.rotateLeft(v13, -16);
        v8 += v13;
        v7 ^= v8;
        v7 = Long.rotateLeft(v7, -63);
        v3 += m[s[15]];
        v3 += v4;
        v14 ^= v3;
        v14 = Long.rotateLeft(v14, -16);
        v9 += v14;
        v4 ^= v9;
        v4 = Long.rotateLeft(v4, -63);
      }

      h[0] ^= v0 ^ v8;
      h[1] ^= v1 ^ v9;
      h[2] ^= v2 ^ v10;
      h[3] ^= v3 ^ v11;
      h[4] ^= v4 ^ v12;
      h[5] ^= v5 ^ v13;
      h[6] ^= v6 ^ v14;
      h[7] ^= v7 ^ v15;
    }

    byte[] output = new byte[64];

    System.arraycopy(Longs.toByteArray(h[0]), 0, output, 0, 8);
    System.arraycopy(Longs.toByteArray(h[1]), 0, output, 8, 8);
    System.arraycopy(Longs.toByteArray(h[2]), 0, output, 16, 8);
    System.arraycopy(Longs.toByteArray(h[3]), 0, output, 24, 8);
    System.arraycopy(Longs.toByteArray(h[4]), 0, output, 32, 8);
    System.arraycopy(Longs.toByteArray(h[5]), 0, output, 40, 8);
    System.arraycopy(Longs.toByteArray(h[6]), 0, output, 48, 8);
    System.arraycopy(Longs.toByteArray(h[7]), 0, output, 56, 8);

    return output;
  }
}
