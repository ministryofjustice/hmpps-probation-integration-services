package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.model.*

@Service
class MappaInformationService(
    private val personRepository: PersonRepository,
    private val registrationRepository: RegistrationRepository,
) {
    fun getMappaInformation(crn: String): MappaInformation {
        val person = personRepository.getPerson(crn)
        val latestMappaRegistration = registrationRepository.findLatestMappaRegistration(person.id)

        return if (latestMappaRegistration != null) {
            MappaInformation(
                subjectOfMappaProcedures = true,
                mappaRegistration = latestMappaRegistration.toModel()
            )
        } else {
            MappaInformation(subjectOfMappaProcedures = false)
        }
    }
}

private fun Registration.toModel() = MappaRegistration(
    id = id,
    type = MappaType(
        code = type.code,
        description = type.description,
    ),
    startDate = date,
    notes = notes,
)