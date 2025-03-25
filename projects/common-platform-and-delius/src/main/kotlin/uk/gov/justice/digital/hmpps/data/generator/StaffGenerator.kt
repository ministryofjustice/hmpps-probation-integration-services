package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team
import java.util.concurrent.atomic.AtomicLong

object StaffGenerator {
    val UNALLOCATED = generate("N07UATU")
    val ALLOCATED = generate("N07T01A")
    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement(),
    ) =
        Staff(code = code, id = id)
}

object ProviderGenerator {
    val DEFAULT = generate()
    fun generate(id: Long = IdGenerator.getAndIncrement()) = Provider(
        id = id,
        selectable = true,
        code = "N07",
        description = "London",
        endDate = null,
    )
}

object TeamGenerator {
    private val teamCodeGenerator = AtomicLong(1)
    val ALLOCATED = generate(code = "N07T01")
    val UNALLOCATED = generate(code = "N07UAT")

    fun generate(
        code: String = "N07${teamCodeGenerator.getAndIncrement().toString().padStart(3, '0')}",
        description: String = "Description of Team $code",
    ) = Team(
        id = IdGenerator.getAndIncrement(),
        code = code,
        description = description,
    )
}
