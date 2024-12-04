package com.google.firebase.dataconnect.minimaldemo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.dataconnect.minimaldemo.connector.GetItemByKeyQuery
import com.google.firebase.dataconnect.minimaldemo.connector.InsertItemMutation
import com.google.firebase.dataconnect.minimaldemo.connector.Zwda6x9zyyKey
import com.google.firebase.dataconnect.minimaldemo.connector.execute
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.next
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel(private val app: MyApplication) : ViewModel() {

  private val _state = MutableStateFlow(State())
  val state: StateFlow<State> = _state.asStateFlow()

  private val rs = RandomSource.default()

  fun insertItem() {
    val arb = Arb.insertItemVariables()
    val variables = if (rs.random.nextFloat() < 0.333f) arb.edgecase(rs)!! else arb.next(rs)

    while (true) {
      val oldState = _state.value
      when (oldState.insertItem) {
        is State.OperationState.InProgress -> return
        is State.OperationState.New,
        is State.OperationState.Completed -> Unit
      }

      val job: Deferred<Zwda6x9zyyKey> =
        viewModelScope.async(start = CoroutineStart.LAZY) {
          app.getConnector().insertItem.ref(variables).execute().data.key
        }
      val inProgressState = State.OperationState.InProgress(nextSequenceNumber(), variables, job)
      val newState = oldState.copy(insertItem = inProgressState)

      if (_state.compareAndSet(oldState, newState)) {
        newState.startInsert(inProgressState)
        break
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun State.startInsert(
    inProgressState: State.OperationState.InProgress<InsertItemMutation.Variables, Zwda6x9zyyKey>
  ) {
    require(inProgressState === insertItem)
    val job = inProgressState.job

    job.start()

    job.invokeOnCompletion { exception ->
      val result =
        if (exception !== null) {
          Log.w(TAG, "insert item failed: $exception", exception)
          Result.failure(exception)
        } else {
          val insertedKey = job.getCompleted()
          Log.i(TAG, "successfully inserted item with key: $insertedKey")
          Result.success(insertedKey)
        }

      while (true) {
        val oldState = _state.value
        if (oldState.insertItem !== inProgressState) {
          break
        }
        val newState =
          oldState.copy(
            insertItem =
              State.OperationState.Completed(
                nextSequenceNumber(),
                inProgressState.variables,
                result,
              ),
            lastInsertedKey = result.getOrNull() ?: oldState.lastInsertedKey,
          )
        if (_state.compareAndSet(oldState, newState)) {
          break
        }
      }
    }
  }

  fun getItem() {
    while (true) {
      val oldState = _state.value
      if (oldState.lastInsertedKey === null) {
        return
      }
      when (oldState.getItem) {
        is State.OperationState.InProgress -> return
        is State.OperationState.New,
        is State.OperationState.Completed -> Unit
      }

      val job: Deferred<GetItemByKeyQuery.Data.Item?> =
        viewModelScope.async(start = CoroutineStart.LAZY) {
          app.getConnector().getItemByKey.execute(oldState.lastInsertedKey).data.item
        }
      val inProgressState =
        State.OperationState.InProgress(nextSequenceNumber(), oldState.lastInsertedKey, job)
      val newState = oldState.copy(getItem = inProgressState)

      if (_state.compareAndSet(oldState, newState)) {
        newState.startGet(inProgressState)
        break
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun State.startGet(
    inProgressState: State.OperationState.InProgress<Zwda6x9zyyKey, GetItemByKeyQuery.Data.Item?>
  ) {
    require(inProgressState === getItem)
    val job = inProgressState.job

    job.start()

    job.invokeOnCompletion { exception ->
      val result =
        if (exception !== null) {
          Log.w(
            TAG,
            "get item with ID ${inProgressState.variables.id} failed: $exception",
            exception,
          )
          Result.failure(exception)
        } else {
          val retrievedItem = job.getCompleted()
          Log.i(TAG, "successfully retrieved item with ID ${inProgressState.variables.id}")
          Result.success(retrievedItem)
        }

      while (true) {
        val oldState = _state.value
        if (oldState.getItem !== inProgressState) {
          break
        }
        val newState =
          oldState.copy(
            getItem =
              State.OperationState.Completed(
                nextSequenceNumber(),
                inProgressState.variables,
                result,
              )
          )
        if (_state.compareAndSet(oldState, newState)) {
          break
        }
      }
    }
  }

  data class State(
    val insertItem: OperationState<InsertItemMutation.Variables, Zwda6x9zyyKey> =
      OperationState.New,
    val getItem: OperationState<Zwda6x9zyyKey, GetItemByKeyQuery.Data.Item?> = OperationState.New,
    val lastInsertedKey: Zwda6x9zyyKey? = null,
  ) {
    sealed interface OperationState<out Variables, out Data> {
      sealed interface SequencedOperationState<out Variables, out Data> :
        OperationState<Variables, Data> {
        val sequenceNumber: Long
      }

      data object New : OperationState<Nothing, Nothing>

      data class InProgress<out Variables, out Data>(
        override val sequenceNumber: Long,
        val variables: Variables,
        val job: Deferred<Data>,
      ) : SequencedOperationState<Variables, Data>

      data class Completed<out Variables, out Data>(
        override val sequenceNumber: Long,
        val variables: Variables,
        val result: Result<Data>,
      ) : SequencedOperationState<Variables, Data>
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { MainActivityViewModel(this[APPLICATION_KEY] as MyApplication) }
    }

    private const val TAG = "MainActivityViewModel"

    private val nextSequenceNumber = AtomicLong(0)

    private fun nextSequenceNumber(): Long = nextSequenceNumber.incrementAndGet()
  }
}
