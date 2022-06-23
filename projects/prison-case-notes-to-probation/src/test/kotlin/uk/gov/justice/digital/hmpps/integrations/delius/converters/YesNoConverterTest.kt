package uk.gov.justice.digital.hmpps.integrations.delius.converters

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class YesNoConverterTest {
    private val converter = BooleanYesNoConverter()

    @ParameterizedTest
    @MethodSource("databaseColumParams")
    fun `convert to database column`(attribute: Boolean, column: Char) {
        val result = converter.convertToDatabaseColumn(attribute)
        assertThat(result, equalTo(column))
    }

    @ParameterizedTest
    @MethodSource("attributeParams")
    fun `convert from database column`(column: Char?, attribute: Boolean) {
        val result = converter.convertToEntityAttribute(column)
        assertThat(result, equalTo(attribute))
    }

    companion object {
        @JvmStatic
        private fun databaseColumParams(): List<Arguments> = listOf(
            Arguments.of(true, 'Y'),
            Arguments.of(false, 'N')
        )

        @JvmStatic
        private fun attributeParams(): List<Arguments> = listOf(
            Arguments.of('N', false),
            Arguments.of('Y', true),
            Arguments.of(null, false)
        )
    }
}
