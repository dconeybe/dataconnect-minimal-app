package com.google.firebase.dataconnect.minimaldemo

import android.os.Bundle
import android.view.View.OnClickListener
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.dataconnect.minimaldemo.MainActivityViewModel.InsertJobState
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
    viewBinding.useEmulatorCheckBox.setOnCheckedChangeListener(useEmulatorOnCheckedChangeListener)
    viewBinding.debugLoggingCheckBox.setOnCheckedChangeListener(debugLoggingOnCheckedChangeListener)

    lifecycleScope.launch {
      viewModel.insertJob.flowWithLifecycle(lifecycle).collectLatest(::collectInsertJob)
    }
  }

  override fun onResume() {
    super.onResume()
    lifecycleScope.launch {
      viewBinding.useEmulatorCheckBox.isChecked = myApplication.getUseDataConnectEmulator()
      viewBinding.debugLoggingCheckBox.isChecked = myApplication.getDataConnectDebugLoggingEnabled()
    }
  }

  private fun collectInsertJob(insertJobState: InsertJobState) {
    data class InsertUiInfo(val buttonEnabled: Boolean, val progressText: String)

    val uiInfo =
      when (insertJobState) {
        is InsertJobState.New ->
          InsertUiInfo(
            buttonEnabled = true,
            progressText = "Click \"${viewBinding.insertItemButton.text}\" to insert an item.",
          )

        is InsertJobState.Active ->
          InsertUiInfo(buttonEnabled = false, progressText = "Inserting an item...")

        is InsertJobState.Completed ->
          InsertUiInfo(
            buttonEnabled = true,
            progressText =
              insertJobState.key.fold(
                onSuccess = { "Item inserted with ID: ${it.id}" },
                onFailure = { "Inserting item FAILED: $it" },
              ),
          )
      }

    viewBinding.insertItemButton.isEnabled = uiInfo.buttonEnabled
    viewBinding.insertProgressText.text = uiInfo.progressText
  }

  private val insertButtonOnClickListener = OnClickListener { viewModel.insertItem() }

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
