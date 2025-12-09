package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.component.transfer.RequirementTransfer
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import java.time.ZonedDateTime

object RequirementTransferGenerator {
    fun generate(
        requirement: Requirement,
        status: ReferenceData,
        originTeam: Team,
        originStaff: Staff,
        receivingTeam: Team = originTeam,
        receivingStaff: Staff = originStaff,
        requestDate: ZonedDateTime = ZonedDateTime.now(),
        id: Long = id()
    ) = RequirementTransfer(
        id = id,
        requirement = requirement,
        status = status,
        requestDate = requestDate,
        originTeam = originTeam,
        originStaff = originStaff,
        receivingTeam = receivingTeam,
        receivingStaff = receivingStaff
    )
}
