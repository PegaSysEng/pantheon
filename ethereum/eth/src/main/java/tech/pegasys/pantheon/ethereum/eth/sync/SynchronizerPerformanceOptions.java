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
package tech.pegasys.pantheon.ethereum.eth.sync;

import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import picocli.CommandLine;

/**
 * A collection of flags/config values that are performance related.
 *
 * <p>These are segregated into a separate class so that they are not considered standard options
 * that are used to configure Pantheon. Generally speaking you will want to leave these options
 * alone unless you know that configuring them will improve your performace.
 *
 * <p>Each of these options starts with <code>--X</code>. This is a Java convention communicating
 * that there is no API stability commitment to these options. While these flags may persist on a
 * long term bases it should be noted they may be added or removed in future updates and no
 * announcement needs to be made for their modifications. Use these at your own risk.
 *
 * <p>All of these options are also hidden so that they will not show up in the CLI help screen.
 */
public class SynchronizerPerformanceOptions {

  private static final Supplier<SynchronizerPerformanceOptions> cachedInstance =
      Suppliers.memoize(SynchronizerPerformanceOptions::new);

  public static SynchronizerPerformanceOptions get() {
    return cachedInstance.get();
  }

  @CommandLine.Option(
      names = "--Xsynchronizer-downloaderChangeTargetThresholdByHeight",
      hidden = true,
      defaultValue = "20",
      paramLabel = "<LONG>",
      description =
          "Performance config for downloaderChangeTargetThresholdByHeight (default: ${DEFAULT-VALUE})")
  public long downloaderChangeTargetThresholdByHeight = 20L;

  //  @CommandLine.Option(
  //      names = "--Xsynchronizer-downloaderChangeTargetThresholdByTd",
  //      hidden = true,
  //      defaultValue = "1000000000",
  //      paramLabel = "<UINT256>",
  //      description =
  //          "Performance config for downloaderChangeTargetThresholdByTd (default:
  // ${DEFAULT-VALUE})")
  public UInt256 downloaderChangeTargetThresholdByTd = UInt256.of(1_000_000_000L);

  @CommandLine.Option(
      names = "--Xsynchronizer-downloaderHeaderRequestSize",
      hidden = true,
      defaultValue = "200",
      paramLabel = "<INTEGER>",
      description =
          "Performance config for downloaderHeaderRequestSize (default: ${DEFAULT-VALUE})")
  public int downloaderHeaderRequestSize = 200;

  @CommandLine.Option(
      names = "--Xsynchronizer-downloaderCheckpointTimeoutsPermitted",
      hidden = true,
      defaultValue = "5",
      paramLabel = "<INTEGER>",
      description =
          "Performance config for downloaderCheckpointTimeoutsPermitted (default: ${DEFAULT-VALUE})")
  public int downloaderCheckpointTimeoutsPermitted = 5;

  @CommandLine.Option(
      names = "--Xsynchronizer-downloaderChainSegmentTimeoutsPermitted",
      hidden = true,
      defaultValue = "5",
      paramLabel = "<INTEGER>",
      description =
          "Performance config for downloaderChainSegmentTimeoutsPermitted (default: ${DEFAULT-VALUE})")
  public int downloaderChainSegmentTimeoutsPermitted = 5;

  @CommandLine.Option(
      names = "--Xsynchronizer-downloaderChainSegmentSize",
      hidden = true,
      defaultValue = "200",
      paramLabel = "<INTEGER>",
      description = "Performance config for downloaderChainSegmentSize (default: ${DEFAULT-VALUE})")
  public int downloaderChainSegmentSize = 200;

  @CommandLine.Option(
      names = "--Xsynchronizer-downloaderParallelism",
      hidden = true,
      defaultValue = "4",
      paramLabel = "<INTEGER>",
      description = "Performance config for downloaderParallelism (default: ${DEFAULT-VALUE})")
  public int downloaderParallelism = 4;

  @CommandLine.Option(
      names = "--Xsynchronizer-transactionsParallelism",
      hidden = true,
      defaultValue = "2",
      paramLabel = "<INTEGER>",
      description = "Performance config for transactionsParallelism (default: ${DEFAULT-VALUE})")
  public int transactionsParallelism = 2;

  @CommandLine.Option(
      names = "--Xsynchronizer-computationParallelism",
      hidden = true,
      paramLabel = "<INTEGER>",
      description = "Performance config for computationParallelism (default: # of processors)")
  public int computationParallelism = Runtime.getRuntime().availableProcessors();

  @CommandLine.Option(
      names = "--Xsynchronizer-fastSyncPivotDistance",
      hidden = true,
      defaultValue = "50",
      paramLabel = "<INTEGER>",
      description = "Performance config for fastSyncPivotDistance (default: ${DEFAULT-VALUE})")
  public int fastSyncPivotDistance = 50;

  @CommandLine.Option(
      names = "--Xsynchronizer-fastSyncFullValidationRate",
      hidden = true,
      defaultValue = "0.1",
      paramLabel = "<FLOAT>",
      description = "Performance config for fastSyncFullValidationRate (default: ${DEFAULT-VALUE})")
  public float fastSyncFullValidationRate = .1f;

  @CommandLine.Option(
      names = "--Xsynchronizer-worldStateHashCountPerRequest",
      hidden = true,
      defaultValue = "348",
      paramLabel = "<INTEGER>",
      description =
          "Performance config for worldStateHashCountPerRequest (default: ${DEFAULT-VALUE})")
  public int worldStateHashCountPerRequest = 348;

  @CommandLine.Option(
      names = "--Xsynchronizer-worldStateRequestParallelism",
      hidden = true,
      defaultValue = "10",
      paramLabel = "<INTEGER>",
      description =
          "Performance config for worldStateRequestParallelism (default: ${DEFAULT-VALUE})")
  public int worldStateRequestParallelism = 10;

  @CommandLine.Option(
      names = "--Xsynchronizer-worldStateMaxRequestsWithoutProgress",
      hidden = true,
      defaultValue = "1000",
      paramLabel = "<INTEGER>",
      description =
          "Performance config for worldStateMaxRequestsWithoutProgress (default: ${DEFAULT-VALUE})")
  public int worldStateMaxRequestsWithoutProgress = 1000;

  @CommandLine.Option(
      names = "--Xsynchronizer-worldStateMinMillisBeforeStalling",
      hidden = true,
      defaultValue = "5",
      paramLabel = "<LONG>",
      description =
          "Performance config for worldStateMinMillisBeforeStalling (default: ${DEFAULT-VALUE})")
  public long worldStateMinMillisBeforeStalling = TimeUnit.MINUTES.toMillis(5);
}
