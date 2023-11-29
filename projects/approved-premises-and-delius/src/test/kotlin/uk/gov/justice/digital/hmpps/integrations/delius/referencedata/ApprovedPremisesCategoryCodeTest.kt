package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.BookingMade
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

class ApprovedPremisesCategoryCodeTest {

    @ParameterizedTest
    @MethodSource("bookingsMade")
    fun `the correct category is selected based on the booking made`(
        sentenceTypeString: String,
        releaseTypeString: String,
        situationString: String?,
        approvedPremisesCategoryCode: String
    ) {
        val bookingMade = bookingMade(sentenceTypeString, releaseTypeString, situationString)
        assertThat(
            ApprovedPremisesCategoryCode.from(bookingMade.sentenceType, bookingMade.releaseType).value,
            equalTo(approvedPremisesCategoryCode)
        )
    }

    private fun bookingMade(sentenceTypeString: String, releaseTypeString: String, situationString: String?) =
        BookingMade(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            ZonedDateTime.now(),
            "1",
            mock(),
            mock(),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(7),
            ZonedDateTime.now().minusDays(5),
            sentenceTypeString,
            releaseTypeString,
            situationString
        )

    companion object {

        @JvmStatic
        fun bookingsMade() = listOf(
            Arguments.of("standardDeterminate", "licence", null, "L"),
            Arguments.of("standardDeterminate", "rotl", null, "N"),
            Arguments.of("standardDeterminate", "hdc", null, "H"),
            Arguments.of("standardDeterminate", "pss", null, "U"),
            Arguments.of("life", "rotl", null, "N"),
            Arguments.of("life", "licence", null, "J"),
            Arguments.of("ipp", "rotl", null, "N"),
            Arguments.of("ipp", "licence", null, "K"),
            Arguments.of("extendedDeterminate", "rotl", null, "N"),
            Arguments.of("extendedDeterminate", "licence", null, "Y"),
            Arguments.of("communityOrder", "in_community", "riskManagement", "C"),
            Arguments.of("communityOrder", "in_community", "residencyManagement", "X"),
            Arguments.of("bailPlacement", "in_community", "bailAssessment", "A"),
            Arguments.of("bailPlacement", "in_community", "bailSentence", "B"),
            Arguments.of("nonStatutory", "not_applicable", null, "MAP")
        )
    }
}
