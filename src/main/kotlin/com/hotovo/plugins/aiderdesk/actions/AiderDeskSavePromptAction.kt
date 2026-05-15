package com.hotovo.plugins.aiderdesk.actions

import com.hotovo.plugins.aiderdesk.AiderDeskConnectorAppService
import com.intellij.openapi.project.Project

class AiderDeskSavePromptAction : AiderDeskPromptAction() {
    override fun execute(appService: AiderDeskConnectorAppService, project: Project, prompt: String): Boolean {
        return appService.savePrompt(project, prompt)
    }
}
