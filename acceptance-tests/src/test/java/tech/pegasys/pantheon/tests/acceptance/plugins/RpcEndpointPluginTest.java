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
package tech.pegasys.pantheon.tests.acceptance.plugins;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.config.JsonUtil;
import tech.pegasys.pantheon.ethereum.jsonrpc.RpcApis;
import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Before;
import org.junit.Test;

public class RpcEndpointPluginTest extends AcceptanceTestBase {

  private PantheonNode node;
  private OkHttpClient client;
  protected static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  @Before
  public void setUp() throws Exception {
    node =
        pantheon.createPluginsNode(
            "node1",
            Collections.singletonList("testPlugins"),
            Collections.emptyList(),
            RpcApis.NET);
    cluster.start(node);
    client = new OkHttpClient();
  }

  @Test
  public void rpcWorking() throws IOException {
    final String firstCall = "FirstCall";
    final String secondCall = "SecondCall";
    final String thirdCall = "ThirdCall";
    final String fourthCall = "FourthCall";

    ObjectNode resultJson = callTestMethod("unitTests_replaceValue", firstCall);
    assertThat(resultJson.get("result").asText()).isEqualTo("InitialValue");

    resultJson = callTestMethod("unitTests_replaceValueArray", secondCall);
    assertThat(resultJson.get("result").get(0).asText()).isEqualTo(firstCall);

    resultJson = callTestMethod("unitTests_replaceValueBean", thirdCall);
    assertThat(resultJson.get("result").get("value").asText()).isEqualTo(secondCall);

    resultJson = callTestMethod("unitTests_replaceValueLength", fourthCall);
    assertThat(resultJson.get("result").asInt()).isEqualTo(thirdCall.length());
  }

  @Test
  public void throwsError() throws IOException {
    ObjectNode resultJson = callTestMethod("unitTests_replaceValue", null);
    assertThat(resultJson.get("result").asText()).isEqualTo("InitialValue");

    resultJson = callTestMethod("unitTests_replaceValueLength", "InitialValue");
    assertThat(resultJson.get("error").get("message").asText()).isEqualTo("Internal error");
  }

  private ObjectNode callTestMethod(final String method, final String value) throws IOException {
    final String resultString =
        client
            .newCall(
                new Request.Builder()
                    .post(
                        RequestBody.create(
                            JSON,
                            "{\"jsonrpc\":\"2.0\",\"method\":\""
                                + method
                                + "\",\"params\":["
                                + (value == null ? value : "\"" + value + "\"")
                                + "],\"id\":33}"))
                    .url(
                        "http://"
                            + node.getHostName()
                            + ":"
                            + node.getJsonRpcSocketPort().get()
                            + "/")
                    .build())
            .execute()
            .body()
            .string();
    System.out.println(resultString);
    return JsonUtil.objectNodeFromString(resultString);
  }
}
