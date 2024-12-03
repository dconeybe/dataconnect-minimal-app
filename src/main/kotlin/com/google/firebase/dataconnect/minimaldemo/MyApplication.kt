package com.google.firebase.dataconnect.minimaldemo

import android.app.Application
import com.google.firebase.dataconnect.minimaldemo.connector.Ctry3q3tp6kzxConnector
import com.google.firebase.dataconnect.minimaldemo.connector.instance

class MyApplication : Application() {

  val connector: Ctry3q3tp6kzxConnector by
    lazy(LazyThreadSafetyMode.PUBLICATION) {
      Ctry3q3tp6kzxConnector.instance.apply { dataConnect.useEmulator() }
    }
}
