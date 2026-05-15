package com.hotovo.plugins.aiderdesk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class AiderDeskSettingsConfigurable : Configurable {
    private var panel: JPanel? = null
    private var aiderDeskUrlField: JBTextField? = null
    private var usernameField: JBTextField? = null
    private var passwordField: JBPasswordField? = null

    override fun getDisplayName(): String = "AiderDesk"

    override fun createComponent(): JComponent {
        aiderDeskUrlField = JBTextField()
        usernameField = JBTextField()
        passwordField = JBPasswordField()

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("AiderDesk URL:", aiderDeskUrlField!!)
            .addLabeledComponent("Username:", usernameField!!)
            .addLabeledComponent("Password:", passwordField!!)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val settings = settings()
        return aiderDeskUrlField?.text != settings.aiderDeskUrl ||
            usernameField?.text != settings.username ||
            String(passwordField?.password ?: CharArray(0)) != settings.password
    }

    override fun apply() {
        val settings = settings()
        settings.aiderDeskUrl = aiderDeskUrlField?.text.orEmpty().trim().trimEnd('/')
        settings.username = usernameField?.text.orEmpty()
        settings.password = String(passwordField?.password ?: CharArray(0))
    }

    override fun reset() {
        val settings = settings()
        aiderDeskUrlField?.text = settings.aiderDeskUrl
        usernameField?.text = settings.username
        passwordField?.text = settings.password
    }

    override fun disposeUIResources() {
        panel = null
        aiderDeskUrlField = null
        usernameField = null
        passwordField = null
    }

    private fun settings(): AiderDeskSettingsState {
        return ApplicationManager.getApplication().getService(AiderDeskSettingsState::class.java)
    }
}
