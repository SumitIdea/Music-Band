package com.sumit.musicband

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.core.content.ContextCompat
import com.sumit.musicband.databinding.ActivityFavouriteBinding
import com.sumit.musicband.databinding.ActivityPlaylistBinding

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.splash_screen)
        binding  = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // for status bar color change
        val window: Window = this.window
        window.statusBarColor = ContextCompat.getColor(this,R.color.cool_blue);
        binding.backBtnPLA.setOnClickListener {
            super.onBackPressed();
        }
    }
}
