package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.*
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.Document
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocumentType
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository

@Service
class CaseViewService(
    private val personRepository: CaseViewPersonRepository,
    private val eventRepository: EventRepository,
    private val additionalOffenceRepository: CaseViewAdditionalOffenceRepository,
    private val requirementRepository: CaseViewRequirementRepository,
    private val contactRepository: ContactRepository,
    private val documentRepository: DocumentRepository
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

    fun reallocationCaseView(crn: String): ReallocationCaseView {
        val person = personRepository.getByCrn(crn)
        val address = personRepository.findMainAddress(person.id)
        val events = eventRepository.getActiveOrders(person.id).map {
            val sentence = personRepository.findSentenceSummary(person.id, it.number)
            val additionalOffences = additionalOffenceRepository.findAllByEventId(it.id)
            val requirements = requirementRepository.findAllByDisposalEventId(it.id)
            ReallocationCaseView.ActiveEvent(
                it.number,
                it.failureToComplyCount,
                listOfNotNull(it.referralDate, it.breachEnd, sentence?.startDate).max(),
                sentence?.toCvSentence(),
                listOfNotNull(sentence?.mainOffence()) + additionalOffences.map { o -> o.toCvOffence() },
                requirements.map { r -> r.toCvRequirement() },
            )
        }
        val nextAppointmentDate = contactRepository.getNextAppointmentDate(person.id)

        return ReallocationCaseView(
            person.name(),
            person.dateOfBirth,
            person.gender?.description,
            person.pncNumber,
            address?.toCvAddress(),
            nextAppointmentDate,
            events
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
