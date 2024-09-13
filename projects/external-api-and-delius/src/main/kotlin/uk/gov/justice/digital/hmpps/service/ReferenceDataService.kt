package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integration.delius.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.model.ProbationReferenceData
import uk.gov.justice.digital.hmpps.model.RefData

@Service
class ReferenceDataService(
    private val registrationRepository: RegistrationRepository,
) {
    fun getReferenceData(): ProbationReferenceData {

        val refData = LinkedHashMap<String, MutableList<RefData>>()
        refData["PHONE_TYPE"] = PhoneTypes.entries.map { RefData(it.name, it.description) }.toMutableList()
        refData.putAll(
            registrationRepository.getReferenceData()
                .groupByTo(LinkedHashMap(), { it.codeSet.trim() }, { RefData(it.code.trim(), it.description) })
        )
        return ProbationReferenceData(refData)
    }
}

enum class PhoneTypes(val description: String) {
    TELEPHONE("Home"),
    MOBILE("Mobile")
}
