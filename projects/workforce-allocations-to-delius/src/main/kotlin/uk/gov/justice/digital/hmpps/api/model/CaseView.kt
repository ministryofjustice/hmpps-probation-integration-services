package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate
import java.time.Period

data class CaseView(
    val name: Name,
    val dateOfBirth: LocalDate,
    val gender: String?,
    val pncNumber: String?,
    val mainAddress: CvAddress?,
    val sentence: CvSentence,
    val offences: List<CvOffence>,
    val requirements: List<CvRequirement>,
    val cpsPack: CvDocument? = null,
    val preConvictionDocument: CvDocument? = null,
    val courtReport: CvDocument? = null,
) {
    val age: Int
        get() = Period.between(dateOfBirth, LocalDate.now()).years
}

data class CvAddress(
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val noFixedAbode: Boolean,
    val typeVerified: Boolean,
    val typeDescription: String?,
    val startDate: LocalDate,
)

data class CvOffence(val mainCategory: String, val subCategory: String, val mainOffence: Boolean)

data class CvSentence(
    val description: String,
    val startDate: LocalDate,
    val length: String,
    val endDate: LocalDate,
)

data class CvRequirement(
    val mainCategory: String,
    val subCategory: String?,
    val length: String,
)

data class CvDocument(
    val documentId: String,
    val documentName: String,
    val dateCreated: LocalDate,
    val description: String? = null,
)
