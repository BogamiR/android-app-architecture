package com.madappgang.architecture.recorder.activities


import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.madappgang.architecture.recorder.AppInstance
import com.madappgang.architecture.recorder.R
import com.madappgang.architecture.recorder.helpers.FileManager.Companion.recordFormat
import com.madappgang.architecture.recorder.helpers.Player
import com.madappgang.architecture.recorder.view_state_model.PlayerViewState
import com.madappgang.architecture.recorder.view_state_model.PlayerViewState.PlayerState.*
import kotlinx.android.synthetic.main.activity_player.*


class PlayerActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener, TextWatcher, Player.PlayerCallback {

    private lateinit var player: Player
    private var isClickOnButton = false
    private val handler = Handler()
    private lateinit var filePath: String
    private lateinit var fileDirectory: String
    private lateinit var fileName: String
    private lateinit var originalName: String
    private val fileManager = AppInstance.appInstance.fileManager
    private val viewStateStore = AppInstance.appInstance.viewStateStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setSupportActionBar(playerToolbar)

        val path = intent.getStringExtra(FILE_PATH)
        val resumePlay = currentViewState().originalFilePath.isNotEmpty()
                && currentViewState().filePath.isNotEmpty()
                && path == currentViewState().originalFilePath
        filePath = if (resumePlay) {
            currentViewState().filePath
        } else {
            viewStateStore.updateFilePath(path)
            viewStateStore.updateOriginalFilePath(path)
            playButton.text = getString(R.string.player_button_play)
            viewStateStore.changePlaybackPosition(0)
            path
        }

        originalName = filePath.split("/").last().removeSuffix(recordFormat)
        fileName = originalName
        fileDirectory = filePath.removeSuffix(recordFormat)
        fileDirectory = fileDirectory.substring(0, fileDirectory.length - (fileName.length + 1))
        player = Player(filePath, this)
        playerLabel.text = fileName
        editRecordName.setText(fileName)
        editRecordName.addTextChangedListener(this)
        seekBar.setOnSeekBarChangeListener(this)

        if (resumePlay && currentViewState().playerState != null && currentViewState().playerState != STOP) {
            seekToPosition(currentViewState().progress.let { it } ?: 0)
            if (currentViewState().playerState == PLAY) {
                startPlaying()
            } else {
                player.play()
                player.pause()
                playButton.text = getString(R.string.player_button_resume_play)
            }
        }

        viewStateStore.playerViewState.observe(this, Observer<PlayerViewState> {
            it?.let { handle(it) }
        })
    }

    override fun setDuration(duration: Int) {
        seekBar.max = duration
        this.duration.text = RecorderActivity.getTimeFormat(duration.toLong())
    }

    private fun seekChange() {
        viewStateStore.playerSeekTo(seekBar.progress)
    }

    private fun startPlayProgressUpdater() {
        if (player.isPlaying()) {
            isClickOnButton = false
            viewStateStore.changePlaybackPosition(player.getCurrentPosition())
            val notification = Runnable { startPlayProgressUpdater() }
            handler.postDelayed(notification, 10)
        } else if (!isClickOnButton) {
            viewStateStore.updatePlayState(STOP)
        }
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        Log.d("seek", "onProgressChanged")
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        Log.d("seek", "onStartTrackingTouch")
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        Log.d("seek", "onStopTrackingTouch")
        seekChange()
    }

    override fun afterTextChanged(p0: Editable?) {
        val newName = editRecordName.text.toString()
        if (fileName != newName) {
            playerLabel.text = newName
            fileName = newName
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    private fun renameFile() {
        val newFile = "$fileDirectory/$fileName$recordFormat"
        if (currentViewState().filePath != newFile) {
            if (fileManager.renameFile(currentViewState().filePath, newFile)) {
                viewStateStore.updateFilePath(newFile)
            }
        }
    }

    fun onClickPlay(v: View) {
        if (player.isPlaying()) {
            viewStateStore.updatePlayState(PAUSE)
        } else {
            viewStateStore.updatePlayState(PLAY)
        }
    }

    override fun onPause() {
        renameFile()
        isClickOnButton = true
        player.pause()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun currentViewState(): PlayerViewState = viewStateStore.playerViewState.value!!

    private fun handle(viewState: PlayerViewState) {
        when (viewState.action) {
            PlayerViewState.Action.CHANGE_PLAYBACK_POSITION -> {
                val progress = viewState.progress.let { it } ?: 0
                seekBar.progress = progress
                currentTime.text = RecorderActivity.getTimeFormat(progress.toLong())
            }
            PlayerViewState.Action.PLAYER_SEEK_TO -> {
                seekToPosition(viewState.progress.let { it } ?: 0)
            }
            PlayerViewState.Action.UPDATE_PLAY_STATE -> {
                when (viewState.playerState) {
                    PLAY -> {
                        isClickOnButton = true
                        startPlaying()
                    }
                    PAUSE -> {
                        isClickOnButton = true
                        player.pause()
                        playButton.text = getString(R.string.player_button_resume_play)
                    }
                    STOP -> {
                        player.pause()
                        playButton.text = getString(R.string.player_button_play)
                        viewStateStore.changePlaybackPosition(0)
                    }
                }
            }
        }
    }

    private fun startPlaying() {
        player.play()
        playButton.text = getString(R.string.player_button_pause)
        startPlayProgressUpdater()
    }

    private fun seekToPosition(progress: Int) {
        player.seekTo(progress)
        seekBar.progress = progress
        currentTime.text = RecorderActivity.getTimeFormat(progress.toLong())
    }

    companion object {
        val FILE_PATH = "file_path"

        fun start(context: Context, filePath: String) {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(FILE_PATH, filePath)
            context.startActivity(intent)
        }
    }
}
