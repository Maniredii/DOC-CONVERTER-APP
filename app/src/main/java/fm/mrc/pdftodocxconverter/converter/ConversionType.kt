package fm.mrc.pdftodocxconverter.converter

enum class ConversionType(val displayName: String, val description: String) {
    PDF_TO_DOCX(
        displayName = "PDF to DOCX",
        description = "Convert PDF files to editable Word documents"
    ),
    DOCX_TO_PDF(
        displayName = "DOCX to PDF",
        description = "Convert Word documents to PDF format"
    ),
    ANY_TO_PDF(
        displayName = "Any to PDF",
        description = "Convert various formats to PDF"
    );
    
    companion object {
        fun fromString(value: String): ConversionType? {
            return values().find { it.name == value.uppercase() }
        }
    }
}
