package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Colour
import uk.gov.justice.digital.hmpps.api.model.RegisterFlag
import uk.gov.justice.digital.hmpps.api.model.RiskItem
import uk.gov.justice.digital.hmpps.api.model.RiskSummary
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.RegistrationRepository

@Service
class RegistrationService(private val registrationRepository: RegistrationRepository) {
    fun findActiveRegistrations(crn: String): RiskSummary {
        val regMap:Map<String, List<Registration>> = registrationRepository
            .findAllByPersonCrn(crn)
            .filter { it.type.flag != null }
            .groupBy { it.type.flag!!.code  }


        return RiskSummary(rosh = regMap.getItem(RegisterFlag.ROSH.code),
                            alerts = regMap.getItem(RegisterFlag.ALERTS.code),
                            safeguarding = regMap.getItem(RegisterFlag.SAFEGUARDING.code),
                            information = regMap.getItem(RegisterFlag.INFORMATION.code),
                            publicProtection = regMap.getItem(RegisterFlag.PUBLIC_PROTECTION.code)
            )
    }

    fun Map<String, List<Registration>>.getItem(code: String): RiskItem? =
        get(code)?.minByOrNull { Colour.of(it.type.colour).priority }?.let {
            RiskItem(it.type.flag!!.description, Colour.of(it.type.colour))
        }
}




