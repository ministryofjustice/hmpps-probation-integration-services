package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Officer
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.OfficerPk
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PartitionArea
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.ProviderEmployee
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*

object StaffGenerator {
    val UNALLOCATED = generate("N01UATU")
    val ALLOCATED = generate("N01ABBA")
    val OFFICER = generateOfficer()
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = Staff(code, "Bob", "Micheal", "Smith", id)

    fun generateOfficer() =
        Officer(
            id = OfficerPk(
                trustProviderFlag = 0,
                staffEmployeeId = ALLOCATED.id
            ),
            surname = "OffSurname",
            forename = "Off1",
            forename2 = "Off2"
        )
}

object ProviderGenerator {
    val DEFAULT = generate()
    fun generate(id: Long = IdGenerator.getAndIncrement()) = ProbationAreaEntity(true, "London", "LN1", null, true, id)
}

object AreaGenerator {
    val PARTITION_AREA = PartitionArea(IdGenerator.getAndIncrement(), "Partition Area")
}

object ProviderEmployeeGenerator {
    val PROVIDER_EMPLOYEE = generateProviderEmployee()
    fun generateProviderEmployee() = ProviderEmployee(
        providerEmployeeId = IdGenerator.getAndIncrement(),
        surname = "ProvEmpSurname",
        forename = "ProvEmpForename1",
        forename2 = "ProvEmpForename2"
    )
}

object LDUGenerator {
    val DEFAULT = generate()
    fun generate(id: Long = IdGenerator.getAndIncrement()) = LocalDeliveryUnit(true, "London", "LN1", id)
}

object BoroughGenerator {
    val DEFAULT = generate()
    fun generate(id: Long = IdGenerator.getAndIncrement()) = Borough(true, id, ProviderGenerator.DEFAULT, "LN1")
}

object DistrictGenerator {
    val DEFAULT = generate()
    fun generate(id: Long = IdGenerator.getAndIncrement()) = District(
        true,
        "London",
        "LN1",
        BoroughGenerator.DEFAULT,
        id
    )
}

object TeamGenerator {
    val DEFAULT = generate()
    fun generate(id: Long = IdGenerator.getAndIncrement()) = Team(
        id,
        "T1",
        "Team1",
        "123",
        "email@address.com",
        LDUGenerator.DEFAULT,
        DistrictGenerator.DEFAULT
    )
}
