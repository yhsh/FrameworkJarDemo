package com.example.frameworkdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //调用framework.jar中的方法
        val volumeDetector = VolumeDetector(StorageManager.from(this))
        volumeDetector.startDetect()
    }
}