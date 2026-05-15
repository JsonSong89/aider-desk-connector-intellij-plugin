package com.hotovo.plugins.aiderdesk

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.hotovo.plugins.aiderdesk.PathUtils

@Service(Service.Level.APP)
class AiderDeskConnectorAppService : Disposable {
    private val LOG = Logger.getInstance(AiderDeskConnectorAppService::class.java)
    private val aiderDeskConnector: AiderDeskConnector = AiderDeskConnector()
    private val restClient: AiderDeskRestClient = AiderDeskRestClient()

    init {
        LOG.info("AiderDesk Connector instance created")
    }

    // Renamed for clarity, starts the core connector logic
    fun startServer() {
        try {
            LOG.info("Starting AiderDesk Connector background processes")
            aiderDeskConnector.start()
            LOG.info("AiderDesk Connector background processes started")
        } catch (e: Exception) {
            LOG.error("Failed to start AiderDesk Connector background processes", e)
        }
    }

    // Called when the application shuts down
    override fun dispose() {
        LOG.info("Disposing AiderDesk Connector App Service")
        stopServer()
    }

    // Connects a specific project to the running connector
    fun startProjectConnector(project: Project) {
        aiderDeskConnector.startProjectConnector(project)
    }

    // Provides the connection status for a specific project
    fun getConnectionStatus(project: Project): ConnectionStatus? {
       return aiderDeskConnector.getConnectionStatus(project)
    }

    fun sendFileMessage(message: FileMessage) {
        aiderDeskConnector.sendMessage(message)
    }

    fun syncWorkspace(project: Project) {
        aiderDeskConnector.syncWorkspace(project)
    }

    fun runPrompt(project: Project, prompt: String): Boolean {
        val projectDir = project.basePath ?: run {
            LOG.warn("Project base path is null for project: ${project.name}")
            return false
        }
        val normalizedProjectDir = PathUtils.normalizeProjectDir(projectDir)
        return restClient.postPromptAction("run-prompt", normalizedProjectDir, prompt)
    }

    fun savePrompt(project: Project, prompt: String): Boolean {
        val projectDir = project.basePath ?: run {
            LOG.warn("Project base path is null for project: ${project.name}")
            return false
        }
        val normalizedProjectDir = PathUtils.normalizeProjectDir(projectDir)
        return restClient.postPromptAction("save-prompt", normalizedProjectDir, prompt)
    }

    fun reconnect(project: Project) {
        aiderDeskConnector.reconnect(project)
    }

    fun disconnectProject(project: Project) {
        aiderDeskConnector.disconnectProject(project)
    }

    // Renamed for clarity, stops the core connector logic and disconnects all projects
    private fun stopServer() {
        try {
            LOG.info("Stopping AiderDesk Connector background processes and disconnecting projects")
            aiderDeskConnector.stop()
            LOG.info("AiderDesk Connector stopped successfully")
        } catch (e: Exception) {
            LOG.error("Failed to stop AiderDesk Connector", e)
        }
    }
}
