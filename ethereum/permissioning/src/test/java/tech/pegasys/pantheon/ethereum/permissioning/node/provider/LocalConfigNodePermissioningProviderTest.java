package tech.pegasys.pantheon.ethereum.permissioning.node.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import tech.pegasys.pantheon.ethereum.permissioning.NodeLocalConfigPermissioningController;
import tech.pegasys.pantheon.util.enode.EnodeURL;

@RunWith(MockitoJUnitRunner.class)
public class LocalConfigNodePermissioningProviderTest {
  private static final EnodeURL enode1 =
      new EnodeURL(
          "enode://94c15d1b9e2fe7ce56e458b9a3b672ef11894ddedd0c6f247e0f1d3487f52b66208fb4aeb8179fce6e3a749ea93ed147c37976d67af557508d199d9594c35f09@192.168.0.2:1234");
  private static final EnodeURL enode2 =
      new EnodeURL(
          "enode://6f8a80d14311c39f35f516fa664deaaaa13e85b2f7493f37f6144d86991ec012937307647bd3b9a82abe2974e1407241d54947bbb39763a4cac9f77166ad92a0@192.168.0.3:5678");

  @Mock
  private NodeLocalConfigPermissioningController controller;
  private LocalConfigNodePermissioningProvider provider;

  @Test
  public void whenSourceAndDestinationArePermittedShouldReturnTrue() {
    when(controller.isPermitted(enode1)).thenReturn(true);
    when(controller.isPermitted(enode2)).thenReturn(true);

    provider = new LocalConfigNodePermissioningProvider(controller);
    assertThat(provider.isPermitted(enode1, enode2)).isTrue();

    verify(controller).isPermitted(enode1);
    verify(controller).isPermitted(enode2);
  }

  @Test
  public void whenDestinationIsNotPermittedShouldReturnFalse() {
    when(controller.isPermitted(enode1)).thenReturn(true);
    when(controller.isPermitted(enode2)).thenReturn(false);

    provider = new LocalConfigNodePermissioningProvider(controller);
    assertThat(provider.isPermitted(enode1, enode2)).isFalse();

    verify(controller).isPermitted(enode1);
    verify(controller).isPermitted(enode2);
  }

  @Test
  public void whenSourceIsNotPermittedShouldReturnFalse() {
    when(controller.isPermitted(enode1)).thenReturn(false);

    provider = new LocalConfigNodePermissioningProvider(controller);
    assertThat(provider.isPermitted(enode1, enode2)).isFalse();

    verify(controller).isPermitted(enode1);
  }
}
