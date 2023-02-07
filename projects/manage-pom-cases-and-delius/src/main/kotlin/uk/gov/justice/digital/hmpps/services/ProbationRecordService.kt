package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.api.model.Resourcing
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByNomsId
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.findMappaRegistration
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData

@Service
class ProbationRecordService(
    val personRepository: PersonRepository,
    val caseAllocationRepository: CaseAllocationRepository,
    val registrationRepository: RegistrationRepository
) {
    fun findByNomsId(nomsId: String): ProbationRecord {
        val person = personRepository.getByNomsId(nomsId)
        val decision = caseAllocationRepository.findLatestActiveDecision(person.id)
        val registration = registrationRepository.findMappaRegistration(person.id)
        return person.record(decision, registration)
    }

    private fun Person.record(decision: ReferenceData?, registration: Registration?): ProbationRecord =
        ProbationRecord(
            crn,
            nomsId!!,
            currentTier?.description,
            decision?.resourcing(),
            manager.manager(),
            registration.level()
        )

    private fun ReferenceData?.resourcing() = when (this?.code) {
        "R" -> Resourcing.ENHANCED
        "A" -> Resourcing.NORMAL
        else -> null
    }

    private fun LocalDeliveryUnit.forManager() =
        uk.gov.justice.digital.hmpps.api.model.LocalDeliveryUnit(code, description)

    private fun Team.forManager() =
        uk.gov.justice.digital.hmpps.api.model.Team(code, description, ldu?.forManager())

    private fun Staff.name() = Name(forename, middleName, surname)
    private fun PersonManager.manager() = Manager(staff.code, staff.name(), team.forManager())

    private fun Registration?.level(): Int = when (this?.level?.code) {
        "M1" -> 1
        "M2" -> 2
        "M3" -> 3
        else -> 0
    }
}
