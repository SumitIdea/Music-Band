package com.sumit.musicband

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sumit.musicband.databinding.ActivityFavouriteBinding


class FavouriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavouriteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.splash_screen)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // for status bar color change
        val window: Window = this.window
        window.statusBarColor = ContextCompat.getColor(this, R.color.cool_green);
        binding.backBtnFA.setOnClickListener {
            super.onBackPressed();
        }
    }

}