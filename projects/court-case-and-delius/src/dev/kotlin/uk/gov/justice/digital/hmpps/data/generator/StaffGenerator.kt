package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaEntity
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team

object StaffGenerator {
    val UNALLOCATED = generate("N01UATU")
    val ALLOCATED = generate("N01ABBA")
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = Staff(code, "Bob", "Micheal", "Smith", id)
}

object ProviderGenerator {
    val DEFAULT = generate()
    fun generate(id: Long = IdGenerator.getAndIncrement()) = ProbationAreaEntity(true, "London", "LN1", null, id)
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
    fun generate(id: Long = IdGenerator.getAndIncrement()) = District(true, "London", "LN1", BoroughGenerator.DEFAULT, id)
}

object TeamGenerator {
    val DEFAULT = generate()
    fun generate(id: Long = IdGenerator.getAndIncrement()) = Team(id, "T1", "Team1", "123", "email@address.com", LDUGenerator.DEFAULT, DistrictGenerator.DEFAULT)
}
