package tech.pegasys.pantheon.tests.acceptance.dsl.waitcondition;

import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;

@FunctionalInterface
public interface WaitCondition {
  void verify(Node node);
}
