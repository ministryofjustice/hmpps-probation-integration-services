package uk.gov.justice.digital.hmpps.integrations.opd

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.PersonRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class OPDService(
    private val personRepository: PersonRepository,
    private val telemetryService: TelemetryService
) {
    @Transactional
    fun processAssessment(crn: String, opdAssessment: OPDAssessment) {
        val person = personRepository.findByCrn(crn) ?: return let {
            telemetryService.trackEvent("PersonNotFound", opdAssessment.telemetryProperties(crn))
        }
    }
}
