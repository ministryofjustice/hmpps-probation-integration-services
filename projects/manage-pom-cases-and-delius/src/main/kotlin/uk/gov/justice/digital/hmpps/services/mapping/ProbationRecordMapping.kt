package uk.gov.justice.digital.hmpps.services.mapping

import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.api.model.Resourcing
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData

fun Person.record(decision: ReferenceData?, registration: Registration?, vloAssigned: Boolean): ProbationRecord =
    ProbationRecord(
        crn,
        nomsId,
        currentTier?.description,
        decision?.resourcing(),
        manager.manager(),
        registration.level(),
        vloAssigned
    )

fun ReferenceData?.resourcing() = when (this?.code) {
    "R" -> Resourcing.ENHANCED
    "A" -> Resourcing.NORMAL
    else -> null
}

fun District.forManager() =
    uk.gov.justice.digital.hmpps.api.model.LocalDeliveryUnit(code, description)

fun Team.forManager() =
    uk.gov.justice.digital.hmpps.api.model.Team(code, description, district?.forManager())

fun Staff.name() = Name(listOfNotNull(forename, middleName).joinToString(" "), surname)
fun PersonManager.manager() =
    if (staff.isUnallocated()) {
        Manager(team.forManager())
    } else {
        Manager(team.forManager(), staff.code, staff.name(), staff.user?.email)
    }

fun Registration?.level(): Int = when (this?.level?.code) {
    "M1" -> 1
    "M2" -> 2
    "M3" -> 3
    else -> 0
}
