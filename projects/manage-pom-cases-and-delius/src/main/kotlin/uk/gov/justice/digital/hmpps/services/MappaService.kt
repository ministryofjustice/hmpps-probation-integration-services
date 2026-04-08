package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.MappaDetail
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository

@Service
class MappaService(
    private val registrationRepository: RegistrationRepository,
    private val personRepository: PersonRepository
) {
    fun getMappaDetail(crn: String): MappaDetail {
        val person = personRepository.getByCrn(crn)
        return registrationRepository.findMappaRegistration(person.id)?.toMappa().orNotFoundBy("crn", crn)
    }
}

fun Registration.toMappa() = MappaDetail(
    level = level?.code?.let { code -> MappaLevel.toCommunityLevel(code) },
    levelDescription = level?.description,
    category = category?.code?.let { code -> MappaCategory.toCommunityCategory(code) },
    categoryDescription = category?.description,
    startDate = date,
    reviewDate = nextReviewDate
)

enum class MappaLevel(val communityValue: Int, val deliusValue: String) {
    NOMINAL(0, "M0"),
    ONE(1, "M1"),
    TWO(2, "M2"),
    THREE(3, "M3");

    companion object {
        fun toCommunityLevel(deliusLevel: String): Int {
            return entries.firstOrNull { level: MappaLevel -> level.deliusValue == deliusLevel }?.communityValue
                ?: NOMINAL.communityValue
        }
    }
}

internal enum class MappaCategory(val communityValue: Int, val deliusValue: String) {
    NOMINAL(0, "X9"),
    ONE(1, "M1"),
    TWO(2, "M2"),
    THREE(3, "M3"),
    FOUR(4, "M4");

    companion object {
        fun toCommunityCategory(deliusCategory: String): Int {
            return entries.firstOrNull { category: MappaCategory -> category.deliusValue == deliusCategory }?.communityValue
                ?: NOMINAL.communityValue
        }
    }
}




