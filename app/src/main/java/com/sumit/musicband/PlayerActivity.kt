package com.sumit.musicband

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sumit.musicband.databinding.ActivityPlayerBinding
import kotlin.system.exitProcess


class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        lateinit var musicListPA : ArrayList<Music>
        var songPosition :Int= 0
        var isPlaying : Boolean = false
        var musicService : MusicServices ? = null
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false

    }
//    private  lateinit var binding: ActivityPlayerBinding
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MusicBand)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // for Starting Services
        val intentService  = Intent(this,MusicServices::class.java)
        bindService(intentService,this, BIND_AUTO_CREATE)
        startService(intentService)

        //for initializing
        initializeLayout()
        binding.playPauseBtnPA.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }
        binding.previousBtnPA.setOnClickListener { prevNextSong(increment = false) }
        binding.nextBtnPA.setOnClickListener { prevNextSong(increment = true) }

        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))
        }
    binding.backBtnPA.setOnClickListener {
        startActivity(Intent(this,MainActivity::class.java))
    }
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
                }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

    binding.equalizerBtnPA.setOnClickListener {
        try {
            val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
            eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
            eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            startActivityForResult(eqIntent, 13)
        }catch (e: Exception){Toast.makeText(this,  "Equalizer Feature not Supported!!", Toast.LENGTH_SHORT).show()}
    }

    binding.repeatBtnPA.setOnClickListener {
        if(!repeat){
            repeat = true
            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }else{
            repeat = false
            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
        }
    }

    binding.timerBtnPA.setOnClickListener {
        val timer = min15 || min30 || min60
        if (!timer) showBottomDailog()
        else{
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Stop Timer")
            builder.setMessage("Are you sure want to stop timer ?")
            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                min15 = false
                min30 = false
                min60 = false
                binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                builder.setCancelable(true)
            }

            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    }

    fun showBottomDailog() {
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottom_sheet_dailog)
        bottomSheet.show()
        bottomSheet.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(this, "Music will stop after 15 minutes", Toast.LENGTH_LONG).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min15 = true
            Thread {
                Thread.sleep((15 * 60000).toLong())
                if (min15) exitApplication()
            }.start()
            bottomSheet.dismiss()
        }
        bottomSheet.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(this, "Music will stop after 30 minutes", Toast.LENGTH_LONG).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min30 = true
            Thread {
                Thread.sleep((30 * 60000).toLong())
                if (min30)  exitApplication()
            }.start()
            bottomSheet.dismiss()
        }

        bottomSheet.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(this, "Music will stop after 60 minutes", Toast.LENGTH_LONG).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min30 = true
            Thread {
                Thread.sleep((60 * 60000).toLong())
                if (min30) exitApplication()
            }.start()
            bottomSheet.dismiss()

           }
    }
    fun initializeLayout(){
        songPosition = intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){
            "MusicAdapter" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }
    private fun setLayout() {
        Glide.with(applicationContext)
            .load(musicListPA[songPosition].artUri)
            .apply(
                RequestOptions().placeholder(R.drawable.music).centerCrop()
            )
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title
        if(repeat) binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(applicationContext, R.color.purple_500))
        if (min15 || min30 || min60)
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))

    }
    fun createMediaPlayer(){
        try{
            if (musicService!!.mediaPlayer == null)
                musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying=true
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0  //initial progress of seek bar is 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        }catch (e:Exception){
            return
        }
    }
    private fun playMusic(){
        isPlaying = true
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.mediaPlayer!!.start()
        musicService!!.showNotification(R.drawable.pause_icon)
    }
    private fun pauseMusic(){
        isPlaying = false
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.play_icon)
    }
    private fun prevNextSong(increment: Boolean){
        if(increment)
        {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else{
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicServices.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()

    }
    override fun onServiceDisconnected(name: ComponentName?) {
        musicService  = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e:Exception) {
                return }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 13 || resultCode == RESULT_OK)
            return
    }
}