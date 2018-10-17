package tech.pegasys.pantheon.tests.acceptance.dsl.condition;

import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;

public interface Condition {

  void verify(Node node);
}
