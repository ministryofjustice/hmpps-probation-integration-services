package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Colour
import uk.gov.justice.digital.hmpps.api.model.RegisterFlag
import uk.gov.justice.digital.hmpps.api.model.RiskItem
import uk.gov.justice.digital.hmpps.api.model.RiskSummary
import uk.gov.justice.digital.hmpps.integrations.delius.registration.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.registration.getFlagsForCrn

@Service
class RegistrationService(private val registrationRepository: RegistrationRepository) {
    fun findActiveRegistrations(crn: String): RiskSummary {
        val regMap: Map<String, List<Registration>> =
            registrationRepository.getFlagsForCrn(crn).groupBy { it.registerType.flag!!.code }

        return RiskSummary(
            selfHarm = regMap.getItem(RegisterFlag.ROSH.code),
            alerts = regMap.getItem(RegisterFlag.ALERTS.code),
            safeguarding = regMap.getItem(RegisterFlag.SAFEGUARDING.code),
            information = regMap.getItem(RegisterFlag.INFORMATION.code),
            publicProtection = regMap.getItem(RegisterFlag.PUBLIC_PROTECTION.code)
        )
    }

    fun Map<String, List<Registration>>.getItem(code: String): RiskItem? =
        get(code)?.minByOrNull { Colour.of(it.registerType.colour!!).priority }?.let {
            RiskItem(it.registerType.flag!!.description, Colour.of(it.registerType.colour!!))
        }
}




