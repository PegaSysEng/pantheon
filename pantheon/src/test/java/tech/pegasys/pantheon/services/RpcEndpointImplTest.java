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
package tech.pegasys.pantheon.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class RpcEndpointImplTest {

  private RPCEndpointServiceImpl serviceImpl;

  @Before
  public void setUp() {
    serviceImpl = new RPCEndpointServiceImpl();
  }

  @Test
  public void testAddsRPC() {
    serviceImpl.registerRPCEndpoint("unit", "test", Object::toString);

    assertThat(serviceImpl.getRpcMethods().size()).isEqualTo(1);
    assertThat(serviceImpl.getRpcMethods()).containsOnlyKeys("unit_test");
    assertThat(serviceImpl.getRpcMethods().get("unit_test").apply(List.of("item")))
        .isEqualTo("[item]");
  }

  @Test
  public void testBadNamespace() {
    assertThatThrownBy(
            () -> serviceImpl.registerRPCEndpoint("this won't work", "test", Object::toString))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void testBadFunctionName() {
    assertThatThrownBy(
            () -> serviceImpl.registerRPCEndpoint("unit", "this won't work", Object::toString))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void testNullMethod() {
    assertThatThrownBy(() -> serviceImpl.registerRPCEndpoint("unit", "test", null))
        .isInstanceOf(NullPointerException.class);
  }
}
