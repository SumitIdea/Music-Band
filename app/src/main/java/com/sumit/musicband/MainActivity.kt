package com.sumit.musicband

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sumit.musicband.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var musicAdapter : MusicAdapter
    companion object{
        lateinit var MusicListMA : ArrayList<Music>
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestRuntimePermission()
        setTheme(R.style.Theme_MusicBand)
        binding  = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // for status bar color change
        val window: Window = this.window
        window.statusBarColor = ContextCompat.getColor(this,R.color.cool_pink);

        val actionBar: ActionBar? = supportActionBar
        val colorDrawable = ColorDrawable(ContextCompat.getColor(this,R.color.cool_pink))
        actionBar?.setBackgroundDrawable(colorDrawable)

        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermission()){
            initializer()

        }

            binding.shuffleBtn.setOnClickListener {
                val intent = Intent(this,PlayerActivity::class.java)
                intent.putExtra("index", 0)
                intent.putExtra("class", "MainActivity")
                startActivity(intent)
        }
        binding.favouriteBtn.setOnClickListener {
            startActivity(Intent(this,FavouriteActivity::class.java))
        }
        binding.playlistBtn.setOnClickListener {
            startActivity(Intent(this,PlaylistActivity::class.java))
        }
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId)
            {
                R.id.navSettings -> Toast.makeText(this,"Setting",Toast.LENGTH_LONG).show()
                R.id.navFeedback -> Toast.makeText(this,"Feedback",Toast.LENGTH_LONG).show()
                R.id.navExit -> exit()
                R.id.navAbout -> Toast.makeText(this,"About",Toast.LENGTH_LONG).show()

            }
            true
        }
    }

    fun exit(){
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Close Music")
        builder.setMessage("Are you sure want to exit ?")
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            finishAffinity();
            System.exit(0);
        }
        builder.setNegativeButton(android.R.string.no) { dialog, which ->
           builder.setCancelable(true)
        }
        builder.setNeutralButton("Maybe") { dialog, which ->
            Toast.makeText(applicationContext,
                "Maybe", Toast.LENGTH_SHORT).show()
        }
        val customDailog = builder.create()
        customDailog.show()
        customDailog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        customDailog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
    }

    //for requesting permission
    private fun requestRuntimePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),13)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode : Int,
        permission: Array<out String>,
        grantResult: IntArray
     ){
    super.onRequestPermissionsResult(requestCode,permission,grantResult)
        if (grantResult.isNotEmpty() && grantResult[0] == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this,"Permission Granted", Toast.LENGTH_LONG).show()
            initializer()

        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),13)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }
    fun initializer(){
               MusicListMA=getAllAudio()
//        val musicList = ArrayList<String>()
//        musicList.add("1 Song")
//        musicList.add("2 Song")
//        musicList.add("3 Song")
//        musicList.add("4 Song")
//        musicList.add("5 Song")
//        musicList.add("6 Song")
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this)
         musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.musicRV.adapter = musicAdapter
        binding.totalSongs.text  = "Total Songs : "+musicAdapter.itemCount
    }

    @SuppressLint("Range")
    fun getAllAudio() : ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection =     MediaStore.Audio.Media.IS_MUSIC+" != 0"
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ALBUM_ID)
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,selection,
            null,MediaStore.Audio.Media.DATE_ADDED+ " DESC " , null)
        if (cursor!=null){
            if (cursor.moveToFirst())
                do{
                    val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUri = Uri.withAppendedPath(uri, albumIdC.toString()).toString()
                    val music = Music(id=idC, title=title, album = albumC, artist = artistC, path = pathC, duration = durationC, artUri = artUri)
                    val file = File(music.path)
                    if(file.exists())
                        tempList.add(music)
                }while (cursor.moveToNext())
            cursor.close()
        }
        return tempList
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null)
        {
            exitApplication()
        }
    }
}
