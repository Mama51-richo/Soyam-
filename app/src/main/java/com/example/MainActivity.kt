package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soyamgebeya.ui.SoyamApp
import com.example.soyamgebeya.viewmodel.SoyamViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: SoyamViewModel = viewModel()
      SoyamApp(viewModel = viewModel)
    }
  }
}
