package com.abc.us.accounting

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.junitxml.JunitXmlReporter
import io.kotest.extensions.spring.SpringExtension
import mu.KotlinLogging
import org.testcontainers.containers.DockerComposeContainer
import java.io.File

class DockerComposeIntegrationTest  : AbstractProjectConfig() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private fun environment() = DockerComposeContainer(File("docker/docker-compose.yml"))

    override fun extensions() = listOf(
        SpringExtension,
        JunitXmlReporter(
            includeContainers = false,
            useTestPathAsName = true,
        ),
    )

    override suspend fun afterProject() {
        environment().stop()
        environment().close()
        logger.info { "afterProject, docker-compose stop!" }
    }

    override suspend fun beforeProject() {
        environment().withEnv("JAVA_OPTS", "-Xmx8192m")
        environment().start()
        logger.info { "afterProject, docker-compose started!" }
    }
}
