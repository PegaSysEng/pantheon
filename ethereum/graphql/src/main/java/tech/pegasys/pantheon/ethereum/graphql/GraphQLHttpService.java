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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;
import static tech.pegasys.pantheon.ethereum.graphql.internal.schema.Schema.SCHEMA;
import static tech.pegasys.pantheon.util.NetworkUtility.urlForSocketAddress;

import tech.pegasys.pantheon.ethereum.graphql.authentication.AuthenticationService;
import tech.pegasys.pantheon.ethereum.graphql.authentication.AuthenticationUtils;
import tech.pegasys.pantheon.ethereum.graphql.internal.GraphQLRequest;
import tech.pegasys.pantheon.ethereum.graphql.internal.GraphQLServiceException;
import tech.pegasys.pantheon.ethereum.graphql.internal.methods.GraphQLMethod;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLErrorResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.GraphQLSuccessResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.RequestError;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.RequestErrorResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.response.UnauthorizedResponse;
import tech.pegasys.pantheon.ethereum.graphql.internal.schema.GraphQLFactory;
import tech.pegasys.pantheon.metrics.LabelledMetric;
import tech.pegasys.pantheon.metrics.MetricCategory;
import tech.pegasys.pantheon.metrics.MetricsSystem;
import tech.pegasys.pantheon.metrics.OperationTimer;
import tech.pegasys.pantheon.util.NetworkUtility;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GraphQLHttpService {

  private static final Logger LOG = LogManager.getLogger();

  private static final InetSocketAddress EMPTY_SOCKET_ADDRESS = new InetSocketAddress("0.0.0.0", 0);
  private static final String APPLICATION_JSON = "application/json";
  private static final String TEXT_PLAIN = "text/plain";

  private final Vertx vertx;
  private final GraphQLConfiguration config;
  private final GraphQL graphQL;
  private final Map<String, GraphQLMethod> methods;
  private final Path dataDir;
  private final LabelledMetric<OperationTimer> requestTimer;

  @VisibleForTesting public final Optional<AuthenticationService> authenticationService;

  private HttpServer httpServer;

  /**
   * Construct a GraphQLHttpService handler
   *
   * @param vertx The vertx process that will be running this service
   * @param dataDir The data directory where requests can be buffered
   * @param config Configuration for the rpc methods being loaded
   * @param metricsSystem The metrics service that activities should be reported to
   * @param methods The graphQL queries
   */
  public GraphQLHttpService(
      final Vertx vertx,
      final Path dataDir,
      final GraphQLConfiguration config,
      final MetricsSystem metricsSystem,
      final Map<String, GraphQLMethod> methods) {
    this(
        vertx,
        dataDir,
        config,
        metricsSystem,
        methods,
        AuthenticationService.create(vertx, config));
  }

  private GraphQLHttpService(
      final Vertx vertx,
      final Path dataDir,
      final GraphQLConfiguration config,
      final MetricsSystem metricsSystem,
      final Map<String, GraphQLMethod> methods,
      final Optional<AuthenticationService> authenticationService) {
    this.dataDir = dataDir;
    requestTimer =
        metricsSystem.createLabelledTimer(
            MetricCategory.RPC,
            "request_time",
            "Time taken to process a JSON-RPC request",
            "methodName");
    validateConfig(config);
    this.config = config;
    this.vertx = vertx;
    this.authenticationService = authenticationService;
    this.methods = methods;
    this.graphQL = GraphQLFactory.buildGraphQL(new ArrayList<>(methods.values()));
  }

  private void validateConfig(final GraphQLConfiguration config) {
    checkArgument(
        config.getPort() == 0 || NetworkUtility.isValidPort(config.getPort()),
        "Invalid port configuration.");
    checkArgument(config.getHost() != null, "Required host is not configured.");
  }

  public CompletableFuture<?> start() {
    LOG.info("Starting JsonRPC service on {}:{}", config.getHost(), config.getPort());
    // Create the HTTP server and a router object.
    httpServer =
        vertx.createHttpServer(
            new HttpServerOptions().setHost(config.getHost()).setPort(config.getPort()));

    // Handle json rpc requests
    final Router router = Router.router(vertx);

    // Verify Host header to avoid rebind attack.
    router.route().handler(checkWhitelistHostHeader());

    router
        .route()
        .handler(
            CorsHandler.create(buildCorsRegexFromConfig())
                .allowedHeader("*")
                .allowedHeader("content-type"));
    router
        .route()
        .handler(
            BodyHandler.create()
                .setUploadsDirectory(dataDir.resolve("uploads").toString())
                .setDeleteUploadedFilesOnEnd(true));
    router.route("/").method(HttpMethod.GET).handler(this::handleEmptyRequest);
    router
        .route("/graphql")
        .method(HttpMethod.POST)
        .produces(APPLICATION_JSON)
        .handler(this::handleGraphQLRequest);
    router
        .route("/graphql/schema")
        .method(HttpMethod.POST)
        .produces(APPLICATION_JSON)
        .handler(this::handleGraphQLSchemaRequest);

    if (authenticationService.isPresent()) {
      router
          .route("/login")
          .method(HttpMethod.POST)
          .produces(APPLICATION_JSON)
          .handler(authenticationService.get()::handleLogin);
    } else {
      router
          .route("/login")
          .method(HttpMethod.POST)
          .produces(APPLICATION_JSON)
          .handler(AuthenticationService::handleDisabledLogin);
    }

    final CompletableFuture<?> resultFuture = new CompletableFuture<>();
    httpServer
        .requestHandler(router)
        .listen(
            res -> {
              if (!res.failed()) {
                resultFuture.complete(null);
                LOG.info(
                    "GraphQL service started and listening on {}:{}",
                    config.getHost(),
                    httpServer.actualPort());
                return;
              }
              httpServer = null;
              final Throwable cause = res.cause();
              if (cause instanceof SocketException) {
                resultFuture.completeExceptionally(
                    new GraphQLServiceException(
                        String.format(
                            "Failed to bind Ethereum JSON RPC listener to %s:%s: %s",
                            config.getHost(), config.getPort(), cause.getMessage())));
                return;
              }
              resultFuture.completeExceptionally(cause);
            });

    return resultFuture;
  }

  private Handler<RoutingContext> checkWhitelistHostHeader() {
    return event -> {
      final Optional<String> hostHeader = getAndValidateHostHeader(event);
      if (config.getHostsWhitelist().contains("*")
          || (hostHeader.isPresent() && hostIsInWhitelist(hostHeader.get()))) {
        event.next();
      } else {
        event
            .response()
            .setStatusCode(403)
            .putHeader("Content-Type", "application/json; charset=utf-8")
            .end("{\"message\":\"Host not authorized.\"}");
      }
    };
  }

  private String getAuthToken(final RoutingContext routingContext) {
    return AuthenticationUtils.getJwtTokenFromAuthorizationHeaderValue(
        routingContext.request().getHeader("Authorization"));
  }

  private Optional<String> getAndValidateHostHeader(final RoutingContext event) {
    final Iterable<String> splitHostHeader = Splitter.on(':').split(event.request().host());
    final long hostPieces = stream(splitHostHeader).count();
    if (hostPieces > 1) {
      // If the host contains a colon, verify the host is correctly formed - host [ ":" port ]
      if (hostPieces > 2 || !Iterables.get(splitHostHeader, 1).matches("\\d{1,5}+")) {
        return Optional.empty();
      }
    }
    return Optional.ofNullable(Iterables.get(splitHostHeader, 0));
  }

  private boolean hostIsInWhitelist(final String hostHeader) {
    return config.getHostsWhitelist().stream()
        .anyMatch(whitelistEntry -> whitelistEntry.toLowerCase().equals(hostHeader.toLowerCase()));
  }

  public CompletableFuture<?> stop() {
    if (httpServer == null) {
      return CompletableFuture.completedFuture(null);
    }

    final CompletableFuture<?> resultFuture = new CompletableFuture<>();
    httpServer.close(
        res -> {
          if (res.failed()) {
            resultFuture.completeExceptionally(res.cause());
          } else {
            httpServer = null;
            resultFuture.complete(null);
          }
        });
    return resultFuture;
  }

  public InetSocketAddress socketAddress() {
    if (httpServer == null) {
      return EMPTY_SOCKET_ADDRESS;
    }
    return new InetSocketAddress(config.getHost(), httpServer.actualPort());
  }

  @VisibleForTesting
  public String url() {
    if (httpServer == null) {
      return "";
    }
    return urlForSocketAddress("http", socketAddress());
  }

  private void handleGraphQLSchemaRequest(final RoutingContext routingContext) {
    // first check token if authentication is required
    String token = getAuthToken(routingContext);
    if (authenticationService.isPresent() && token == null) {
      // no auth token when auth required
      handleGraphQLUnauthorizedError(routingContext, RequestError.UNAUTHORIZED);
    } else {
      // Parse json
      try {
        AuthenticationUtils.getUser(
            authenticationService,
            token,
            user -> {
              final HttpServerResponse response = routingContext.response();
              vertx.executeBlocking(
                  future -> {
                    future.complete(SCHEMA);
                  },
                  false,
                  (res) -> {
                    if (res.failed()) {
                      response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                      return;
                    }

                    final String schemaResponse = (String) res.result();
                    response.setStatusCode(200);
                    response.putHeader("Content-Type", TEXT_PLAIN);
                    response.end(schemaResponse);
                  });
            });
      } catch (final DecodeException ex) {
        handleRequestError(routingContext, RequestError.PARSE_ERROR);
      }
    }
  }

  private void handleGraphQLRequest(final RoutingContext routingContext) {
    // first check token if authentication is required
    String token = getAuthToken(routingContext);
    if (authenticationService.isPresent() && token == null) {
      // no auth token when auth required
      handleGraphQLUnauthorizedError(routingContext, RequestError.UNAUTHORIZED);
    } else {
      // Parse json
      try {
        final String json = routingContext.getBodyAsString().trim();
        if (!json.isEmpty() && json.charAt(0) == '{') {
          AuthenticationUtils.getUser(
              authenticationService,
              token,
              user -> {
                handleGraphQLSingleRequest(routingContext, new JsonObject(json), user);
              });
        } else if (json.length() == 0) {
          handleRequestError(routingContext, RequestError.EMPTY_QUERY_ERROR);

        } else {
          final JsonArray array = new JsonArray(json);
          if (array.size() < 1) {
            handleRequestError(routingContext, RequestError.INVALID_REQUEST);
            return;
          }
          AuthenticationUtils.getUser(
              authenticationService,
              token,
              user -> {
                handleGraphQLBatchRequest(routingContext, array, user);
              });
        }

      } catch (final DecodeException ex) {
        handleRequestError(routingContext, RequestError.PARSE_ERROR);
      }
    }
  }

  // Facilitate remote health-checks in AWS, inter alia.
  private void handleEmptyRequest(final RoutingContext routingContext) {
    routingContext.response().setStatusCode(201).end();
  }

  private void handleGraphQLSingleRequest(
      final RoutingContext routingContext, final JsonObject json, final Optional<User> user) {
    final HttpServerResponse response = routingContext.response();
    vertx.executeBlocking(
        future -> {
          final GraphQLResponse graphQLResponse = process(json, user);
          future.complete(graphQLResponse);
        },
        false,
        (res) -> {
          if (res.failed()) {
            response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            return;
          }

          final GraphQLResponse graphQLResponse = (GraphQLResponse) res.result();
          response.setStatusCode(status(graphQLResponse).code());
          response.putHeader("Content-Type", APPLICATION_JSON);
          response.end(serialise(graphQLResponse));
        });
  }

  private HttpResponseStatus status(final GraphQLResponse response) {

    switch (response.getType()) {
      case UNAUTHORIZED:
        return HttpResponseStatus.UNAUTHORIZED;
      case ERROR:
        return HttpResponseStatus.BAD_REQUEST;
      case SUCCESS:
      default:
        return HttpResponseStatus.OK;
    }
  }

  private String serialise(final GraphQLResponse response) {
    return Json.encodePrettily(response);
  }

  @SuppressWarnings("rawtypes")
  private void handleGraphQLBatchRequest(
      final RoutingContext routingContext, final JsonArray queryArray, final Optional<User> user) {
    // Interpret json as rpc request
    final List<Future> responses =
        queryArray.stream()
            .map(
                obj -> {
                  if (!(obj instanceof JsonObject)) {
                    return Future.succeededFuture(errorResponse(RequestError.INVALID_REQUEST));
                  }

                  final JsonObject req = (JsonObject) obj;
                  final Future<GraphQLResponse> fut = Future.future();
                  vertx.executeBlocking(
                      future -> future.complete(process(req, user)),
                      false,
                      ar -> {
                        if (ar.failed()) {
                          fut.fail(ar.cause());
                        } else {
                          fut.complete((GraphQLResponse) ar.result());
                        }
                      });
                  return fut;
                })
            .collect(toList());

    CompositeFuture.all(responses)
        .setHandler(
            (res) -> {
              if (res.failed()) {
                routingContext
                    .response()
                    .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .end();
                return;
              }
              final GraphQLResponse[] completed =
                  res.result().list().stream()
                      .map(GraphQLResponse.class::cast)
                      .toArray(GraphQLResponse[]::new);

              routingContext.response().end(Json.encode(completed));
            });
  }

  private GraphQLResponse process(final JsonObject query, final Optional<User> user) {
    final GraphQLRequest request;
    try {
      LOG.debug(query);
      request = query.mapTo(GraphQLRequest.class);
    } catch (final IllegalArgumentException exception) {
      return errorResponse(RequestError.INVALID_REQUEST);
    }

    LOG.debug("GraphQL query -> \n {}", request.getQuery());

    if (AuthenticationUtils.isPermitted(
        authenticationService, user, methods.get(request.getOperationName()))) {
      // Generate response
      try (final OperationTimer.TimingContext ignored =
          requestTimer.labels(request.getOperationName()).startTimer()) {
        ExecutionResult executionResult =
            graphQL.execute(
                ExecutionInput.newExecutionInput()
                    .query(request.getQuery())
                    .operationName(request.getOperationName())
                    .variables(request.getVariables())
                    .build());

        LOG.debug("Errors {}", executionResult.getErrors());

        LOG.debug("Spec result {}", executionResult.toSpecification());
        if (executionResult.getErrors().size() == 0) {
          return Json.mapper.convertValue(
              executionResult.toSpecification(), GraphQLSuccessResponse.class);
        } else {
          return new GraphQLErrorResponse(executionResult.getErrors());
        }
      } catch (final RuntimeException e) {
        LOG.error("Error processing GraphQL request", e);
        return errorResponse(RequestError.INTERNAL_ERROR);
      }
    } else {
      return unauthorizedResponse(RequestError.UNAUTHORIZED);
    }
  }

  private void handleRequestError(final RoutingContext routingContext, final RequestError error) {
    routingContext
        .response()
        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
        .end(Json.encode(new RequestErrorResponse(error)));
  }

  private void handleGraphQLUnauthorizedError(
      final RoutingContext routingContext, final RequestError error) {
    routingContext
        .response()
        .setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
        .end(Json.encode(new UnauthorizedResponse(error)));
  }

  private GraphQLResponse errorResponse(final RequestError error) {
    return new RequestErrorResponse(error);
  }

  private GraphQLResponse unauthorizedResponse(final RequestError error) {
    return new UnauthorizedResponse(error);
  }

  private String buildCorsRegexFromConfig() {
    if (config.getCorsAllowedDomains().isEmpty()) {
      return "";
    }
    if (config.getCorsAllowedDomains().contains("*")) {
      return "*";
    } else {
      final StringJoiner stringJoiner = new StringJoiner("|");
      config.getCorsAllowedDomains().stream().filter(s -> !s.isEmpty()).forEach(stringJoiner::add);
      return stringJoiner.toString();
    }
  }
}
