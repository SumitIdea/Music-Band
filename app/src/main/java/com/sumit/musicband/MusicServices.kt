package com.sumit.musicband

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat


class MusicServices: Service() {
    private var myBinder = MyBinder()
    var mediaPlayer : MediaPlayer ?  = null
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder? {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }
    inner class MyBinder:Binder() {
        fun currentService() :MusicServices{
            return this@MusicServices
        }
    }


    fun showNotification(playPauseBtn: Int)
    {
        val intent = Intent(baseContext, MainActivity::class.java)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val preIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendIntent = PendingIntent.getBroadcast(baseContext,0, preIntent,flag)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendIntent = PendingIntent.getBroadcast(baseContext,0, playIntent,flag)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendIntent = PendingIntent.getBroadcast(baseContext,0, nextIntent,flag)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendIntent = PendingIntent.getBroadcast(baseContext,0, exitIntent,flag)

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if(imgArt != null){
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        }else{
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_slash_screen)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist+" -- "+"Sumit")
            .setSmallIcon(R.drawable.playlist_icon)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.previous_icon, "Previous", prevPendIntent)
            .addAction(playPauseBtn, "Play", playPendIntent)
            .addAction(R.drawable.next_icon, "Next", nextPendIntent)
            .addAction(R.drawable.exit_icon, "Exit", exitPendIntent)
            .build()


//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//            val playbackSpeed = if(PlayerActivity.isPlaying) 1F else 0F
//            mediaSession.setMetadata(MediaMetadataCompat.Builder()
//                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
//                .build())
//            val playBackState = PlaybackStateCompat.Builder()
//                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
//                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                .build()
//            mediaSession.setPlaybackState(playBackState)
//            mediaSession.setCallback(object: MediaSessionCompat.Callback(){
//                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
//                    return super.onMediaButtonEvent(mediaButtonEvent)
//                    }
//                })
//            }
        startForeground(13,notification)
    }

    fun createMediaPlayer(){
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0  //initial progress of seek bar is 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration
        }catch (e: Exception){return}
    }

    fun seekBarSetup(){
    runnable = Runnable {
        PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
        PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition.toLong().toInt()
        Handler(Looper.getMainLooper()).postDelayed(runnable,200)
            }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0) // kab start hoga ye handle 0 milli sec k baad start hoga
    }
}