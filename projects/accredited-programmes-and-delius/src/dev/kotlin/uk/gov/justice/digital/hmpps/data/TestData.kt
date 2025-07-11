package uk.gov.justice.digital.hmpps.data

import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.LocalAdminUnit
import uk.gov.justice.digital.hmpps.entity.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.Team

object TestData {
    val GENDER = ReferenceData(id(), "M", "Male")
    val ETHNICITY = ReferenceData(id(), "A9", "Asian or Asian British: Other")
    val PDU = ProbationDeliveryUnit(id(), "PDU1", "Test PDU")
    val LAU = LocalAdminUnit(id(), PDU)
    val TEAM = Team(id(), LAU)
    val STAFF = StaffGenerator.generate()
    val PERSON = PersonGenerator.generate("A000001", GENDER, ETHNICITY)
    val MANAGER = ManagerGenerator.generate(PERSON, STAFF, TEAM)
    val USER = UserGenerator.generate("TestUser", STAFF)
    val USER_WITH_LIMITED_ACCESS = UserGenerator.generate("TestUserWithLimitedAccess")
    val RESTRICTION = RestrictionGenerator.generate(PERSON, USER)
    val EXCLUSION = ExclusionGenerator.generate(PERSON, USER_WITH_LIMITED_ACCESS)
}