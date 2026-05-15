package com.hotovo.plugins.aiderdesk.actions

import com.hotovo.plugins.aiderdesk.AiderDeskConnectorAppService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger

abstract class AiderDeskPromptAction : AnAction() {
    private val LOG = Logger.getInstance(AiderDeskPromptAction::class.java)

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val project = e.project
        e.presentation.isEnabledAndVisible =
            project?.basePath != null && !editor?.selectionModel?.selectedText.isNullOrBlank()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val prompt = e.getData(CommonDataKeys.EDITOR)?.selectionModel?.selectedText ?: return
        if (prompt.isBlank()) {
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            val appService = ApplicationManager.getApplication().getService(AiderDeskConnectorAppService::class.java)
            val success = execute(appService, project, prompt)
            if (!success) {
                LOG.warn("AiderDesk prompt action failed: ${e.presentation.text}")
            }
        }
    }

    protected abstract fun execute(
        appService: AiderDeskConnectorAppService,
        project: com.intellij.openapi.project.Project,
        prompt: String
    ): Boolean
}
