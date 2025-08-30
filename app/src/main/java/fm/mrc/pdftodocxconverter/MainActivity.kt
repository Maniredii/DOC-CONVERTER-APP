package fm.mrc.pdftodocxconverter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import fm.mrc.pdftodocxconverter.converter.ConversionType
import fm.mrc.pdftodocxconverter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var currentConversionType: ConversionType = ConversionType.PDF_TO_DOCX
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
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
    }
    
    override fun onResume() {
        super.onResume()
        // Check if we returned from settings with permissions granted
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R && 
            android.os.Environment.isExternalStorageManager()) {
            // Permission granted, we can proceed
        }
    }
    
    private fun setupUI() {
        binding.apply {
            btnPdfToDocx.setOnClickListener {
                currentConversionType = ConversionType.PDF_TO_DOCX
                if (checkPermissions()) {
                    showFilePicker()
                }
            }
            
            btnDocxToPdf.setOnClickListener {
                currentConversionType = ConversionType.DOCX_TO_PDF
                if (checkPermissions()) {
                    showFilePicker()
                }
            }
            
            btnAnyToPdf.setOnClickListener {
                currentConversionType = ConversionType.ANY_TO_PDF
                if (checkPermissions()) {
                    showFilePicker()
                }
            }
            
            btnRecentConversions.setOnClickListener {
                showSnackbar("Recent conversions feature coming soon!")
            }
            
            btnSettings.setOnClickListener {
                showSnackbar("Settings feature coming soon!")
            }
        }
    }
    
    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            
            if (permissionsToRequest.isEmpty()) {
                true
            } else {
                requestPermissions(permissionsToRequest.toTypedArray())
                false
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            // Check if we need MANAGE_EXTERNAL_STORAGE for broader access and writing to Downloads
            if (!android.os.Environment.isExternalStorageManager()) {
                showManageExternalStorageDialog()
                return false
            }
            true // No runtime permissions needed for file access
        } else {
            // Android 10 and below (API 29-)
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            
            if (permissionsToRequest.isEmpty()) {
                true
            } else {
                if (shouldShowRequestPermissionRationale(permissionsToRequest.first())) {
                    showPermissionRationaleDialog()
                } else {
                    requestPermissions(permissionsToRequest.toTypedArray())
                }
                false
            }
        }
    }
    
    private fun requestPermissions(permissions: Array<String>) {
        requestPermissionLauncher.launch(permissions)
    }
    
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_rationale_title))
            .setMessage(getString(R.string.permission_rationale_message))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissions = arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                    requestPermissions(permissions)
                } else {
                    val permissions = arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    requestPermissions(permissions)
                }
            }
            .setNegativeButton(getString(R.string.cancel_permission), null)
            .show()
    }
    
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_denied_title))
            .setMessage(getString(R.string.permission_denied_message))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(getString(R.string.cancel_permission), null)
            .show()
    }
    
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
    
    private fun showManageExternalStorageDialog() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("This app needs access to all files to convert documents. Please grant 'All files access' permission.")
            .setPositiveButton("Grant Permission") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general storage settings
                    val fallbackIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(fallbackIntent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showFilePicker() {
        val mimeType = when (currentConversionType) {
            ConversionType.PDF_TO_DOCX -> "application/pdf"
            ConversionType.DOCX_TO_PDF -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ConversionType.ANY_TO_PDF -> "*/*"
        }
        filePickerLauncher.launch(mimeType)
    }
    
    private fun handleFileSelection(uri: Uri) {
        showSnackbar(getString(R.string.file_selected, uri.lastPathSegment ?: "Unknown file"))
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
