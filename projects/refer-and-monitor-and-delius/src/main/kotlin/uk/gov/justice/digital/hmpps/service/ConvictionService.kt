package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseConviction
import uk.gov.justice.digital.hmpps.api.model.CaseConvictions
import uk.gov.justice.digital.hmpps.api.model.Conviction
import uk.gov.justice.digital.hmpps.api.model.Offence
import uk.gov.justice.digital.hmpps.api.model.Sentence
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByCrnAndId

@Service
class ConvictionService(private val personService: PersonService, private val eventRepository: EventRepository) {
    fun findConvictions(crn: String): CaseConvictions = personService.findDetailsFor(crn)?.let { case ->
        CaseConvictions(case, eventRepository.findAllByCrn(crn).map { it.asConviction() })
    } ?: personNotFound(crn)

    fun findConviction(crn: String, convictionId: Long): CaseConviction = personService.findDetailsFor(crn)?.let {
        CaseConviction(it, eventRepository.getByCrnAndId(crn, convictionId).asConviction())
    } ?: personNotFound(crn)

    fun personNotFound(crn: String): Nothing = throw NotFoundException("Person", "crn", crn)
}

fun Event.asConviction() = Conviction(
    id,
    convictionDate,
    Sentence(disposal!!.type.description, disposal.expectedEndDate()),
    Offence(mainOffence!!.offence.mainCategoryDescription, mainOffence.offence.subCategoryDescription),
    active && disposal.active
)
