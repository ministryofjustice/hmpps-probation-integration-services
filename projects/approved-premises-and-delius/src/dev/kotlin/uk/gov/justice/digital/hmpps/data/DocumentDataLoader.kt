package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.generate
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import java.util.*

@Component
class DocumentDataLoader(
    private val personRepository: PersonRepository,
    private val documentRepository: DocumentRepository,
    private val eventRepository: EventRepository
) {
    fun loadData() {
        documentRepository.saveAll(
            listOf(
                DocumentGenerator.EVENT,
                DocumentGenerator.PERSON,
                DocumentGenerator.PREVIOUS_CONVICTIONS,
                DocumentGenerator.CPS_PACK,
                DocumentGenerator.ADDRESSASSESSMENT,
                DocumentGenerator.PERSONALCONTACT,
                DocumentGenerator.PERSONAL_CIRCUMSTANCE,
                DocumentGenerator.OFFENDER_CONTACT,
                DocumentGenerator.OFFENDER_NSI,
            )
        )

        val person = personRepository.getByCrn(ProbationCaseGenerator.CASE_X320741.crn)
        val personEvent = PersonGenerator.generateEvent("1", person.id)
            .apply(eventRepository::save)

        val personDocument = generate(
            tableName = "OFFENDER",
            person = person,
            name = "Random offender document.pdf",
            alfrescoId = UUID.randomUUID().toString()
        )
        val cpsPack = generate(
            tableName = "EVENT",
            type = "CPS_PACK",
            name = "CPS pack.pdf",
            person = person,
            primaryKeyId = personEvent.id
        )
        val previousConvictions = generate(
            tableName = "OFFENDER",
            type = "PREVIOUS_CONVICTION",
            name = "Conviction document.pdf",
            person = person,
            primaryKeyId = person.id
        )

        documentRepository.saveAll(
            listOf(
                personDocument,
                cpsPack,
                previousConvictions
            )
        )
    }
}