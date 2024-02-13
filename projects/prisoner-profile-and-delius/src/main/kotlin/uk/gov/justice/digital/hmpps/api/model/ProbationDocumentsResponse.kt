package uk.gov.justice.digital.hmpps.api.model

data class ProbationDocumentsResponse(
    val crn: String,
    val name: Name,
    val documents: List<Document>,
    val convictions: List<Conviction>
)
