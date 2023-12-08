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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(MockitoExtension::class)
class DateSerializerTest {
    @Mock
    private lateinit var jsonParser: JsonParser

    private val deserializer = ZonedDateTimeDeserializer()

    @ParameterizedTest
    @MethodSource("dateTimeStrings")
    fun `deserialize zoned date time`(
        dateTime: String,
        zonedDateTime: ZonedDateTime,
    ) {
        whenever(jsonParser.text).thenReturn(dateTime)

        val result = deserializer.deserialize(jsonParser, null)
        MatcherAssert.assertThat(result, Matchers.equalTo(zonedDateTime))
    }

    companion object {
        private val bstLocalDate = LocalDate.of(2022, 6, 23)
        private val bstLocalTime = LocalTime.of(23, 10, 47)
        private val bstLocalDateTime = LocalDateTime.of(bstLocalDate, bstLocalTime)
        private val bstDateTime = bstLocalDateTime.atZone(EuropeLondon)

        private val utcLocalDate = LocalDate.of(2022, 2, 23)
        private val utcLocalTime = LocalTime.of(23, 10, 47)
        private val utcLocalDateTime = LocalDateTime.of(utcLocalDate, utcLocalTime)
        private val utcDateTime = utcLocalDateTime.atZone(EuropeLondon)

        @JvmStatic
        private fun dateTimeStrings(): List<Arguments> =
            listOf(
                Arguments.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(bstDateTime), bstDateTime),
                Arguments.of(DateTimeFormatter.ISO_INSTANT.format(bstDateTime), bstDateTime),
                Arguments.of(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(bstDateTime), bstDateTime),
                Arguments.of(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(bstDateTime), bstDateTime),
                Arguments.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(utcDateTime), utcDateTime),
                Arguments.of(DateTimeFormatter.ISO_INSTANT.format(utcDateTime), utcDateTime),
                Arguments.of(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(utcDateTime), utcDateTime),
                Arguments.of(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(utcDateTime), utcDateTime),
            )
    }
}
