package com.madappgang.architecture.recorder.view_state_model

import java.io.File


data class FolderViewState(val folderUUID: String, val editing: Boolean = false, val scrollOffset: Double = 0.0, val action: Action? = null, val file: File? = null) {
    enum class Action {
        TOGGLE_EDITING,
        SHOW_CREATE_FOLDER,
        SHOW_RECORD_VIEW,
        SHOW_PLAYER_VIEW,
        SHOW_SAVE_RECORDING,
        PUSH_FOLDER,
        POP_FOLDER,
        DISMISS_ALERT
    }
}

data class RecorderViewState(val recordDuration: Long = 0, val action: RecorderViewState.Action? = null) {
    enum class Action {
        UPDATE_RECORD_DURATION,
        DISMISS_RECORDING
    }
}

data class PlayerViewState(val uuid: String?, var playState: Int? = null) {
    enum class Action {
        UPDATE_PLAY_STATE,
        TOGGLE_PLAY,
        CHANGE_PLAYBACK_POSITION
    }
}