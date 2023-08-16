package com.bhavinqf.downloadandstoreimage.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import com.bhavinqf.downloadandstoreimage.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

class CustomVideoDialog(val activity: Activity,val videoUrl:Uri) : Dialog(activity) {

    private var player: ExoPlayer? = null
    var playerView: StyledPlayerView? = null
    private var currentPlayer: StyledPlayerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_video_dialog)

        playerView = findViewById(R.id.player_view)
        // Keep the current Player View instance.
        currentPlayer = playerView
        setFullScreenListener()
        initPlayer()
    }


    private fun initPlayer() {
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(activity)
        val mediaSourceFactory: MediaSource.Factory = DefaultMediaSourceFactory(dataSourceFactory)

        // Create an ExoPlayer and set it as the player for content.
        player = ExoPlayer.Builder(activity).setMediaSourceFactory(mediaSourceFactory).build()
        playerView?.player = player

        // Create the MediaItem to play, specifying the content URI and ad tag URI.
        val mediaItem: MediaItem.Builder = MediaItem.Builder().setUri(videoUrl)

        // Prepare the content and ad to be played with the SimpleExoPlayer.
        player!!.setMediaItem(mediaItem.build())
        player!!.prepare()

        // Set Player Properties
        player!!.playWhenReady = true


    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setFullScreenListener() {
        // Creating a new Player View and place it inside a Full Screen Dialog.
        val fullScreenPlayerView = StyledPlayerView(activity)
        val dialog = object : Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen){
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
                // User pressed back button. Exit Full Screen Mode.
                playerView?.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_fullscreen)
                    ?.setImageResource(R.drawable.ic_fullscreen_expand)
                player?.let { StyledPlayerView.switchTargetView(it, fullScreenPlayerView, playerView) }
                currentPlayer = playerView
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                super.onBackPressed()
            }

        }
        dialog.addContentView(
            fullScreenPlayerView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        // Adding Full Screen Button Click Listeners.
        playerView?.setFullscreenButtonClickListener {
            // If full Screen Dialog is not visible, make player full screen.
            if(!dialog.isShowing){
                dialog.show()
                fullScreenPlayerView.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_fullscreen)
                    .setImageResource(R.drawable.ic_fullscreen_shrink)
                player?.let { StyledPlayerView.switchTargetView(it, playerView, fullScreenPlayerView) }
                currentPlayer = fullScreenPlayerView
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
        fullScreenPlayerView.setFullscreenButtonClickListener {
            // Exit Full Screen.
            playerView?.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_fullscreen)
                ?.setImageResource(R.drawable.ic_fullscreen_expand)
            player?.let { StyledPlayerView.switchTargetView(it, fullScreenPlayerView, playerView) }
            currentPlayer = playerView
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            dialog.dismiss()
        }
    }

   /* override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > 23) {
            initPlayer()
            currentPlayer?.onResume()
        }
    }*/

    override fun onStop() {
        super.onStop()
        currentPlayer?.player = null
        player!!.release()
        player = null
    }
}
