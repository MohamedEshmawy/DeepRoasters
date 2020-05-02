package com.pedro.sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  // declare surface view object
  var mSurface: SurfaceView?=null

  private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
    Manifest.permission.WRITE_EXTERNAL_STORAGE)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    setContentView(R.layout.activity_main)
    b_camera_demo.setOnClickListener {
      if (!hasPermissions(this, *PERMISSIONS)) {
        ActivityCompat.requestPermissions(this, PERMISSIONS, 1)
      } else {
        // streaming window
        //CameraDemoActivity.mSurface  = findViewById(R.id.stream) as SurfaceView
        startActivity(Intent(this, CameraDemoActivity::class.java))
      }
    }
    // resending integration
    button_id.setOnClickListener {
      if (!hasPermissions(this, *PERMISSIONS)) {
        ActivityCompat.requestPermissions(this, PERMISSIONS, 1)
      } else {
        //var vlcplayer = Main2Activity()
        //mSurface = findViewById(R.id.resend) as SurfaceView
//        var etUrl= findViewById<EditText>(R.id.etURL)
//        etUrl.setText("https://d1pmarobgdhgjx.cloudfront.net/education/ED_pause-and-think-online.mp4")
        //var btn=  vlcplayer.findViewById(R.id.btnOk) as Button

        //vlcplayer!!.mSurface=mSurface

//      vlcplayer!!.createPlayer(etUrl.text.toString())
        //emmm
        startActivity(Intent(this, Main2Activity::class.java))
      }
    }
  }

  private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
      for (permission in permissions) {
        if (ActivityCompat.checkSelfPermission(context,
              permission) != PackageManager.PERMISSION_GRANTED) {
          return false
        }
      }
    }
    return true
  }
}