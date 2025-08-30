package fm.mrc.pdftodocxconverter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import fm.mrc.pdftodocxconverter.databinding.ActivityConversionBinding
import fm.mrc.pdftodocxconverter.viewmodel.ConversionViewModel
import fm.mrc.pdftodocxconverter.converter.ConversionType

class ConversionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConversionBinding
    private lateinit var viewModel: ConversionViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupUI()
        handleIntent()
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ConversionViewModel::class.java]
        
        // Observe conversion progress
        viewModel.conversionProgress.observe(this) { progress ->
            binding.progressBar.progress = progress
            binding.tvProgress.text = "$progress%"
        }
        
        // Observe conversion status
        viewModel.conversionStatus.observe(this) { status ->
            when (status) {
                is ConversionStatus.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvStatus.text = getString(R.string.converting)
                    binding.btnConvert.isEnabled = false
                }
                is ConversionStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvStatus.text = getString(R.string.conversion_completed)
                    binding.btnConvert.text = getString(R.string.open_file)
                    binding.btnConvert.isEnabled = true
                    binding.btnConvert.setOnClickListener {
                        openConvertedFile(status.fileUri)
                    }
                }
                is ConversionStatus.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvStatus.text = getString(R.string.conversion_error)
                    binding.btnConvert.isEnabled = true
                    binding.btnConvert.setOnClickListener {
                        startConversion()
                    }
                }
                is ConversionStatus.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvStatus.text = getString(R.string.ready_to_convert)
                    binding.btnConvert.isEnabled = true
                }
            }
        }
    }
    
    private fun setupUI() {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            
            btnConvert.setOnClickListener {
                startConversion()
            }
            
            btnCancel.setOnClickListener {
                viewModel.cancelConversion()
                finish()
            }
        }
    }
    
    private fun handleIntent() {
        intent.getStringExtra("file_uri")?.let { uriString ->
            val uri = Uri.parse(uriString)
            viewModel.setInputFile(uri)
            binding.tvFileName.text = uri.lastPathSegment ?: "Unknown file"
        }
        
        // Handle conversion type from intent
        intent.getStringExtra("conversion_type")?.let { typeString ->
            val type = ConversionType.fromString(typeString)
            if (type != null) {
                viewModel.setConversionType(type)
                binding.tvConversionType.text = type.displayName
            }
        }
    }
    
    private fun startConversion() {
        viewModel.startConversion()
    }
    
    private fun openConvertedFile(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(uri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            showSnackbar(getString(R.string.file_open_error, e.message))
        }
    }
    
    private fun getMimeType(uri: Uri): String {
        return when (uri.toString().lowercase()) {
            in listOf(".pdf") -> "application/pdf"
            in listOf(".docx", ".doc") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "*/*"
        }
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}

sealed class ConversionStatus {
    object Idle : ConversionStatus()
    object Loading : ConversionStatus()
    data class Success(val fileUri: Uri) : ConversionStatus()
    data class Error(val message: String) : ConversionStatus()
}
