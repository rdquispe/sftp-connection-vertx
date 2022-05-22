package com.rodrigo.sftp_connection_vertx

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory

class Application {

    companion object {

        private val LOG = LoggerFactory.getLogger(Application::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val vertex = Vertx.vertx()
            val mainVerticle = MainVerticle()

            vertex.deployVerticle(mainVerticle, DeploymentOptions().apply {})

            LOG.info("Application Success Running")
        }
    }
}