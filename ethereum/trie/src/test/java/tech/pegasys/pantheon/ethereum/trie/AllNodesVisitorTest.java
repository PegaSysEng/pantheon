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
package tech.pegasys.pantheon.ethereum.trie;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;

public class AllNodesVisitorTest {

  private final StoredNode storedNode = mock(StoredNode.class);

  @Test
  public void visitExtension() {
    final ExtensionNode extensionNode = mock(ExtensionNode.class);
    when(extensionNode.getChild()).thenReturn(storedNode);
    new AllNodesVisitor(x -> {}).visit(extensionNode);
    verify(storedNode).unload();
  }

  @Test
  public void visitBranch() {
    final BranchNode branchNode = mock(BranchNode.class);
    when(branchNode.getChildren()).thenReturn(Collections.singletonList(storedNode));
    new AllNodesVisitor(x -> {}).visit(branchNode);
    verify(storedNode).unload();
  }
}
