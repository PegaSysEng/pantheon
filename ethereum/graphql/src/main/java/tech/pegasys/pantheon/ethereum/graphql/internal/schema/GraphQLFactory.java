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
package tech.pegasys.pantheon.ethereum.graphql.internal.schema;

import tech.pegasys.pantheon.ethereum.graphql.internal.methods.GraphQLMethod;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.EchoingWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphQLFactory {

  public static String SCHEMA = getSchemaFromResources();

  public static GraphQL buildGraphQL(final List<GraphQLMethod> methods) {
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(SCHEMA);

    RuntimeWiring.Builder runtimeWiringBuilder =
        RuntimeWiring.newRuntimeWiring()
            .scalar(
                GraphQLScalarType.newScalar()
                    .name("Bytes32")
                    .coercing(CoercingFactory.getBytes32())
                    .build())
            .scalar(
                GraphQLScalarType.newScalar()
                    .name("Address")
                    .coercing(CoercingFactory.getAddress())
                    .build())
            .scalar(
                GraphQLScalarType.newScalar()
                    .name("Bytes")
                    .coercing(CoercingFactory.getBytes())
                    .build())
            .scalar(
                GraphQLScalarType.newScalar()
                    .name("BigInt")
                    .coercing(CoercingFactory.getBigInt())
                    .build())
            .scalar(
                GraphQLScalarType.newScalar()
                    .name("Long")
                    .coercing(CoercingFactory.getLong())
                    .build())
            .wiringFactory(new EchoingWiringFactory());

    methods.forEach(
        method ->
            runtimeWiringBuilder.type(
                method.getType().getType(),
                builder -> builder.dataFetcher(method.getType().getField(), (DataFetcher) method)));

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema =
        schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private static String getSchemaFromResources() {
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    InputStream is = classLoader.getResourceAsStream("schema.graphql");
    if (is != null) {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
    return null;
  }
}
