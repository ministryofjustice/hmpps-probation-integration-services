package uk.gov.justice.digital.hmpps.datetime

import com.fasterxml.jackson.core.JsonParser
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class DateSerializerTest {

    @Mock
    private lateinit var jsonParser: JsonParser

    private val deserializer = ZonedDateTimeDeserializer()

    @ParameterizedTest
    @MethodSource("dateTimeStrings")
    fun `deserialize zoned date time`(dateTime: String, zonedDateTime: ZonedDateTime) {
        whenever(jsonParser.text).thenReturn(dateTime)

        val result = deserializer.deserialize(jsonParser, null)
        MatcherAssert.assertThat(result, Matchers.equalTo(zonedDateTime))
    }

    companion object {
        private val localDate = LocalDate.of(2022, 6, 23)
        private val localTime = LocalTime.of(23, 10, 47)
        private val dateTime = LocalDateTime.of(localDate, localTime).atZone(ZoneId.systemDefault())

        @JvmStatic
        private fun dateTimeStrings(): List<Arguments> = listOf(
            Arguments.of("2022-06-23T23:10:47", dateTime),
            Arguments.of("2022-06-23T23:10:47+01:00", dateTime),
            Arguments.of("2022-06-23T23:10:47+01:00[Europe/London]", dateTime)
        )
    }
}
