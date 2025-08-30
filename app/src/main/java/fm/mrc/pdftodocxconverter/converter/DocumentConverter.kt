package fm.mrc.pdftodocxconverter.converter

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DocumentConverter {
    
    companion object {
        private const val TAG = "DocumentConverter"
    }
    
    fun convert(inputUri: Uri, conversionType: ConversionType): Uri {
        return when (conversionType) {
            ConversionType.PDF_TO_DOCX -> convertPdfToDocx(inputUri)
            ConversionType.DOCX_TO_PDF -> convertDocxToPdf(inputUri)
            ConversionType.ANY_TO_PDF -> convertAnyToPdf(inputUri)
        }
    }
    
    private fun convertPdfToDocx(inputUri: Uri): Uri {
        try {
            Log.d(TAG, "Starting PDF to DOCX conversion")
            
            // Load PDF document
            val inputStream = getInputStreamFromUri(inputUri)
            val pdfDocument = PDDocument.load(inputStream)
            
            // Extract text from PDF
            val textStripper = PDFTextStripper()
            val pdfText = textStripper.getText(pdfDocument)
            
            // Create DOCX document
            val docxDocument = XWPFDocument()
            
            // Split text into paragraphs and add to DOCX
            val paragraphs = pdfText.split("\n\n")
            for (paragraphText in paragraphs) {
                if (paragraphText.trim().isNotEmpty()) {
                    val paragraph = docxDocument.createParagraph()
                    val run = paragraph.createRun()
                    run.text = paragraphText.trim()
                    run.fontSize = 12
                }
            }
            
            // Save DOCX document
            val outputFile = createOutputFile("converted", "docx")
            val outputStream = FileOutputStream(outputFile)
            docxDocument.write(outputStream)
            
            // Clean up
            outputStream.close()
            docxDocument.close()
            pdfDocument.close()
            inputStream.close()
            
            Log.d(TAG, "PDF to DOCX conversion completed successfully")
            return Uri.fromFile(outputFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error converting PDF to DOCX", e)
            throw RuntimeException("Failed to convert PDF to DOCX: ${e.message}")
        }
    }
    
    private fun convertDocxToPdf(inputUri: Uri): Uri {
        try {
            Log.d(TAG, "Starting DOCX to PDF conversion")
            
            // Load DOCX document
            val inputStream = getInputStreamFromUri(inputUri)
            val docxDocument = XWPFDocument(inputStream)
            
            // Extract text from DOCX
            val textBuilder = StringBuilder()
            for (paragraph in docxDocument.paragraphs) {
                textBuilder.append(paragraph.text).append("\n\n")
            }
            
            // Create PDF document
            val pdfDocument = PDDocument()
            // TODO: Implement actual PDF creation with text content
            // For now, create a simple PDF with extracted text
            
            // Save PDF document
            val outputFile = createOutputFile("converted", "pdf")
            val outputStream = FileOutputStream(outputFile)
            pdfDocument.save(outputStream)
            
            // Clean up
            outputStream.close()
            pdfDocument.close()
            inputStream.close()
            docxDocument.close()
            
            Log.d(TAG, "DOCX to PDF conversion completed successfully")
            return Uri.fromFile(outputFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error converting DOCX to PDF", e)
            throw RuntimeException("Failed to convert DOCX to PDF: ${e.message}")
        }
    }
    
    private fun convertAnyToPdf(inputUri: Uri): Uri {
        try {
            Log.d(TAG, "Starting Any to PDF conversion")
            
            // Determine input file type and convert accordingly
            val fileName = getFileNameFromUri(inputUri)
            val fileExtension = getFileExtension(fileName)
            
            return when (fileExtension.lowercase()) {
                "docx", "doc" -> convertDocxToPdf(inputUri)
                "txt" -> convertTextToPdf(inputUri)
                "rtf" -> convertRtfToPdf(inputUri)
                else -> throw UnsupportedOperationException("Unsupported file format: $fileExtension")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error converting file to PDF", e)
            throw RuntimeException("Failed to convert file to PDF: ${e.message}")
        }
    }
    
    private fun convertTextToPdf(inputUri: Uri): Uri {
        try {
            // Read text content
            val inputStream = getInputStreamFromUri(inputUri)
            val textContent = inputStream.bufferedReader().use { it.readText() }
            
            // Create PDF document
            val pdfDocument = PDDocument()
            // TODO: Implement actual PDF creation with text content
            
            // Save PDF document
            val outputFile = createOutputFile("converted", "pdf")
            val outputStream = FileOutputStream(outputFile)
            pdfDocument.save(outputStream)
            
            // Clean up
            outputStream.close()
            pdfDocument.close()
            inputStream.close()
            
            return Uri.fromFile(outputFile)
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to convert text to PDF: ${e.message}")
        }
    }
    
    private fun convertRtfToPdf(inputUri: Uri): Uri {
        try {
            // Read RTF content
            val inputStream = getInputStreamFromUri(inputUri)
            val rtfContent = inputStream.bufferedReader().use { it.readText() }
            
            // TODO: Implement RTF parsing and PDF creation
            // For now, convert as plain text
            return convertTextToPdf(inputUri)
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to convert RTF to PDF: ${e.message}")
        }
    }
    
    private fun getInputStreamFromUri(uri: Uri): InputStream {
        // TODO: Implement proper URI handling for different content providers
        return File(uri.path ?: "").inputStream()
    }
    
    private fun getFileNameFromUri(uri: Uri): String {
        return uri.lastPathSegment ?: "unknown"
    }
    
    private fun getFileExtension(fileName: String): String {
        return if (fileName.contains(".")) {
            fileName.substringAfterLast(".")
        } else {
            ""
        }
    }
    
    private fun createOutputFile(baseName: String, extension: String): File {
        val outputDir = File(System.getProperty("java.io.tmpdir"), "pdf_converter")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        
        var counter = 1
        var outputFile: File
        do {
            val fileName = if (counter == 1) {
                "${baseName}.${extension}"
            } else {
                "${baseName}_$counter.${extension}"
            }
            outputFile = File(outputDir, fileName)
            counter++
        } while (outputFile.exists())
        
        return outputFile
    }
}
