package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Conviction
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.Offence
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.api.model.Sentence
import uk.gov.justice.digital.hmpps.api.model.toOffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdditionalOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.PersonManagerRepository

@Service
class OffenderService(
    private val personManagerRepository: PersonManagerRepository,
    private val disposalRepository: DisposalRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val additionalOffenceRepository: AdditionalOffenceRepository

) {
    fun getProbationRecord(crn: String): ProbationRecord {
        val personManager = personManagerRepository.findActiveManager(crn)
        // TODO get email and telephone for staff
        val convictions = getConvictions(crn)
        return ProbationRecord(crn, listOf(personManager.toOffenderManager()), convictions)
    }

    private fun getOffences(event: Event): List<Offence> {
        val offences: ArrayList<Offence> = arrayListOf()
        mainOffenceRepository.findByEvent(event).let { offences += Offence(it.offence.description, true, it.date, null) } // TODO populate plea
        additionalOffenceRepository.findByEvent(event).forEach {
            offences += Offence(it.offence.description, false, it.date, null) // TODO populate plea
        }
        return offences
    }

    fun getConvictions(crn: String): List<Conviction> {
        val disposals = disposalRepository.getByCrn(crn)
        val convictions = arrayListOf<Conviction>()
        disposals.forEach { disposal ->
            convictions += Conviction(
                disposal.event.active,
                disposal.event.inBreach,
                false,
                disposal.event.convictionDate,
                getOffences(disposal.event),
                disposal.sentenceOf(),
                custodialType = disposal.custody?.let { c -> KeyValue(c.status.code, c.status.description) },
                documents = listOf(),
                breaches = listOf(),
                requirements = listOf(),
                pssRequirements = listOf(),
                licenceConditions = listOf()
            )
        }

        return convictions
    }
}

fun Disposal.sentenceOf() = Sentence(
    disposalType.description,
    entryLength,
    entryLengthUnit?.description,
    lengthInDays,
    terminationDate?.toLocalDate(),
    startDate.toLocalDate(),
    endDate?.toLocalDate(),
    terminationReason?.description,
    null // TODO fill this in.
)
