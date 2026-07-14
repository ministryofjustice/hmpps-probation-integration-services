package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Answers
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator.TIER
import uk.gov.justice.digital.hmpps.exception.NotFoundException

internal class ReferenceDataRepositoryTest {
    private val repository = mock(ReferenceDataRepository::class.java, Answers.CALLS_REAL_METHODS)

    @Test
    fun `getByCodeAndSetName returns reference data when found`() {
        val referenceData = ReferenceDataGenerator.generate("SPB", TIER)
        whenever(repository.findByCodeAndSetName("SPB", "TIER")).thenReturn(referenceData)

        assertThat(repository.getByCodeAndSetName("SPB", "TIER")).isEqualTo(referenceData)
    }

    @Test
    fun `getByCodeAndSetName throws when reference data is missing`() {
        assertThatThrownBy { repository.getByCodeAndSetName("UNKNOWN", "TIER") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("TIER with code of UNKNOWN not found")
    }

    @Test
    fun `getV2Tier looks up v2 tier code`() {
        val referenceData = ReferenceDataGenerator.generate("UD2", TIER)
        whenever(repository.findByCodeAndSetName("UD2", "TIER")).thenReturn(referenceData)

        assertThat(repository.getV2Tier("D2")).isEqualTo(referenceData)
    }

    @ParameterizedTest
    @MethodSource("v3mappings")
    fun `getV3Tier maps tier score to reference data code`(tierScore: String, provisional: Boolean, expected: String) {
        val referenceData = ReferenceDataGenerator.generate(expected, TIER)
        whenever(repository.findByCodeAndSetName(expected, "TIER")).thenReturn(referenceData)

        assertThat(repository.getV3Tier(tierScore, provisional)).isEqualTo(referenceData)
    }

    companion object {
        @JvmStatic
        fun v3mappings() = listOf(
            Arguments.of("MISSING", false, "M"),
            Arguments.of("NOT_SUPERVISED", false, "SPNA"),
            Arguments.of("B", true, "SPBI"),
            Arguments.of("B", false, "SPB"),
        )
    }
}
