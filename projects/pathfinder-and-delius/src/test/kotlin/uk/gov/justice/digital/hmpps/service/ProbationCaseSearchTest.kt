package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.RegisterType
import uk.gov.justice.digital.hmpps.entity.Registration
import java.time.LocalDate
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class ProbationCaseSearchTest {
    @ParameterizedTest
    @MethodSource("mappaDetails")
    fun `mappa details mapping`(
        levelCode: String?,
        expectedLevel: Int,
        expectedLevelDescription: String,
        categoryCode: String?,
        expectedCategory: Int,
        expectedCategoryDescription: String
    ) {
        val registration = Registration(
            id = id(),
            person = DetailsGenerator.PERSON,
            type = RegisterType(id(), "MAPP", "Description of MAPP"),
            level = levelCode?.let { ReferenceData(id(), it, "Description of $it") },
            category = categoryCode?.let { ReferenceData(id(), it, "Description of $it") },
            date = LocalDate.now(),
            nextReviewDate = LocalDate.now().plusYears(1),
            team = TEAM,
            staff = DetailsGenerator.STAFF,
            notes = "Some notes",
        )

        val mappaDetails = registration.asMappaDetails()

        assertEquals(expectedLevel, mappaDetails.level)
        assertEquals(expectedLevelDescription, mappaDetails.levelDescription)
        assertEquals(expectedCategory, mappaDetails.category)
        assertEquals(expectedCategoryDescription, mappaDetails.categoryDescription)
    }

    companion object {
        @JvmStatic
        fun mappaDetails(): Stream<Arguments> = Stream.of(
            Arguments.of("M1", 1, "Description of M1", "M1", 1, "Description of M1"),
            Arguments.of("M2", 2, "Description of M2", "M2", 2, "Description of M2"),
            Arguments.of("M3", 3, "Description of M3", "M3", 3, "Description of M3"),
            Arguments.of("M9", 0, "Description of M9", "M4", 4, "Description of M4"),
            Arguments.of(null, 0, "Missing Level", "M9", 0, "Description of M9"),
            Arguments.of("M1", 1, "Description of M1", null, 0, "Missing category")
        )
    }
}