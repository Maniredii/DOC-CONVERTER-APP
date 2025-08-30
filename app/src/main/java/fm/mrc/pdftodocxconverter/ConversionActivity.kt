package fm.mrc.pdftodocxconverter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import fm.mrc.pdftodocxconverter.converter.ConversionType
import fm.mrc.pdftodocxconverter.databinding.ActivityConversionBinding
import fm.mrc.pdftodocxconverter.viewmodel.ConversionViewModel
import fm.mrc.pdftodocxconverter.viewmodel.ConversionStatus
import java.io.File

class ConversionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConversionBinding
    private lateinit var viewModel: ConversionViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[ConversionViewModel::class.java]
        viewModel.setContext(this)
        
        setupUI()
        handleIntent()
        observeViewModel()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        binding.btnStartConversion.setOnClickListener {
            viewModel.startConversion()
        }
        
        binding.btnCancel.setOnClickListener {
            viewModel.cancelConversion()
            finish()
        }
        
        binding.btnViewFile.setOnClickListener {
            // Get the current output path from the ViewModel
            viewModel.getCurrentOutputPath()?.let { outputPath ->
                openConvertedFile(outputPath)
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
    
    private fun observeViewModel() {
        viewModel.conversionProgress.observe(this) { progress ->
            binding.progressBar.progress = progress
            binding.tvProgress.text = getString(R.string.progress_percentage, progress)
        }
        
        viewModel.conversionStatus.observe(this) { status ->
            when (status) {
                is ConversionStatus.Idle -> {
                    binding.tvStatus.text = getString(R.string.status_idle)
                    binding.btnStartConversion.isEnabled = true
                    binding.btnCancel.isEnabled = false
                    binding.btnViewFile.visibility = android.view.View.GONE
                }
                is ConversionStatus.Loading -> {
                    binding.tvStatus.text = getString(R.string.status_converting)
                    binding.btnStartConversion.isEnabled = false
                    binding.btnCancel.isEnabled = true
                    binding.btnViewFile.visibility = android.view.View.GONE
                }
                is ConversionStatus.Success -> {
                    binding.tvStatus.text = getString(R.string.file_converted_successfully)
                    binding.btnStartConversion.isEnabled = false
                    binding.btnCancel.isEnabled = false
                    binding.btnViewFile.visibility = android.view.View.VISIBLE
                    
                    // Show additional info about the converted file
                    val fileName = status.outputPath.substringAfterLast("/").substringAfterLast("\\")
                    binding.tvStatus.text = getString(R.string.converted_file_path, fileName)
                }
                is ConversionStatus.Error -> {
                    binding.tvStatus.text = getString(R.string.status_error, status.message)
                    binding.btnStartConversion.isEnabled = true
                    binding.btnCancel.isEnabled = false
                    binding.btnViewFile.visibility = android.view.View.GONE
                }
            }
        }
    }
    
    private fun openConvertedFile(outputPath: String) {
        val file = File(outputPath)
        if (file.exists()) {
            val uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(outputPath))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // If no app can handle the file, show a message
                binding.tvStatus.text = getString(R.string.status_file_ready, outputPath)
            }
        }
    }
    
    private fun getMimeType(filePath: String): String {
        return when {
            filePath.endsWith(".pdf") -> "application/pdf"
            filePath.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            filePath.endsWith(".txt") -> "text/plain"
            filePath.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "*/*"
        }
    }
}
