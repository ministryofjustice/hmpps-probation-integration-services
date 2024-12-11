package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferralGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Referral
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ResidenceRepository
import uk.gov.justice.digital.hmpps.model.ApReferral
import uk.gov.justice.digital.hmpps.model.ApprovedPremises
import uk.gov.justice.digital.hmpps.model.ExistingReferrals
import uk.gov.justice.digital.hmpps.model.ReferralDetail
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReferralControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var referralRepository: ReferralRepository

    @Autowired
    lateinit var residenceRepository: ResidenceRepository

    @Test
    fun `existing referrals for a crn are returned successfully`() {
        referralRepository.save(ReferralGenerator.EXISTING_REFERRAL)
        val person = PersonGenerator.DEFAULT
        val res = mockMvc
            .perform(get("/probation-case/${person.crn}/referrals").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<ExistingReferrals>()

        assertThat(res.referrals.size, equalTo(1))
    }

    @ParameterizedTest()
    @MethodSource("bookingDetails")
    fun `referral detail is returned correctly`(bookingId: String, detail: ReferralDetail, loadData: Boolean) {
        if (loadData) {
            referralRepository.save(ReferralGenerator.BOOKING_WITHOUT_ARRIVAL)
            ReferralGenerator.BOOKING_ARRIVED = referralRepository.save(ReferralGenerator.BOOKING_ARRIVED)
            ReferralGenerator.BOOKING_DEPARTED = referralRepository.save(ReferralGenerator.BOOKING_DEPARTED)
            ReferralGenerator.ARRIVAL = residenceRepository.save(
                ReferralGenerator.generateResidence(
                    PersonGenerator.PERSON_WITH_BOOKING,
                    ReferralGenerator.BOOKING_ARRIVED,
                    arrivalDateTime = ReferralGenerator.ARRIVAL.arrivalDate,
                )
            )
            ReferralGenerator.DEPARTURE = residenceRepository.save(
                ReferralGenerator.generateResidence(
                    PersonGenerator.PERSON_WITH_BOOKING,
                    ReferralGenerator.BOOKING_DEPARTED,
                    arrivalDateTime = ReferralGenerator.DEPARTURE.arrivalDate,
                    departureDateTime = ReferralGenerator.DEPARTURE.departureDate
                )
            )
        }

        val person = PersonGenerator.PERSON_WITH_BOOKING
        val res = mockMvc
            .perform(get("/probation-case/${person.crn}/referrals/$bookingId").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<ReferralDetail>()

        assertThat(res, equalTo(detail))
    }

    companion object {
        private fun Referral.detail(arrivalDateTime: ZonedDateTime? = null, departureDateTime: ZonedDateTime? = null) =
            ReferralDetail(
                ApReferral(
                    referralDate,
                    expectedArrivalDate,
                    expectedDepartureDate,
                    decisionDate,
                    ApprovedPremises(ApprovedPremisesGenerator.DEFAULT.code.description)
                ),
                arrivedAt = arrivalDateTime,
                departedAt = departureDateTime
            )

        @JvmStatic
        fun bookingDetails() = listOf(
            Arguments.of(ReferralGenerator.BOOKING_ID, ReferralGenerator.BOOKING_WITHOUT_ARRIVAL.detail(), true),
            Arguments.of(
                ReferralGenerator.ARRIVED_ID,
                ReferralGenerator.BOOKING_ARRIVED.detail(
                    ReferralGenerator.ARRIVAL.arrivalDate
                ), false
            ),
            Arguments.of(
                ReferralGenerator.DEPARTED_ID,
                ReferralGenerator.BOOKING_DEPARTED.detail(
                    ReferralGenerator.DEPARTURE.arrivalDate,
                    ReferralGenerator.DEPARTURE.departureDate
                ), false
            )
        )
    }
}
