package com.rodrigo.sftp_connection_vertx

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory

class Application {

    companion object {

        private val logger = LoggerFactory.getLogger(Application::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val vertex = Vertx.vertx()
            val mainVerticle = MainVerticle()

            vertex.deployVerticle(mainVerticle, DeploymentOptions().apply {})

            logger.info("Application Success Running")
        }
    }
}