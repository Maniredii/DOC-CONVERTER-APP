package fm.mrc.pdftodocxconverter.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fm.mrc.pdftodocxconverter.ConversionStatus
import fm.mrc.pdftodocxconverter.converter.DocumentConverter
import fm.mrc.pdftodocxconverter.converter.ConversionType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class ConversionViewModel : ViewModel() {
    
    private val _conversionStatus = MutableLiveData<ConversionStatus>(ConversionStatus.Idle)
    val conversionStatus: LiveData<ConversionStatus> = _conversionStatus
    
    private val _conversionProgress = MutableLiveData<Int>(0)
    val conversionProgress: LiveData<Int> = _conversionProgress
    
    private var inputFileUri: Uri? = null
    private var conversionType: ConversionType = ConversionType.PDF_TO_DOCX
    private var conversionJob: Job? = null
    
    fun setInputFile(uri: Uri) {
        inputFileUri = uri
        _conversionStatus.value = ConversionStatus.Idle
        _conversionProgress.value = 0
    }
    
    fun setConversionType(type: ConversionType) {
        conversionType = type
    }
    
    fun startConversion() {
        val uri = inputFileUri ?: run {
            _conversionStatus.value = ConversionStatus.Error("No input file selected")
            return
        }
        
        if (conversionJob?.isActive == true) {
            return
        }
        
        _conversionStatus.value = ConversionStatus.Loading
        _conversionProgress.value = 0
        
        conversionJob = viewModelScope.launch {
            try {
                // Simulate conversion progress
                simulateProgress()
                
                // Perform actual conversion
                val converter = DocumentConverter()
                val outputUri = converter.convert(uri, conversionType)
                
                _conversionStatus.value = ConversionStatus.Success(outputUri)
                _conversionProgress.value = 100
                
            } catch (e: Exception) {
                _conversionStatus.value = ConversionStatus.Error(e.message ?: "Unknown error occurred")
                _conversionProgress.value = 0
            }
        }
    }
    
    fun cancelConversion() {
        conversionJob?.cancel()
        _conversionStatus.value = ConversionStatus.Idle
        _conversionProgress.value = 0
    }
    
    private suspend fun simulateProgress() {
        for (i in 0..100 step 5) {
            if (conversionJob?.isActive == true) {
                _conversionProgress.value = i
                delay(100) // Simulate processing time
            } else {
                break
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        conversionJob?.cancel()
    }
}
