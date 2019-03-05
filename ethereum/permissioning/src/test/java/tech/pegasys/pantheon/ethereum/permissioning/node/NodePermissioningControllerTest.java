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
package tech.pegasys.pantheon.ethereum.permissioning.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.permissioning.node.provider.SyncStatusNodePermissioningProvider;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodePermissioningControllerTest {

  private static final EnodeURL bootnode =
      new EnodeURL(
          "enode://6332792c4a00e3e4ee0926ed89e0d27ef985424d97b6a45bf0f23e51f0dcb5e66b875777506458aea7af6f9e4ffb69f43f3778ee73c81ed9d34c51c4b16b0b0f@192.168.0.1:9999");
  private static final EnodeURL enode1 =
      new EnodeURL(
          "enode://94c15d1b9e2fe7ce56e458b9a3b672ef11894ddedd0c6f247e0f1d3487f52b66208fb4aeb8179fce6e3a749ea93ed147c37976d67af557508d199d9594c35f09@192.168.0.2:1234");
  private static final EnodeURL enode2 =
      new EnodeURL(
          "enode://6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0@192.168.0.3:5678");

  @Mock private SyncStatusNodePermissioningProvider syncStatusNodePermissioningProvider;

  private NodePermissioningController controller;
  private Runnable hasReachedSyncCallback;

  @Before
  public void before() {
    ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
    doNothing()
        .when(syncStatusNodePermissioningProvider)
        .setHasReachedSyncCallback(captor.capture());

    this.controller = new NodePermissioningController(syncStatusNodePermissioningProvider);
    this.hasReachedSyncCallback = captor.getValue();
  }

  @Test
  public void isPermittedShouldDelegateToSyncStatusProvider() {
    controller.isPermitted(enode1, enode2);

    verify(syncStatusNodePermissioningProvider).isPermitted(eq(enode1), eq(enode2));
  }

  @Test
  public void ifSyncHasBeenReachedPeerDiscoveryShouldRun() {
    final Runnable peerDiscoveryCallback = mock(Runnable.class);
    when(syncStatusNodePermissioningProvider.hasReachedSync()).thenReturn(true);

    controller.startPeerDiscoveryCallback(peerDiscoveryCallback);

    verify(peerDiscoveryCallback).run();
  }

  @Test
  public void ifSyncHasNotBeenReachedShouldSetPeerDiscoveryCallback() {
    final Runnable peerDiscoveryCallback = mock(Runnable.class);
    when(syncStatusNodePermissioningProvider.hasReachedSync()).thenReturn(false);

    controller.startPeerDiscoveryCallback(peerDiscoveryCallback);

    verifyZeroInteractions(peerDiscoveryCallback);
    assertThat(controller.getPeerDiscoveryCallback()).isNotEmpty();
  }

  @Test
  public void shouldExecutePeerDiscoveryCallbackWhenHasReachedSyncCallbackIsTriggered() {
    final Runnable peerDiscoveryCallback = mock(Runnable.class);
    when(syncStatusNodePermissioningProvider.hasReachedSync()).thenReturn(false);

    controller.startPeerDiscoveryCallback(peerDiscoveryCallback);
    hasReachedSyncCallback.run();

    verify(peerDiscoveryCallback).run();
    // should set the callback to empty after executing it once
    assertThat(controller.getPeerDiscoveryCallback()).isEmpty();
  }
}
