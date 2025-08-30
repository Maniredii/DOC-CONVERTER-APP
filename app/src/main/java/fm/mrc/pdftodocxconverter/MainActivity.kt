package fm.mrc.pdftodocxconverter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import fm.mrc.pdftodocxconverter.databinding.ActivityMainBinding
import fm.mrc.pdftodocxconverter.converter.ConversionType

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var currentConversionType: ConversionType = ConversionType.PDF_TO_DOCX
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showFilePicker()
        } else {
            showPermissionDeniedDialog()
        }
    }
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleFileSelection(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkPermissions()
    }
    
    private fun setupUI() {
        binding.apply {
            // PDF to DOCX conversion
            btnPdfToDocx.setOnClickListener {
                if (checkPermissions()) {
                    currentConversionType = ConversionType.PDF_TO_DOCX
                    showFilePicker()
                }
            }
            
            // DOCX to PDF conversion
            btnDocxToPdf.setOnClickListener {
                if (checkPermissions()) {
                    currentConversionType = ConversionType.DOCX_TO_PDF
                    showFilePicker()
                }
            }
            
            // Any format to PDF
            btnAnyToPdf.setOnClickListener {
                if (checkPermissions()) {
                    currentConversionType = ConversionType.ANY_TO_PDF
                    showFilePicker()
                }
            }
            
            // Recent conversions
            btnRecentConversions.setOnClickListener {
                // TODO: Navigate to recent conversions
                showSnackbar(getString(R.string.recent_conversions_coming_soon))
            }
            
            // Settings
            btnSettings.setOnClickListener {
                // TODO: Navigate to settings
                showSnackbar(getString(R.string.settings_coming_soon))
            }
        }
    }
    
    private fun checkPermissions(): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                true
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                showPermissionRationaleDialog()
                false
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                false
            }
        }
    }
    
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.permission_message))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_denied))
            .setMessage(getString(R.string.permission_denied_message))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showFilePicker() {
        filePickerLauncher.launch("*/*")
    }
    
    private fun handleFileSelection(uri: Uri) {
        // TODO: Process the selected file based on the conversion type
        showSnackbar("File selected: ${uri.lastPathSegment}")
        
        // Navigate to conversion activity
        val intent = Intent(this, ConversionActivity::class.java).apply {
            putExtra("file_uri", uri.toString())
            putExtra("conversion_type", currentConversionType.name)
        }
        startActivity(intent)
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
