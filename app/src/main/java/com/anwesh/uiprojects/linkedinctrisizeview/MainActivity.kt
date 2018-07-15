package com.anwesh.uiprojects.linkedinctrisizeview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.litsview.LITSView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view : LITSView = LITSView.create(this)
        view.addOnCompleteListener {
            Toast.makeText(this, "${it} animation completed", Toast.LENGTH_SHORT).show()
        }
        fullScreen()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}
