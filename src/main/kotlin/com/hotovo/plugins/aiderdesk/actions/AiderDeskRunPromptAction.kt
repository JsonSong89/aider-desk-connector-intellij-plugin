package com.hotovo.plugins.aiderdesk.actions

import com.hotovo.plugins.aiderdesk.AiderDeskConnectorAppService
import com.intellij.openapi.project.Project

class AiderDeskRunPromptAction : AiderDeskPromptAction() {
    override fun execute(appService: AiderDeskConnectorAppService, project: Project, prompt: String): Boolean {
        return appService.runPrompt(project, prompt)
    }
}
