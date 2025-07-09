package uk.gov.justice.digital.hmpps.data

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.set

object TestData : TestDataManager() {
    val GENDER = add(ReferenceData(id(), "M", "Male"))
    val ETHNICITY = add(ReferenceData(id(), "A9", "Asian or Asian British: Other"))
    val PDU = add(ProbationDeliveryUnit(id(), "PDU1", "Test PDU"))
    val LAU = add(LocalAdminUnit(id(), PDU))
    val TEAM = add(Team(id(), LAU))
    val STAFF = add(StaffGenerator.generate())
    val USER = add(UserGenerator.generate(STAFF).also { STAFF.set(Staff::user, it) })
    val PERSON = add(PersonGenerator.generate("A000001", GENDER, ETHNICITY))
    val MANAGER = add(ManagerGenerator.generate(PERSON, STAFF, TEAM).also { PERSON.set(PERSON::manager, it) })
}

open class TestDataManager : Iterable<Any> {
    private val data = mutableListOf<Any>()
    fun <T : Any> add(value: T): T = value.also { data += it }
    override fun iterator(): Iterator<Any> = data.iterator()
}
