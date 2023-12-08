package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifiers
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.MappaDetail
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.entity.Category
import uk.gov.justice.digital.hmpps.entity.Level
import uk.gov.justice.digital.hmpps.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.findMappa
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Service
class CaseDetailService(
    private val personRepository: PersonRepository,
    private val registrationRepository: RegistrationRepository,
    private val personManagerRepository: PersonManagerRepository,
) {
    fun findCrnByNomsId(nomsId: String): CaseIdentifiers =
        personRepository.findCrnByNomsId(nomsId)?.let { CaseIdentifiers(it) }
            ?: throw NotFoundException("Person", "nomsId", nomsId)

    fun findMappaDetail(crn: String): MappaDetail =
        registrationRepository.findMappa(crn)?.let {
            MappaDetail(
                it.level?.code?.toMappaLevel(),
                it.level?.description,
                it.category?.code?.toMappaCategory(),
                it.category?.description,
                it.date,
                it.reviewDate,
            )
        } ?: throw NotFoundException("No MAPPA details found for $crn")

    fun findCommunityManager(crn: String) =
        personManagerRepository.findByPersonCrn(crn)?.staff?.asManager()
            ?: throw NotFoundException("Person", "crn", crn)
}

private fun String.toMappaLevel() =
    Level.entries.find { it.name == this }?.number
        ?: throw IllegalStateException("Unexpected MAPPA level: $this")

private fun String.toMappaCategory() =
    Category.entries.find { it.name == this }?.number
        ?: throw IllegalStateException("Unexpected MAPPA category: $this")

private fun Staff.asManager() =
    if (code.endsWith("U")) {
        Manager(null, true)
    } else {
        Manager(Name(forename, surname), false)
    }
