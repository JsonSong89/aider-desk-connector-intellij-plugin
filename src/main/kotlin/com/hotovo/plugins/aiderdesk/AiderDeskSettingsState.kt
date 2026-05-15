package com.hotovo.plugins.aiderdesk

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "AiderDeskSettings",
    storages = [Storage("aiderDeskSettings.xml")]
)
@Service(Service.Level.APP)
class AiderDeskSettingsState : PersistentStateComponent<AiderDeskSettingsState.State> {
    companion object {
        const val DEFAULT_AIDER_DESK_URL = "http://localhost:24337"
    }

    data class State(
        var aiderDeskUrl: String = DEFAULT_AIDER_DESK_URL,
        var username: String = "",
        var password: String = ""
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var aiderDeskUrl: String
        get() = state.aiderDeskUrl.ifBlank { DEFAULT_AIDER_DESK_URL }
        set(value) {
            state.aiderDeskUrl = value.ifBlank { DEFAULT_AIDER_DESK_URL }
        }

    var username: String
        get() = state.username
        set(value) {
            state.username = value
        }

    var password: String
        get() = state.password
        set(value) {
            state.password = value
        }
}
