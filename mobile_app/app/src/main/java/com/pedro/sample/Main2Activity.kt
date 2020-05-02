package com.pedro.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media

class Main2Activity : AppCompatActivity(),IVLCVout.Callback, MediaPlayer.EventListener  {


    // declare media player object
    private var mediaPlayer: MediaPlayer?=null
    // declare surface view object
    private var mSurface: SurfaceView?=null
    // declare surface holder object
    private var holder: SurfaceHolder?= null
    // declare libvlc object
    private var libvlc: LibVLC?=null

    companion object {
        private var TAG= "VideoController"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        mSurface = findViewById<SurfaceView>(R.id.resend)
        var etUrl= findViewById<EditText>(R.id.etURL)
        etUrl.setText("https://d1pmarobgdhgjx.cloudfront.net/education/ED_pause-and-think-online.mp4")

        findViewById<Button>(R.id.btnOk).setOnClickListener(View.OnClickListener {
            createPlayer(etUrl.text.toString())
        })
    }







    /**
     * Creates MediaPlayer and plays video
     * @param media
     */
    fun createPlayer(media: String) {
        if(mediaPlayer!=null && libvlc!=null){
            releasePlayer()
        }
        Log.i(TAG, "Creating vlc player")
        Log.i(TAG, media)
        try {
            // create arraylist to assign option to create libvlc object
            val options = ArrayList<String>()
            options.add("--aout=opensles")
            options.add("--http-reconnect")
            options.add("--audio-time-stretch") // time stretching
            options.add("--network-caching=1500")
            options.add("-vvv") // verbosity

            // create libvlc object
            libvlc = LibVLC(this, options)

            // get surface view holder to display video
            this.holder=mSurface!!.holder
            holder!!.setKeepScreenOn(true)

            // Creating media player
            mediaPlayer = MediaPlayer(libvlc)

            // Setting up video output
            val vout = mediaPlayer!!.vlcVout
            vout.setVideoView(mSurface)
            vout.addCallback(this)
            vout.attachViews()
            val m = Media(libvlc, Uri.parse(media))
            mediaPlayer!!.setMedia(m)
            mediaPlayer!!.play()

        } catch (e: Exception) {
            Toast.makeText(this, "Error in creating player!", Toast.LENGTH_LONG).show()
        }

    }
    /**
     * release player
     */
    fun releasePlayer() {
        Log.i(TAG,"releasing player started")
        if (libvlc == null)
            return
        mediaPlayer!!.stop()
        var vout: IVLCVout = mediaPlayer!!.vlcVout
        vout.removeCallback(this)
        vout.detachViews()
        mediaPlayer!!.release()
        mediaPlayer=null
        holder = null
        libvlc!!.release()
        libvlc = null
        Log.i(TAG,"released player")
    }

    override fun onEvent(event: MediaPlayer.Event) {
        when (event.type) {
            MediaPlayer.Event.EndReached -> {
                this.releasePlayer()
            }
            else->Log.i(TAG,"nothing")
        }
    }


    override fun onSurfacesCreated(vlcVout: IVLCVout?) {
        val sw = mSurface!!.width
        val sh = mSurface!!.height

        if (sw * sh == 0) {
            Log.e(TAG, "Invalid surface size")
            return
        }

        mediaPlayer!!.vlcVout.setWindowSize(sw, sh)
        mediaPlayer!!.aspectRatio="4:3"
        mediaPlayer!!.setScale(0f)
    }

    override fun onSurfacesDestroyed(vlcVout: IVLCVout?) {
        releasePlayer()
    }

}
