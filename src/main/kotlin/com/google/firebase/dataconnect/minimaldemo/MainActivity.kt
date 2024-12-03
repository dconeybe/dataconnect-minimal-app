package com.google.firebase.dataconnect.minimaldemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dataconnect.minimaldemo.connector.Ctry3q3tp6kzxConnector
import com.google.firebase.dataconnect.minimaldemo.connector.instance

private val connector =
  Ctry3q3tp6kzxConnector.instance.apply {
    // Connect to the emulator on "10.0.2.2:9399" (default port)
    dataConnect.useEmulator()

    // (alternatively) if you're running your emulator on non-default port:
    dataConnect.useEmulator(port = 9999)
  }

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  }
}
