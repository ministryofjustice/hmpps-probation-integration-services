package uk.gov.justice.digital.hmpps.audit.converter

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction

internal class AuditedInteractionOutcomeConverterTest {
    private val converter = AuditedInteractionOutcomeConverter()

    @ParameterizedTest
    @MethodSource("outcomeToDb")
    fun convertToDatabaseColumn(
        outcome: AuditedInteraction.Outcome,
        column: Char,
    ) {
        val res = converter.convertToDatabaseColumn(outcome)
        assertThat(res, equalTo(column))
    }

    @ParameterizedTest
    @MethodSource("outcomeToDb")
    fun convertToEntityAttribute(
        outcome: AuditedInteraction.Outcome,
        column: Char,
    ) {
        val res = converter.convertToEntityAttribute(column)
        assertThat(res, equalTo(outcome))
    }

    @Test
    fun `test unknown outcome from db`() {
        assertThrows<IllegalArgumentException> { converter.convertToEntityAttribute('T') }
    }

    companion object {
        @JvmStatic
        fun outcomeToDb(): List<Arguments> =
            listOf(
                Arguments.of(AuditedInteraction.Outcome.FAIL, 'F'),
                Arguments.of(AuditedInteraction.Outcome.SUCCESS, 'P'),
            )
    }
}
