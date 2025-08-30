package fm.mrc.pdftodocxconverter.converter

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.os.Environment
import android.media.MediaScannerConnection
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class DocumentConverter(private val context: Context) {

    fun convert(inputUri: Uri, conversionType: ConversionType, progressCallback: ((Int) -> Unit)? = null): String {
        try {
            // Validate input file first
            val fileSize = validateInputFile(inputUri)
            progressCallback?.invoke(10)
            
            val outputPath = when (conversionType) {
                ConversionType.PDF_TO_DOCX -> {
                    progressCallback?.invoke(30)
                    convertPdfToDocx(inputUri)
                }
                ConversionType.DOCX_TO_PDF -> {
                    progressCallback?.invoke(30)
                    convertDocxToPdf(inputUri)
                }
                ConversionType.ANY_TO_PDF -> {
                    progressCallback?.invoke(30)
                    convertAnyToPdf(inputUri)
                }
            }
            
            progressCallback?.invoke(80)
            
            // Scan the file so it appears in Downloads app
            try {
                scanFile(outputPath)
            } catch (e: Exception) {
                // Ignore scan errors, file will still be saved
            }
            
            progressCallback?.invoke(90)
            
            // Show notification
            try {
                val fileName = File(outputPath).name
                showFileSavedNotification(fileName, outputPath)
            } catch (e: Exception) {
                // Ignore notification errors
            }
            
            progressCallback?.invoke(100)
            return outputPath
        } catch (e: Exception) {
            throw RuntimeException("Conversion failed: ${e.message}")
        }
    }

    private fun convertPdfToDocx(inputUri: Uri): String {
        val inputStream = getInputStreamFromUri(inputUri)
        val fileName = getFileNameFromUri(inputUri)
        val outputFile = createOutputFile(fileName, "docx")

        try {
            // Use buffered streams for better memory management
            val bufferedInputStream = inputStream.buffered()
            
            // Create a simple DOCX document for now
            // TODO: Implement actual PDF text extraction using iText7
            val document = XWPFDocument()
            
            val paragraph = document.createParagraph()
            val run = paragraph.createRun()
            run.setText("PDF to DOCX conversion completed.")
            run.addBreak()
            run.setText("Original file: $fileName")
            run.addBreak()
            run.setText("Note: This is a placeholder conversion. Full PDF text extraction will be implemented in future updates.")
            
            val outputStream = FileOutputStream(outputFile)
            document.write(outputStream)
            document.close()
            outputStream.close()
            bufferedInputStream.close()

            return outputFile.absolutePath
        } catch (e: Exception) {
            throw RuntimeException("Failed to convert PDF to DOCX: ${e.message}")
        }
    }

    private fun convertDocxToPdf(inputUri: Uri): String {
        val inputStream = getInputStreamFromUri(inputUri)
        val fileName = getFileNameFromUri(inputUri)
        val outputFile = createOutputFile(fileName, "pdf")

        try {
            // Use buffered streams for better memory management
            val bufferedInputStream = inputStream.buffered()
            
            // Load DOCX with memory optimization
            val document = XWPFDocument(bufferedInputStream)
            
            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfWriter)
            val pdfDoc = Document(pdfDocument)

            // Process paragraphs in chunks to avoid memory issues
            val paragraphs = document.paragraphs
            val chunkSize = 50 // Process 50 paragraphs at a time
            
            for (i in paragraphs.indices step chunkSize) {
                val endIndex = minOf(i + chunkSize, paragraphs.size)
                val chunk = paragraphs.subList(i, endIndex)
                
                for (paragraph in chunk) {
                    val text = paragraph.text
                    if (text.isNotEmpty()) {
                        val pdfParagraph = Paragraph(text)
                        pdfDoc.add(pdfParagraph)
                    }
                }
                
                // Force garbage collection periodically
                if (i % 200 == 0) {
                    System.gc()
                }
            }

            pdfDoc.close()
            pdfDocument.close()
            pdfWriter.close()
            document.close()
            bufferedInputStream.close()

            return outputFile.absolutePath
        } catch (e: Exception) {
            throw RuntimeException("Failed to convert DOCX to PDF: ${e.message}")
        }
    }

    private fun convertAnyToPdf(inputUri: Uri): String {
        val extension = getFileExtension(inputUri).lowercase()
        
        return when (extension) {
            "xlsx" -> convertXlsxToPdf(inputUri)
            "docx" -> convertDocxToPdf(inputUri)
            "txt" -> convertTextToPdf(inputUri)
            "rtf" -> convertRtfToPdf(inputUri)
            else -> {
                // For unsupported formats, create a simple PDF with file info
                val inputStream = getInputStreamFromUri(inputUri)
                val fileName = getFileNameFromUri(inputUri)
                val outputFile = createOutputFile(fileName, "pdf")
                
                try {
                    val bufferedInputStream = inputStream.buffered()
                    
                    val pdfWriter = PdfWriter(outputFile)
                    val pdfDocument = PdfDocument(pdfWriter)
                    val pdfDoc = Document(pdfDocument)
                    
                    val titleParagraph = Paragraph("Unsupported File Format")
                    titleParagraph.setFontSize(16f)
                    pdfDoc.add(titleParagraph)
                    
                    val infoParagraph = Paragraph("File: $fileName\nFormat: $extension\n\nThis file format is not yet supported for conversion to PDF.")
                    pdfDoc.add(infoParagraph)
                    
                    pdfDoc.close()
                    pdfDocument.close()
                    pdfWriter.close()
                    bufferedInputStream.close()
                    
                    outputFile.absolutePath
                } catch (e: Exception) {
                    throw RuntimeException("Failed to create PDF for unsupported format: ${e.message}")
                }
            }
        }
    }

    private fun convertXlsxToPdf(inputUri: Uri): String {
        val inputStream = getInputStreamFromUri(inputUri)
        val fileName = getFileNameFromUri(inputUri)
        val outputFile = createOutputFile(fileName, "pdf")

        try {
            // Use buffered streams for better memory management
            val bufferedInputStream = inputStream.buffered()
            
            val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook(bufferedInputStream)
            val sheet = workbook.getSheetAt(0)

            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfWriter)
            val pdfDoc = Document(pdfDocument)

            val titleParagraph = Paragraph("Excel File: $fileName")
            titleParagraph.setFontSize(16f)
            pdfDoc.add(titleParagraph)

            // Process rows in chunks to avoid memory issues
            val lastRowNum = sheet.lastRowNum
            val chunkSize = 100 // Process 100 rows at a time
            
            for (rowIndex in 0..lastRowNum step chunkSize) {
                val endRowIndex = minOf(rowIndex + chunkSize, lastRowNum + 1)
                
                for (currentRowIndex in rowIndex until endRowIndex) {
                    val row = sheet.getRow(currentRowIndex)
                    if (row != null) {
                        val rowText = StringBuilder()
                        for (cellIndex in 0 until row.lastCellNum) {
                            val cell = row.getCell(cellIndex)
                            if (cell != null) {
                                when (cell.cellType) {
                                    org.apache.poi.ss.usermodel.CellType.STRING -> rowText.append(cell.stringCellValue)
                                    org.apache.poi.ss.usermodel.CellType.NUMERIC -> rowText.append(cell.numericCellValue.toString())
                                    org.apache.poi.ss.usermodel.CellType.BOOLEAN -> rowText.append(cell.booleanCellValue.toString())
                                    org.apache.poi.ss.usermodel.CellType.FORMULA -> rowText.append(cell.cellFormula)
                                    else -> rowText.append("")
                                }
                                rowText.append("\t")
                            }
                        }
                        if (rowText.isNotEmpty()) {
                            val pdfParagraph = Paragraph(rowText.toString().trim())
                            pdfDoc.add(pdfParagraph)
                        }
                    }
                }
                
                // Force garbage collection periodically
                if (rowIndex % 500 == 0) {
                    System.gc()
                }
            }

            pdfDoc.close()
            pdfDocument.close()
            pdfWriter.close()
            workbook.close()
            bufferedInputStream.close()

            return outputFile.absolutePath
        } catch (e: Exception) {
            throw RuntimeException("Failed to convert XLSX to PDF: ${e.message}")
        }
    }

    private fun convertTextToPdf(inputUri: Uri): String {
        val inputStream = getInputStreamFromUri(inputUri)
        val fileName = getFileNameFromUri(inputUri)
        val outputFile = createOutputFile(fileName, "pdf")
        
        try {
            val text = inputStream.bufferedReader().use { it.readText() }
            
            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfWriter)
            val pdfDoc = Document(pdfDocument)
            
            val pdfParagraph = Paragraph(text)
            pdfDoc.add(pdfParagraph)
            
            pdfDoc.close()
            pdfDocument.close()
            pdfWriter.close()
            
            return outputFile.absolutePath
        } catch (e: Exception) {
            throw RuntimeException("Failed to convert text to PDF: ${e.message}")
        }
    }

    private fun convertRtfToPdf(inputUri: Uri): String {
        val inputStream = getInputStreamFromUri(inputUri)
        val fileName = getFileNameFromUri(inputUri)
        val outputFile = createOutputFile(fileName, "pdf")
        
        try {
            // TODO: Implement RTF parsing
            // For now, just create a simple PDF with placeholder text
            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfWriter)
            val pdfDoc = Document(pdfDocument)
            
            val pdfParagraph = Paragraph("RTF conversion not yet implemented")
            pdfDoc.add(pdfParagraph)
            
            pdfDoc.close()
            pdfDocument.close()
            pdfWriter.close()
            
            return outputFile.absolutePath
        } catch (e: Exception) {
            throw RuntimeException("Failed to convert RTF to PDF: ${e.message}")
        }
    }

    private fun validateInputFile(inputUri: Uri): Long {
        val inputStream = getInputStreamFromUri(inputUri)
        val fileSize = inputStream.available().toLong()
        inputStream.close()
        
        // Check if file is too large (e.g., > 100MB)
        val maxFileSize = 100 * 1024 * 1024 // 100MB
        if (fileSize > maxFileSize) {
            throw RuntimeException("File is too large (${fileSize / (1024 * 1024)}MB). Maximum size is 100MB.")
        }
        
        // Check if file is empty
        if (fileSize == 0L) {
            throw RuntimeException("File is empty or cannot be read.")
        }
        
        return fileSize
    }

    private fun getInputStreamFromUri(uri: Uri): InputStream {
        return context.contentResolver.openInputStream(uri)
            ?: throw RuntimeException("Could not open input stream for URI: $uri")
    }

    private fun getFileNameFromUri(uri: Uri): String {
        return DocumentFile.fromSingleUri(context, uri)?.name ?: "unknown_file"
    }

    private fun getFileExtension(uri: Uri): String {
        val fileName = getFileNameFromUri(uri)
        return if (fileName.contains(".")) {
            fileName.substringAfterLast(".")
        } else {
            ""
        }
    }

    private fun createOutputFile(fileName: String, extension: String): File {
        val baseName = fileName.substringBeforeLast(".")
        val outputFileName = "${baseName}_converted.$extension"
        
        // Try to save to public Downloads directory first
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir != null && (downloadsDir.exists() || downloadsDir.mkdirs())) {
                val file = File(downloadsDir, outputFileName)
                // Ensure we can write to this location
                if (file.parentFile?.canWrite() == true) {
                    return file
                }
            }
        } catch (e: Exception) {
            // Fallback to app's external files directory
        }
        
        // Fallback to app's external files directory
        try {
            val externalDir = context.getExternalFilesDir(null)
            if (externalDir != null) {
                return File(externalDir, outputFileName)
            }
        } catch (e: Exception) {
            // Fallback to internal files directory
        }
        
        // Final fallback to internal files directory
        val internalDir = context.filesDir
        return File(internalDir, outputFileName)
    }

    fun scanFile(filePath: String) {
        MediaScannerConnection.scanFile(context, arrayOf(filePath), null, null)
    }
    
    private fun showFileSavedNotification(fileName: String, filePath: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "file_conversion",
                "File Conversion",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for file conversion status"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open the file
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.fromFile(File(filePath)), getMimeType(filePath))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, "file_conversion")
            .setContentTitle("File Converted Successfully!")
            .setContentText("$fileName saved to Downloads")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(1, notification)
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
