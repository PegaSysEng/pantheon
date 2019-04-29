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
package tech.pegasys.pantheon.ethereum.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.vertx.core.json.JsonObject;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GraphQLHttpBySpecTest extends AbstractGraphQLHttpServiceTest {

  private final String specFileName;

  public GraphQLHttpBySpecTest(final String specFileName) {
    this.specFileName = specFileName;
  }

  /*
   Mapping between GraphQl method class and its spec files

   Formatter will be turned off to make this easier to read (one spec per line)
   @formatter:off
  */
  @Parameters(name = "{index}: {0}")
  public static Collection<String> specs() {
    final List<String> specs = new ArrayList<>();

        specs.add("eth_blockNumber");
        specs.add("eth_call_block_8");
        specs.add("eth_call_callParamsMissing_block_8");
        specs.add("eth_call_earliestBlock");
        specs.add("eth_call_gasLimitTooLow_block_8");
        specs.add("eth_call_gasPriceTooHigh_block_8");
    specs.add("eth_call_latestBlock");
    specs.add("eth_call_toMissing_block_8");
    specs.add("eth_call_valueTooHigh_block_8");
    specs.add("eth_estimateGas_contractDeploy");
    specs.add("eth_estimateGas_insufficientGas");
    specs.add("eth_estimateGas_noParams");
    specs.add("eth_estimateGas_transfer");
    specs.add("eth_getBalance_illegalRangeGreaterThan");
    specs.add("eth_getBalance_illegalRangeLessThan");
    specs.add("eth_getBalance_invalidParams");
    specs.add("eth_getBalance_latest");

    return specs;
  }
  // @formatter:on

  @Test
  public void graphQlCallWithSpecFile() throws Exception {
    graphQlCall(specFileName);
  }

  private void graphQlCall(final String name) throws IOException {
    final String testSpecFile = name + ".json";
    final String json =
        Resources.toString(
            GraphQLHttpBySpecTest.class.getResource(testSpecFile), Charsets.UTF_8);
    final JsonObject spec = new JsonObject(json);

    final String rawRequestBody = spec.getJsonObject("request").toString();

    final RequestBody requestBody = RequestBody.create(JSON, rawRequestBody);
    final Request request =
        new Request.Builder().post(requestBody).url(baseUrl + "/graphql").build();

    importBlocks(1, BLOCKS.size());
    try (final Response resp = client.newCall(request).execute()) {
      final int expectedStatusCode = spec.getInteger("statusCode");
      assertThat(resp.code()).isEqualTo(expectedStatusCode);

      final JsonObject expectedRespBody = spec.getJsonObject("response");
      assertThat(new JsonObject(resp.body().string())).isEqualTo(expectedRespBody);
    }
  }

  private void importBlocks(final int from, final int to) {
    for (int i = from; i < to; ++i) {
      importBlock(i);
    }
  }
}
