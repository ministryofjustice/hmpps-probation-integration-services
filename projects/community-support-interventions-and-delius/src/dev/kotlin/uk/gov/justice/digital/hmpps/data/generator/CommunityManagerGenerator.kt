package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.CommunityManager
import uk.gov.justice.digital.hmpps.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.Pdu
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.StaffUser
import uk.gov.justice.digital.hmpps.entity.Team

object CommunityManagerGenerator {
    val JOB_ROLE = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "PO",
        description = "Probation Officer",
    )

    val STAFF = Staff(
        id = IdGenerator.getAndIncrement(),
        forename = "John",
        middleName = "Michael",
        surname = "Smith",
        code = "N01A001",
        jobRole = JOB_ROLE,
    )

    val STAFF_USER = StaffUser(
        id = IdGenerator.getAndIncrement(),
        staff = STAFF,
        userName = "john.smith",
    )

    val OFFICE_LOCATION = OfficeLocation(
        id = IdGenerator.getAndIncrement(),
        description = "Sheffield Office",
    )

    val TEAM = Team(
        id = IdGenerator.getAndIncrement(),
        code = "N01T01",
        telephone = "0114 123 4567",
        officeLocations = mutableListOf(OFFICE_LOCATION),
    )

    val PDU = Pdu(
        id = IdGenerator.getAndIncrement(),
        code = "N01",
        description = "South Yorkshire",
    )

    val COMMUNITY_MANAGER = CommunityManager(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON1,
        softDeleted = false,
        active = true,
        staff = STAFF,
    )
}

