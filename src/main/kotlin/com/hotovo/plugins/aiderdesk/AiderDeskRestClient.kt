package com.hotovo.plugins.aiderdesk

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64

class AiderDeskRestClient {
    private val LOG = Logger.getInstance(AiderDeskRestClient::class.java)
    private val mapper = jacksonObjectMapper()

    fun postPromptAction(actionName: String, projectDir: String, prompt: String): Boolean {
        val taskId = getCurrentTaskId(projectDir) ?: return false
        val payload = mapOf(
            "projectDir" to projectDir,
            "taskId" to taskId,
            "prompt" to prompt
        )

        return try {
            val response = postJson("${aiderDeskBaseUrl()}/api/$actionName", mapper.writeValueAsString(payload))
            val success = response.statusCode in 200..299
            if (!success) {
                LOG.warn("AiderDesk $actionName failed. Status: ${response.statusCode}, body: ${response.body}")
            }
            success
        } catch (e: Exception) {
            LOG.error("Failed to call AiderDesk $actionName API", e)
            false
        }
    }

    private fun getCurrentTaskId(projectDir: String): String? {
        return try {
            val encodedProjectDir = URLEncoder.encode(projectDir, StandardCharsets.UTF_8)
            val response = get("${aiderDeskBaseUrl()}/api/project/tasks?projectDir=$encodedProjectDir")
            if (response.statusCode !in 200..299) {
                LOG.warn("Failed to fetch AiderDesk tasks. Status: ${response.statusCode}, body: ${response.body}")
                return null
            }

            val tasks: List<AiderDeskTask> = mapper.readValue(response.body)
            selectCurrentTask(tasks)?.id
        } catch (e: Exception) {
            LOG.error("Failed to fetch current AiderDesk task id", e)
            null
        }
    }

    private fun selectCurrentTask(tasks: List<AiderDeskTask>): AiderDeskTask? {
        val latestNamedTask = tasks.firstOrNull { it.name?.contains("最新") == true }
        if (latestNamedTask != null) {
            return latestNamedTask
        }

        return tasks.maxByOrNull { task ->
            task.updatedAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.EPOCH
        }
    }

    private fun get(url: String): RestResponse {
        val connection = openConnection(url, "GET")
        return readResponse(connection)
    }

    private fun postJson(url: String, body: String): RestResponse {
        // TODO: Change to LOG.debug() after debugging
        LOG.info("AiderDeskRestClient.postJson request. URL: $url, body: $body")

        val connection = openConnection(url, "POST")
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.outputStream.use { outputStream ->
            outputStream.write(body.toByteArray(StandardCharsets.UTF_8))
        }
        val response = readResponse(connection)

        // TODO: Change to LOG.debug() after debugging
        LOG.info("AiderDeskRestClient.postJson response. URL: $url, statusCode: ${response.statusCode}, body: ${response.body}")

        return response
    }

    private fun openConnection(url: String, method: String): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 10_000
        connection.readTimeout = 30_000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "AiderDesk-IntelliJ-Plugin")

        val settings = ApplicationManager.getApplication().getService(AiderDeskSettingsState::class.java)
        val username = settings.username
        val password = settings.password
        if (username.isNotBlank() && password.isNotBlank()) {
            val token = Base64.getEncoder()
                .encodeToString("$username:$password".toByteArray(StandardCharsets.UTF_8))
            connection.setRequestProperty("Authorization", "Basic $token")
        }

        return connection
    }

    private fun readResponse(connection: HttpURLConnection): RestResponse {
        return try {
            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val body = responseStream.use { stream ->
                stream.readBytes().toString(StandardCharsets.UTF_8)
            }
            RestResponse(statusCode, body)
        } finally {
            connection.disconnect()
        }
    }

    private fun aiderDeskBaseUrl(): String {
        val settings = ApplicationManager.getApplication().getService(AiderDeskSettingsState::class.java)
        return settings.aiderDeskUrl.trim().trimEnd('/').ifBlank { AiderDeskSettingsState.DEFAULT_AIDER_DESK_URL }
    }

    private data class RestResponse(
        val statusCode: Int,
        val body: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class AiderDeskTask(
        val id: String,
        val name: String? = null,
        val updatedAt: String? = null
    )
}
