package com.rodrigo.sftp_connection_vertx

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory


class MainVerticle : AbstractVerticle() {

    private val LOG = LoggerFactory.getLogger(MainVerticle::class.java)

    companion object {
        const val PORT = 8080
        const val APPLICATION_JSON = "application/json"
    }

    override fun start(startPromise: Promise<Void>) {

        val router = Router.router(vertx).apply {
            get("/download").handler(::download)
        }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(PORT) { http ->
                if (http.succeeded()) {
                    startPromise.complete()
                    LOG.info("HTTP server started on port ${http.result().actualPort()}")
                } else {
                    startPromise.fail(http.cause());
                }
            }
    }

    private fun download(context: RoutingContext) {

        vertx.executeBlocking<List<String>>({ promise ->

            val files = Connection(vertx).download("/upload/examples/", "/tmp/uploads/")

            promise.complete(files)
        }, { ar ->

            if (ar.succeeded()) {
                context.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                    .end(JsonArray(ar.result()).encode())
            } else {
                context.response()
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                    .end(JsonObject().put("error", ar.failed().toString()).encode())
            }
        })
    }
}
