package uk.gov.justice.digital.hmpps.audit.converter

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction

internal class AuditedInteractionParametersConverterTest {
    private val converter = AuditedInteractionParamsConverter()

    @ParameterizedTest
    @MethodSource("paramToDb")
    fun convertToDatabaseColumn(
        params: AuditedInteraction.Parameters,
        column: String,
    ) {
        val res = converter.convertToDatabaseColumn(params)
        assertThat(res, equalTo(column))
    }

    @ParameterizedTest
    @MethodSource("dbToParam")
    fun convertToEntityAttribute(
        params: AuditedInteraction.Parameters,
        column: String?,
    ) {
        val res = converter.convertToEntityAttribute(column)
        assertThat(res, equalTo(params))
    }

    companion object {
        @JvmStatic
        fun paramToDb(): List<Arguments> =
            listOf(
                Arguments.of(AuditedInteraction.Parameters("param" to "value"), "param='value'"),
                Arguments.of(
                    AuditedInteraction.Parameters(
                        "param1" to "value1",
                        "param2" to "value2",
                        "param3" to "value3",
                    ),
                    "param1='value1',param2='value2',param3='value3'",
                ),
                Arguments.of(AuditedInteraction.Parameters(), ""),
                Arguments.of(AuditedInteraction.Parameters("param" to ""), "param=''"),
            )

        @JvmStatic
        fun dbToParam() =
            paramToDb() +
                listOf(
                    Arguments.of(AuditedInteraction.Parameters(), "="),
                    Arguments.of(AuditedInteraction.Parameters(), null),
                )
    }
}
