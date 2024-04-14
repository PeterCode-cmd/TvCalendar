package com.example.tvcalendar

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class VideoActivity : AppCompatActivity() {

    lateinit var youtubePlayerView: YouTubePlayerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_video)

        actionBar?.hide()

        youtubePlayerView = findViewById(R.id.youTubePlayerView)

        lifecycle.addObserver(youtubePlayerView)


        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
                val videoId = intent.getStringExtra("videoId") ?: ""
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })

    }
}