package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.model.PersonalDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.entity.DisabilityRepository
import uk.gov.justice.digital.hmpps.entity.NsiRepository
import uk.gov.justice.digital.hmpps.entity.PersonCircumstanceRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

@Service
class PersonalDetailsService(
    private val personRepository: PersonRepository,
    private val personalCircumstanceRepository: PersonCircumstanceRepository,
    private val disabilityRepository: DisabilityRepository,
    private val nsiRepository: NsiRepository,
) {
    fun getPersonDetails(crn: String): PersonalDetailsAndCircumstances {
        val person = personRepository.findByCrn(crn).orNotFoundBy("crn", crn)
        val personalCircumstances = personalCircumstanceRepository.findCurrentCircumstances(person.id)
        val disabilities = disabilityRepository.findByPersonId(person.id)
        val opd = nsiRepository.findOffenderPersonalityDisorderByCrn(crn)
        return PersonalDetailsAndCircumstances(
            preferredLanguage = person.language?.toCodeAndDescription(),
            personalCircumstances = personalCircumstances.map { it.toPersonCircumstance() },
            disabilities = disabilities.map { it.toModelDisability() },
            offenderPersonalityDisorder = opd?.toOffenderPersonalityDisorder()
        )
    }
}