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

    fun convert(inputUri: Uri, conversionType: ConversionType): String {
        return when (conversionType) {
            ConversionType.PDF_TO_DOCX -> convertPdfToDocx(inputUri)
            ConversionType.DOCX_TO_PDF -> convertDocxToPdf(inputUri)
            ConversionType.ANY_TO_PDF -> convertAnyToPdf(inputUri)
        }.also { outputPath ->
            // Scan the file so it appears in Downloads app
            scanFile(outputPath)
            
            // Show notification
            val fileName = File(outputPath).name
            showFileSavedNotification(fileName, outputPath)
        }
    }

    private fun convertPdfToDocx(inputUri: Uri): String {
        val inputStream = getInputStreamFromUri(inputUri)
        val fileName = getFileNameFromUri(inputUri)
        val outputFile = createOutputFile(fileName, "docx")
        
        try {
            // For now, we'll create a simple DOCX with extracted text
            // In a real implementation, you'd use iText7 to extract text from PDF
            val document = XWPFDocument()
            val paragraph = document.createParagraph()
            val run = paragraph.createRun()
            run.setText("Converted from PDF: $fileName")
            
            // TODO: Implement actual PDF text extraction using iText7
            // This is a placeholder - you'd need to implement PDF parsing
            
            val outputStream = FileOutputStream(outputFile)
            document.write(outputStream)
            outputStream.close()
            document.close()
            
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
            val document = XWPFDocument(inputStream)
            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfWriter)
            val pdfDoc = Document(pdfDocument)
            
            // Extract text from DOCX and add to PDF
            for (paragraph in document.paragraphs) {
                val text = paragraph.text
                if (text.isNotEmpty()) {
                    val pdfParagraph = Paragraph(text)
                    pdfDoc.add(pdfParagraph)
                }
            }
            
            pdfDoc.close()
            pdfDocument.close()
            pdfWriter.close()
            document.close()
            
            return outputFile.absolutePath
        } catch (e: Exception) {
            throw RuntimeException("Failed to convert DOCX to PDF: ${e.message}")
        }
    }

    private fun convertAnyToPdf(inputUri: Uri): String {
        val fileExtension = getFileExtension(inputUri)
        return when (fileExtension.lowercase()) {
            "docx" -> convertDocxToPdf(inputUri)
            "txt" -> convertTextToPdf(inputUri)
            "rtf" -> convertRtfToPdf(inputUri)
            "xlsx" -> convertXlsxToPdf(inputUri)
            else -> throw RuntimeException("Unsupported file format: $fileExtension")
        }
    }

    private fun convertXlsxToPdf(inputUri: Uri): String {
        val inputStream = getInputStreamFromUri(inputUri)
        val fileName = getFileNameFromUri(inputUri)
        val outputFile = createOutputFile(fileName, "pdf")
        
        try {
            // Use Apache POI to read XLSX file
            val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0) // Get first sheet
            
            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfWriter)
            val pdfDoc = Document(pdfDocument)
            
            // Add title
            val titleParagraph = Paragraph("Excel File: $fileName")
            titleParagraph.setFontSize(16f)
            pdfDoc.add(titleParagraph)
            
            // Extract data from Excel and add to PDF
            for (rowIndex in 0..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex)
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
            
            pdfDoc.close()
            pdfDocument.close()
            pdfWriter.close()
            workbook.close()
            
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
        
        // Save to public Downloads directory for easy access
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        return File(downloadsDir, outputFileName)
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
