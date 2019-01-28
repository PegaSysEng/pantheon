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
package tech.pegasys.pantheon.cli;

import tech.pegasys.pantheon.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

public class CommandLineUtils {
  /**
   * Check if options are passed that reauire --rpc-http-enabled option to be true to have any
   * effect and show an error with the list of affected options.
   *
   * <p>Note that in future version of PicoCLI some options dependency mechanism may be implemented
   * that could replace this. See https://github.com/remkop/picocli/issues/295
   */
  public static void checkOptionDependencies(
      final CommandLine commandLine,
      final Object commandClass,
      final String mainOptionName,
      final boolean isMainOptionCondition,
      final List<String> dependentOptionsNames) {
    if (isMainOptionCondition) {
      String affectedOptions =
          commandLine
              .getCommandSpec()
              .options()
              .stream()
              .filter(
                  option ->
                      Arrays.stream(option.names()).anyMatch(dependentOptionsNames::contains)
                          && !option.stringValues().isEmpty())
              .map(option -> option.names()[0])
              .collect(
                  Collectors.collectingAndThen(
                      Collectors.toList(), StringUtils.joiningWithLastDelimiter(", ", " and ")));

      if (!affectedOptions.isEmpty()) {
        throw new ParameterException(
            new CommandLine(commandClass),
            String.format("%1$s require %2$s option to be true.", affectedOptions, mainOptionName));
      }
    }
  }
}
