package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseView
import uk.gov.justice.digital.hmpps.api.model.CvAddress
import uk.gov.justice.digital.hmpps.api.model.CvDocument
import uk.gov.justice.digital.hmpps.api.model.CvOffence
import uk.gov.justice.digital.hmpps.api.model.CvRequirement
import uk.gov.justice.digital.hmpps.api.model.CvSentence
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewAdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewAdditionalOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewPerson
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewPersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewRequirement
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.SentenceSummary
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.Document
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocumentType

@Service
class CaseViewService(
    val personRepository: CaseViewPersonRepository,
    val additionalOffenceRepository: CaseViewAdditionalOffenceRepository,
    val requirementRepository: CaseViewRequirementRepository,
    val documentRepository: DocumentRepository
) {
    fun caseView(crn: String, eventNumber: String): CaseView {
        val person = personRepository.getByCrn(crn)
        val address = personRepository.findMainAddress(person.id)
        val sentence = personRepository.findSentenceSummary(person.id, eventNumber)
            ?: throw NotFoundException("Event", "number", eventNumber)
        val additionalOffences = additionalOffenceRepository.findAllByEventId(sentence.eventId)
        val requirements = requirementRepository.findAllByDisposalEventId(sentence.eventId)
        val docs = documentRepository.findCpsAndPreCons(person.id).associateBy { it.type }
        val cpsPack = docs[DocumentType.CPS_PACK]
        val preCon = docs[DocumentType.PREVIOUS_CONVICTION]
        val courtReport = documentRepository.findLatestCourtReport(person.id)
        return CaseView(
            person.name(),
            person.dateOfBirth,
            person.gender?.description,
            person.pncNumber,
            address?.toCvAddress(),
            sentence.toCvSentence(),
            listOf(sentence.mainOffence()) + additionalOffences.map { it.toCvOffence() },
            requirements.map { it.toCvRequirement() },
            cpsPack?.toCvDocument(),
            preCon?.toCvDocument(),
            courtReport?.toCvDocument()
        )
    }

    private fun CaseViewPerson.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)

    private fun CaseViewPersonAddress.toCvAddress() = CvAddress(
        buildingName,
        addressNumber,
        streetName,
        town,
        county,
        postcode,
        noFixedAbode ?: false,
        typeVerified ?: false,
        type?.description,
        startDate
    )

    private fun SentenceSummary.toCvSentence() = CvSentence(description, startDate, length, endDate)
    private fun SentenceSummary.mainOffence() = CvOffence(offenceMainCategory, offenceSubCategory, true)
    private fun CaseViewAdditionalOffence.toCvOffence() =
        CvOffence(offence.mainCategoryDescription, offence.subCategoryDescription, false)

    private fun CaseViewRequirement.toCvRequirement() = CvRequirement(
        mainCategory.description,
        subCategory?.description,
        length?.let { "$length ${mainCategory.units?.description ?: ""}" } ?: ""
    )

    private fun Document.toCvDocument() = CvDocument(
        alfrescoId!!,
        name,
        dateProduced?.toLocalDate() ?: lastSaved!!.toLocalDate(),
        findRelatedTo().description
    )
}
