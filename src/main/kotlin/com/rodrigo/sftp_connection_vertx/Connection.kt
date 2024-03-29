package com.rodrigo.sftp_connection_vertx

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelSftp.LsEntry
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory
import java.util.Vector


class Connection(private val vertx: Vertx) {

    private val logger = LoggerFactory.getLogger(Connection::class.java)
    private lateinit var channel: ChannelSftp
    private lateinit var session: Session

    fun download(pathRemote: String, pathLocal: String): List<String> {

        val files = mutableListOf<String>()

        try {
            directory(pathLocal)

            val ssh = JSch()

            session = ssh.getSession("<USERNAME>", "<HOST>", 22)
            session.setConfig("StrictHostKeyChecking", "no")
            session.setPassword("<PASSWORD>")
            session.connect()

            channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()

            channel.cd(pathRemote)
            val entries = channel.ls(".") as Vector<LsEntry>

            entries.forEach { entry ->
                if (!entry.attrs.isDir) {
                    channel.get("$pathRemote${entry.filename}", "$pathLocal${entry.filename}")
                    files.add(entry.filename)
                    logger.info("Filename: {}, Size: {}", entry.filename, entry.attrs.size)
                }
            }
        } catch (ex: JSchException) {
            logger.error(ex.message.toString())
        } catch (ex: SftpException) {
            logger.error(ex.message.toString())
        } finally {
            disconnect()
        }

        return files
    }

    private fun disconnect() {
        channel.disconnect()
        session.disconnect()
    }

    private fun directory(path: String) {
        vertx.fileSystem()
            .exists(path) { asyncResult ->
                if (asyncResult.result()) {
                    logger.warn("DIRECTORY_EXIST: {}", asyncResult.result())
                } else {
                    vertx.fileSystem().let { fileSystem ->
                        fileSystem.mkdir(path) { result ->
                            if (result.succeeded()) {
                                logger.info("DIRECTORY_CREATED: $path")
                            } else {
                                logger.error("DIRECTORY_ERROR: ${result.cause()}")
                                throw Exception("DIRECTORY_ERROR", result.cause())
                            }
                        }
                    }
                }
            }
    }
}