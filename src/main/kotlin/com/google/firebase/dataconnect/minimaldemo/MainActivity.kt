package com.google.firebase.dataconnect.minimaldemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.dataconnect.minimaldemo.MainActivityViewModel.InsertJobState
import com.google.firebase.dataconnect.minimaldemo.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  private lateinit var viewBinding: ActivityMainBinding
  private val viewModel: MainActivityViewModel by viewModels { MainActivityViewModel.Factory }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    viewBinding.insertItemButton.setOnClickListener { viewModel.insertItem() }

    lifecycleScope.launch {
      viewModel.insertJob.flowWithLifecycle(lifecycle).collectLatest(::collectInsertJob)
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
}
