package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integration.delius.EventRepository
import uk.gov.justice.digital.hmpps.integration.delius.PersonRepository
import uk.gov.justice.digital.hmpps.integration.delius.getByCrn
import uk.gov.justice.digital.hmpps.model.CourtAppearance
import uk.gov.justice.digital.hmpps.model.LengthUnit
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.Sentence
import uk.gov.justice.digital.hmpps.model.Supervision

@Service
class CaseDetailsService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
) {
    fun getSupervisions(crn: String) =
        with(personRepository.getByCrn(crn)) {
            eventRepository.findByPersonIdOrderByConvictionDateDesc(id).map { event ->
                Supervision(
                    number = event.number.toInt(),
                    active = event.active,
                    date = event.convictionDate,
                    sentence =
                        event.disposal?.let { disposal ->
                            Sentence(
                                description = disposal.type.description,
                                date = disposal.date,
                                length = disposal.length?.toInt(),
                                lengthUnits = disposal.lengthUnits?.let { LengthUnit.valueOf(it.description) },
                                custodial = disposal.type.isCustodial(),
                            )
                        },
                    mainOffence = event.mainOffence.let { Offence.of(it.date, it.count, it.offence) },
                    additionalOffences = event.additionalOffences.map { Offence.of(it.date, it.count, it.offence) },
                    courtAppearances =
                        event.courtAppearances.map {
                            CourtAppearance(
                                type = it.type.description,
                                date = it.date,
                                court = it.court.name,
                                plea = it.plea?.description,
                            )
                        },
                )
            }
        }
}
