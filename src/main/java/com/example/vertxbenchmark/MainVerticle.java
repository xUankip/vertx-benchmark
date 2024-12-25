package com.example.vertxbenchmark;

import com.example.vertxbenchmark.config.ConfigLoader;
import com.example.vertxbenchmark.model.User;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public class MainVerticle extends AbstractVerticle {
    private PgPool pool;

    @Override
    public void start(Promise<Void> startPromise) {
        ConfigLoader configLoader = new ConfigLoader(vertx);

        configLoader.loadConfig()
                .onSuccess(config -> {
                    // Khởi tạo PostgreSQL connection
                    PgConnectOptions connectOptions = new PgConnectOptions()
                            .setPort(config.getJsonObject("database").getInteger("port"))
                            .setHost(config.getJsonObject("database").getString("host"))
                            .setDatabase(config.getJsonObject("database").getString("database"))
                            .setUser(config.getJsonObject("database").getString("user"))
                            .setPassword(config.getJsonObject("database").getString("password"))
                            .setCachePreparedStatements(true)
                            .setTcpKeepAlive(true)
                            .setTcpNoDelay(true);

                    // Tối ưu connection pool
                    PoolOptions poolOptions = new PoolOptions()
                            .setMaxSize(config.getJsonObject("database").getInteger("poolSize"))
                            .setMaxWaitQueueSize(config.getJsonObject("database").getInteger("maxWaitQueueSize"));

                    pool = PgPool.pool(vertx, connectOptions, poolOptions);

                    Router router = Router.router(vertx);

                    // Hello World endpoint
                    router.get("/hello").handler(ctx -> {
                        JsonObject response = new JsonObject()
                                .put("message", "Hello World!")
                                .put("timestamp", System.currentTimeMillis());

                        ctx.response()
                                .putHeader("content-type", "application/json")
                                .end(response.encode());
                    });

                    // Single record endpoint
                    router.get("/single").handler(ctx -> {
                        pool.preparedQuery("SELECT * FROM users WHERE id = $1")
                                .execute(Tuple.of(1))
                                .onSuccess(rows -> {
                                    if (rows.iterator().hasNext()) {
                                        Row row = rows.iterator().next();
                                        User user = User.fromRow(row);
                                        ctx.response()
                                                .putHeader("content-type", "application/json")
                                                .end(JsonObject.mapFrom(user).encode());
                                    } else {
                                        ctx.response()
                                                .setStatusCode(404)
                                                .end(new JsonObject()
                                                        .put("error", "User not found")
                                                        .encode());
                                    }
                                })
                                .onFailure(err -> {
                                    ctx.response()
                                            .setStatusCode(500)
                                            .end(new JsonObject()
                                                    .put("error", err.getMessage())
                                                    .encode());
                                });
                    });

                    // Bulk records endpoint
                    router.get("/bulk").handler(ctx -> {
                        pool.query("SELECT * FROM users ORDER BY id LIMIT 1000")
                                .execute()
                                .onSuccess(rows -> {
                                    JsonArray result = new JsonArray();
                                    for (Row row : rows) {
                                        User user = User.fromRow(row);
                                        result.add(JsonObject.mapFrom(user));
                                    }
                                    ctx.response()
                                            .putHeader("content-type", "application/json")
                                            .end(result.encode());
                                })
                                .onFailure(err -> {
                                    ctx.response()
                                            .setStatusCode(500)
                                            .end(new JsonObject()
                                                    .put("error", err.getMessage())
                                                    .encode());
                                });
                    });

                    // Tối ưu HTTP server
                    HttpServerOptions serverOptions = new HttpServerOptions()
                            .setTcpFastOpen(true)
                            .setTcpNoDelay(true)
                            .setReusePort(true);

                    vertx.createHttpServer(serverOptions)
                            .requestHandler(router)
                            .listen(config.getJsonObject("http").getInteger("port"))
                            .onSuccess(http -> {
                                System.out.println("HTTP server started on port " +
                                        config.getJsonObject("http").getInteger("port"));
                                startPromise.complete();
                            })
                            .onFailure(startPromise::fail);
                })
                .onFailure(startPromise::fail);
    }

    public static void main(String[] args) {
        // Tối ưu Vert.x options
        VertxOptions options = new VertxOptions()
                .setPreferNativeTransport(true)
                .setEventLoopPoolSize(2 * Runtime.getRuntime().availableProcessors());

        Vertx vertx = Vertx.vertx(options);

        // Deploy nhiều instance của Verticle
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(Runtime.getRuntime().availableProcessors());

        vertx.deployVerticle(MainVerticle.class.getName(), deploymentOptions);
    }
}