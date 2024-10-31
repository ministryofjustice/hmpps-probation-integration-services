package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocumentType
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import java.util.*

@Component
class DocumentDataLoader(
    private val personRepository: PersonRepository,
    private val documentRepository: DocumentRepository,
    private val eventRepository: EventRepository
) {
    fun loadData() {
        documentRepository.save(DocumentGenerator.EVENT_DOC)
        documentRepository.save(DocumentGenerator.PERSON_DOC)

        val person = personRepository.findByCrnAndSoftDeletedIsFalse(ProbationCaseGenerator.CASE_X320741.crn)!!

        documentRepository.save(
            DocumentGenerator.generatePersonDoc(
                person = person,
                name = "Random offender document.pdf",
                alfrescoId = UUID.randomUUID().toString(),
                documentType = DocumentType.DOCUMENT
            )
        )

        val personEvent = PersonGenerator.generateEvent(
            "1",
            person.id
        ).apply(eventRepository::save)

        listOf(
            Pair("CPS pack.pdf", DocumentType.CPS_PACK),
            Pair("Conviction document.pdf", DocumentType.PREVIOUS_CONVICTION)
        ).forEach {
            documentRepository.save(
                DocumentGenerator.generateEventDoc(
                    person = person,
                    event = personEvent,
                    name = it.first,
                    alfrescoId = UUID.randomUUID().toString(),
                    documentType = it.second,
                )
            )
        }
    }
}