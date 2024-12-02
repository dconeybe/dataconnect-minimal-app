package com.google.firebase.dataconnect.minimaldemo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.dataconnect.minimaldemo.connector.Zwda6x9zyyKey
import com.google.firebase.dataconnect.minimaldemo.connector.execute
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel(private val app: MyApplication) : ViewModel() {

  private val _insertJob = MutableStateFlow<InsertJobState>(InsertJobState.New)
  val insertJob: StateFlow<InsertJobState> = _insertJob.asStateFlow()

  fun insertItem() {
    while (true) {
      val oldState =
        when (val oldState = _insertJob.value) {
          is InsertJobState.Active -> break
          is InsertJobState.New,
          is InsertJobState.Completed -> oldState
        }

      val newState =
        InsertJobState.Active(
          viewModelScope.async(start = CoroutineStart.LAZY) {
            app.getConnector().insertItem.execute {}.data.key
          }
        )

      if (_insertJob.compareAndSet(oldState, newState)) {
        newState.start()
        break
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun InsertJobState.Active.start() {
    job.start()

    job.invokeOnCompletion { exception ->
      val result =
        if (exception !== null) {
          Log.w(TAG, "insert item failed: $exception", exception)
          Result.failure(exception)
        } else {
          val insertedKey = job.getCompleted()
          Log.w(TAG, "successfully inserted item with key: $insertedKey")
          Result.success(insertedKey)
        }
      _insertJob.compareAndSet(this@start, InsertJobState.Completed(result))
    }
  }

  sealed interface InsertJobState {
    data object New : InsertJobState

    data class Active(val job: Deferred<Zwda6x9zyyKey>) : InsertJobState

    data class Completed(val key: Result<Zwda6x9zyyKey>) : InsertJobState
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { MainActivityViewModel(this[APPLICATION_KEY] as MyApplication) }
    }

    private const val TAG = "MainActivityViewModel"
  }
}
