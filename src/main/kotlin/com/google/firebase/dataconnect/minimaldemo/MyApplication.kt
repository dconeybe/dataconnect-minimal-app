package com.google.firebase.dataconnect.minimaldemo

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.dataconnect.FirebaseDataConnect
import com.google.firebase.dataconnect.LogLevel
import com.google.firebase.dataconnect.logLevel
import com.google.firebase.dataconnect.minimaldemo.connector.Ctry3q3tp6kzxConnector
import com.google.firebase.dataconnect.minimaldemo.connector.instance
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class MyApplication : Application() {

  /**
   * A [CoroutineScope] whose lifetime matches that of this [Application] object.
   *
   * Namely, the scope will be cancelled when [onTerminate] is called.
   *
   * This scope's [Job] is a [SupervisorJob], and, therefore, uncaught exceptions will _not_
   * terminate the application.
   */
  val coroutineScope =
    CoroutineScope(
      SupervisorJob() +
        CoroutineName("MyApplication@${System.identityHashCode(this@MyApplication)}") +
        CoroutineExceptionHandler { context, throwable ->
          val coroutineName = context[CoroutineName]?.name
          Log.w(
            TAG,
            "WARNING: ignoring uncaught exception thrown from coroutine " +
              "named \"$coroutineName\": $throwable " +
              "(error code 8xrn9vvddd)",
            throwable,
          )
        }
    )

  private val connectorMutex = Mutex()
  private var connector: Ctry3q3tp6kzxConnector? = null

  suspend fun getConnector(): Ctry3q3tp6kzxConnector {
    connectorMutex.withLock {
      val oldConnector = connector
      if (oldConnector !== null) {
        return oldConnector
      }

      val newConnector = Ctry3q3tp6kzxConnector.instance

      newConnector.dataConnect.useEmulator()

      isDataConnectDebugLoggingEnabled()?.let { isEnabled ->
        FirebaseDataConnect.logLevel = if (isEnabled) LogLevel.DEBUG else LogLevel.WARN
      }

      connector = newConnector
      return newConnector
    }
  }

  private suspend fun getSharedPreferences(): SharedPreferences =
    withContext(Dispatchers.IO) {
      getSharedPreferences("MyApplicationSharedPreferences", MODE_PRIVATE)
    }

  suspend fun isDataConnectDebugLoggingEnabled(): Boolean? =
    getSharedPreferences().all[SharedPrefsKeys.IS_DATA_CONNECT_LOGGING_ENABLED] as? Boolean

  suspend fun setDataConnectDebugLoggingEnabled(enabled: Boolean) {
    val prefs = getSharedPreferences()
    withContext(Dispatchers.IO) {
      val editor = prefs.edit()
      editor.putBoolean(SharedPrefsKeys.IS_DATA_CONNECT_LOGGING_ENABLED, enabled)
      if (!editor.commit()) {
        Log.w(
          TAG,
          "WARNING: setDataConnectDebugLoggingEnabled() failed to save " +
            "to SharedPreferences; ignoring the failure (error code wzy99s7jmy)",
        )
      }
    }
  }

  override fun onTerminate() {
    coroutineScope.cancel("MyApplication.onTerminate() called")
    super.onTerminate()
  }

  private object SharedPrefsKeys {
    const val IS_DATA_CONNECT_LOGGING_ENABLED = "isDataConnectDebugLoggingEnabled"
  }

  companion object {
    private const val TAG = "MyApplication"
  }
}
