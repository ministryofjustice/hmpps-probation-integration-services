package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.OffenderDeltaGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.OffenderDeltaPoller
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDelta
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest @Autowired constructor(
    private val offenderDeltaPoller: OffenderDeltaPoller,
    private val offenderDeltaRepository: OffenderDeltaRepository
) {
    @MockitoSpyBean
    lateinit var offenderDeltaService: OffenderDeltaService

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @ParameterizedTest
    @MethodSource("deltas")
    fun `offender delta test`(delta: OffenderDelta, expected: List<Map<String, String>>) {
        // given
        offenderDeltaRepository.save(delta)

        // when
        offenderDeltaPoller.poll()
        assertThat(offenderDeltaRepository.count()).isEqualTo(0L)

        // then
        if (expected.isNotEmpty()) {
            val propertiesCaptor = argumentCaptor<Map<String, String>>()

            verify(telemetryService, atLeast(expected.size)).trackEvent(
                eq("OffenderEventPublished"),
                propertiesCaptor.capture(),
                any()
            )

            assertThat(propertiesCaptor.allValues.map { it["eventType"] }).containsExactlyInAnyOrderElementsOf(expected.map { it["eventType"] })
            assertThat(propertiesCaptor.allValues.map { it["crn"] }).containsExactlyInAnyOrderElementsOf(expected.map { it["crn"] })
        } else {
            verify(telemetryService, never()).trackEvent(any(), any(), any())
        }
    }

    companion object {
        private val properties = mapOf("crn" to "X123456")

        @JvmStatic
        private fun deltas() = listOf(
            Arguments.of(
                OffenderDeltaGenerator.generate(),
                listOf(
                    properties + ("eventType" to "OFFENDER_CHANGED"),
                    properties + ("eventType" to "OFFENDER_DETAILS_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "ALIAS", sourceId = id()),
                listOf(
                    properties + ("eventType" to "OFFENDER_CHANGED"),
                    properties + ("eventType" to "OFFENDER_ALIAS_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "DEREGISTRATION", sourceId = id()),
                listOf(
                    properties + ("eventType" to "OFFENDER_REGISTRATION_DEREGISTERED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "DISPOSAL", sourceId = id()),
                listOf(
                    properties + ("eventType" to "SENTENCE_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "EVENT", sourceId = id()),
                listOf(
                    properties + ("eventType" to "CONVICTION_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "MANAGEMENT_TIER_EVENT", sourceId = id()),
                emptyList<Map<String, String>>()
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "MERGE_HISTORY", sourceId = id()),
                listOf(
                    properties + ("eventType" to "OFFENDER_MERGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "OFFENDER_MANAGER", sourceId = id()),
                listOf(
                    properties + ("eventType" to "OFFENDER_CHANGED"),
                    properties + ("eventType" to "OFFENDER_MANAGER_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "OFFICER", sourceId = id()),
                listOf(
                    properties + ("eventType" to "OFFENDER_CHANGED"),
                    properties + ("eventType" to "OFFENDER_OFFICER_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "OGRS_ASSESSMENT", sourceId = id()),
                listOf(
                    properties + ("eventType" to "OFFENDER_OGRS_ASSESSMENT_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = id()),
                listOf(
                    properties + ("eventType" to "OFFENDER_REGISTRATION_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "RQMNT", sourceId = id()),
                listOf(
                    properties + ("eventType" to "SENTENCE_ORDER_REQUIREMENT_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = id(), action = "DELETE"),
                listOf(
                    properties + ("eventType" to "OFFENDER_REGISTRATION_DELETED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "CONTACT", sourceId = ContactGenerator.DEFAULT.id),
                listOf(
                    properties + ("eventType" to "CONTACT_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(
                    sourceTable = "CONTACT",
                    sourceId = ContactGenerator.DELETED.id,
                    action = "DELETE"
                ),
                listOf(
                    properties + ("eventType" to "CONTACT_DELETED")
                )
            ),
        )
    }
}
