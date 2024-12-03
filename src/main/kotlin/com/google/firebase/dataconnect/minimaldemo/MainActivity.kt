package com.google.firebase.dataconnect.minimaldemo

import android.os.Bundle
import android.view.View.OnClickListener
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.dataconnect.minimaldemo.MainActivityViewModel.State.OperationState
import com.google.firebase.dataconnect.minimaldemo.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  private lateinit var myApplication: MyApplication
  private lateinit var viewBinding: ActivityMainBinding
  private val viewModel: MainActivityViewModel by viewModels { MainActivityViewModel.Factory }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    myApplication = application as MyApplication

    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    viewBinding.insertItemButton.setOnClickListener(insertButtonOnClickListener)
    viewBinding.getItemButton.setOnClickListener(getItemButtonOnClickListener)
    viewBinding.useEmulatorCheckBox.setOnCheckedChangeListener(useEmulatorOnCheckedChangeListener)
    viewBinding.debugLoggingCheckBox.setOnCheckedChangeListener(debugLoggingOnCheckedChangeListener)

    lifecycleScope.launch {
      viewModel.state.flowWithLifecycle(lifecycle).collectLatest(::collectViewModelState)
    }
  }

  override fun onResume() {
    super.onResume()
    lifecycleScope.launch {
      viewBinding.useEmulatorCheckBox.isChecked = myApplication.getUseDataConnectEmulator()
      viewBinding.debugLoggingCheckBox.isChecked = myApplication.getDataConnectDebugLoggingEnabled()
    }
  }

  private fun collectViewModelState(state: MainActivityViewModel.State) {
    val (insertProgressText, insertSequenceNumber) =
      when (state.insertItem) {
        is OperationState.New -> Pair(null, null)
        is OperationState.InProgress ->
          Pair("Inserting an item...", state.insertItem.sequenceNumber)
        is OperationState.Completed ->
          Pair(
            state.insertItem.result.fold(
              onSuccess = { "Item inserted with ID: ${it.id}" },
              onFailure = { "Inserting item FAILED: $it" },
            ),
            state.insertItem.sequenceNumber,
          )
      }

    val (getProgressText, getSequenceNumber) =
      when (state.getItem) {
        is OperationState.New -> Pair(null, null)
        is OperationState.InProgress ->
          Pair(
            "Retrieving item with ID ${state.getItem.variables.id}...",
            state.getItem.sequenceNumber,
          )
        is OperationState.Completed ->
          Pair(
            state.getItem.result.fold(
              onSuccess = { "Retrieved item with ID ${state.getItem.variables.id}: $it" },
              onFailure = { "Retrieving item with ID ${state.getItem.variables.id} FAILED: $it" },
            ),
            state.getItem.sequenceNumber,
          )
      }

    viewBinding.insertItemButton.isEnabled = state.insertItem !is OperationState.InProgress
    viewBinding.getItemButton.isEnabled = state.getItem !is OperationState.InProgress

    viewBinding.progressText.text =
      if (
        insertSequenceNumber !== null &&
          (getSequenceNumber === null || insertSequenceNumber > getSequenceNumber)
      ) {
        insertProgressText
      } else {
        getProgressText
      }
  }

  private val insertButtonOnClickListener = OnClickListener { viewModel.insertItem() }

  private val getItemButtonOnClickListener = OnClickListener {}

  private val debugLoggingOnCheckedChangeListener = OnCheckedChangeListener { _, isChecked ->
    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
      return@OnCheckedChangeListener
    }
    myApplication.coroutineScope.launch {
      myApplication.setDataConnectDebugLoggingEnabled(isChecked)
    }
  }

  private val useEmulatorOnCheckedChangeListener = OnCheckedChangeListener { _, isChecked ->
    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
      return@OnCheckedChangeListener
    }
    myApplication.coroutineScope.launch { myApplication.setUseDataConnectEmulator(isChecked) }
  }
}
