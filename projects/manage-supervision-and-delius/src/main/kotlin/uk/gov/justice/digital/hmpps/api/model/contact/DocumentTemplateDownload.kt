package uk.gov.justice.digital.hmpps.api.model.contact

data class DocumentTemplateDownload(
    val fileName:String,
    val fileContent: ByteArray
)
