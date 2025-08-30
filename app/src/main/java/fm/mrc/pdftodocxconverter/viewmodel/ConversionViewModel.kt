package fm.mrc.pdftodocxconverter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fm.mrc.pdftodocxconverter.converter.ConversionType
import fm.mrc.pdftodocxconverter.converter.DocumentConverter
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConversionViewModel : ViewModel() {
    
    private val _conversionStatus = MutableLiveData<ConversionStatus>(ConversionStatus.Idle)
    val conversionStatus: LiveData<ConversionStatus> = _conversionStatus
    
    private val _conversionProgress = MutableLiveData<Int>(0)
    val conversionProgress: LiveData<Int> = _conversionProgress
    
    private var inputFile: Uri? = null
    private var conversionType: ConversionType? = null
    private var conversionJob: Job? = null
    private var documentConverter: DocumentConverter? = null
    
    fun setInputFile(uri: Uri) {
        inputFile = uri
    }
    
    fun setConversionType(type: ConversionType) {
        conversionType = type
    }
    
    fun setContext(context: Context) {
        documentConverter = DocumentConverter(context)
    }
    
    fun startConversion() {
        if (inputFile == null || conversionType == null || documentConverter == null) {
            _conversionStatus.value = ConversionStatus.Error("Missing input file, conversion type, or context")
            return
        }
        
        conversionJob?.cancel()
        conversionJob = viewModelScope.launch {
            try {
                _conversionStatus.value = ConversionStatus.Loading
                _conversionProgress.value = 0
                
                // Simulate progress
                simulateProgress()
                
                // Perform conversion
                val outputPath = documentConverter!!.convert(inputFile!!, conversionType!!)
                
                _conversionProgress.value = 100
                _conversionStatus.value = ConversionStatus.Success(outputPath)
                
            } catch (e: Exception) {
                _conversionStatus.value = ConversionStatus.Error(e.message ?: "Unknown error occurred")
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
            delay(100)
            _conversionProgress.value = i
        }
    }
}

sealed class ConversionStatus {
    object Idle : ConversionStatus()
    object Loading : ConversionStatus()
    data class Success(val outputPath: String) : ConversionStatus()
    data class Error(val message: String) : ConversionStatus()
}
