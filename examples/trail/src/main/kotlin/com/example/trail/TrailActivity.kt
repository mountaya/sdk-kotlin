package com.example.trail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/** TrailActivity is Trail's one screen: it reads the settings once and hands them to the app's root. */
class TrailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settings = TrailSettings.read(resources)
        setContent { Trail(settings) }
    }
}
