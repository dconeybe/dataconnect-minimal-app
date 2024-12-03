package com.google.firebase.dataconnect.minimaldemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dataconnect.minimaldemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var viewBinding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)
  }
}
