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

class DocumentConverter(private val context: Context) {

    fun convert(inputUri: Uri, conversionType: ConversionType): String {
        return when (conversionType) {
            ConversionType.PDF_TO_DOCX -> convertPdfToDocx(inputUri)
            ConversionType.DOCX_TO_PDF -> convertDocxToPdf(inputUri)
            ConversionType.ANY_TO_PDF -> convertAnyToPdf(inputUri)
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
            else -> throw RuntimeException("Unsupported file format: $fileExtension")
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

    private fun createOutputFile(inputFileName: String, outputExtension: String): File {
        val baseName = inputFileName.substringBeforeLast(".")
        val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(outputDir, "${baseName}_converted.$outputExtension")
    }
}
