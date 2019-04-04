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
package tech.pegasys.pantheon.services.kvstore;

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
public class RocksDBPerformanceOptions {

  private static final Supplier<RocksDBPerformanceOptions> cachedInstance =
      Suppliers.memoize(RocksDBPerformanceOptions::new);

  public static RocksDBPerformanceOptions get() {
    return cachedInstance.get();
  }

  @CommandLine.Option(
      names = {"--Xrocksdb-max-open-files"},
      hidden = true,
      defaultValue = "1024",
      paramLabel = "<INTEGER>",
      description = "Max number of files RocksDB will open (default: ${DEFAULT-VALUE})")
  public final Integer maxOpenFiles = 1024;
}
