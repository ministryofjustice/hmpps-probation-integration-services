package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Disposal
import uk.gov.justice.digital.hmpps.entity.SupervisionPackage
import java.time.ZonedDateTime

object SupervisionPackageGenerator {
    private val START = ZonedDateTime.parse("2026-01-01T10:00:00Z")

    val SINGLE_PACKAGE = generateHistory("S123451", listOf(1))
    val UNCHANGED_TYPE = generateHistory("S123452", listOf(1, 1, 1))
    val CHANGED_TYPE = generateHistory("S123453", listOf(1, 2))
    val UNCHANGED_AFTER_CHANGE = generateHistory("S123454", listOf(1, 2, 2))
    val RETURNED_TO_ORIGINAL_TYPE = generateHistory("S123455", listOf(1, 2, 1))
    val UNCHANGED_AFTER_RETURN = generateHistory("S123456", listOf(1, 2, 1, 1))

    val MULTIPLE_DISPOSALS_PERSON = PersonGenerator.generate(crn = "S234567")
    val FIRST_EVENT = EventGenerator.generate(MULTIPLE_DISPOSALS_PERSON)
    val SECOND_EVENT = EventGenerator.generate(MULTIPLE_DISPOSALS_PERSON)
    val MULTIPLE_DISPOSALS = listOf(FIRST_EVENT, SECOND_EVENT).flatMap { event ->
        val disposal = DisposalGenerator.generate(event, START.toLocalDate())
        listOf(
            generate(disposal, packageType = 1),
            generate(disposal, packageType = 2, start = START.plusDays(1))
        )
    }
    val OTHER_PERSON_PACKAGES = generateHistory("S654321", listOf(1, 2))

    fun generate(
        disposal: Disposal,
        packageType: Long = 1,
        start: ZonedDateTime = START,
        lastUpdated: ZonedDateTime = START.plusMonths(1),
        id: Long = IdGenerator.getAndIncrement()
    ) = SupervisionPackage(
        id = id,
        person = disposal.event.person,
        disposal = disposal,
        packageType = packageType,
        supervisionPackageStart = start,
        supervisionPackageLastUpdated = lastUpdated
    )

    private fun generateHistory(crn: String, types: List<Long>): List<SupervisionPackage> {
        val person = PersonGenerator.generate(crn = crn)
        val event = EventGenerator.generate(person)
        val disposal = DisposalGenerator.generate(event, START.toLocalDate())
        return types.mapIndexed { index, type ->
            generate(disposal, type, START.plusDays(index.toLong()))
        }.reversed()
    }
}
