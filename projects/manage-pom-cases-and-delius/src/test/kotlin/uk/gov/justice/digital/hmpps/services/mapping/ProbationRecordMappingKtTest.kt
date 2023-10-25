package uk.gov.justice.digital.hmpps.services.mapping

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.api.model.Resourcing
import uk.gov.justice.digital.hmpps.api.model.Team
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate

internal class ProbationRecordMappingKtTest {

    @Test
    fun `record mapping`() {
        val person = PersonGenerator.generate("J123456", "J00243U")
        PersonManagerGenerator.generate(person = person).also { person.set("managers", listOf(it)) }

        val personRecord = person.record(null, null, false)
        assertNull(personRecord.currentTier)
        assertNull(personRecord.resourcing)
        assertThat(personRecord.mappaLevel, equalTo(0))
    }

    @ParameterizedTest
    @MethodSource("resourceMapping")
    fun `resourcing mapping`(code: String?, result: Resourcing?) {
        val decision = code?.let { ReferenceDataGenerator.generate(it) }
        assertThat(decision.resourcing(), equalTo(result))
    }

    @Test
    fun `team mapping handles null district`() {
        assertThat(
            ProviderGenerator.generateTeam("TEST", district = null).forManager(),
            equalTo(Team("TEST", "Team TEST", null))
        )
    }

    @Test
    fun `unallocated om provides null staff details`() {
        val unallocated = Staff("N07UATU", "Unallocated", "Staff", probationAreaId = 1, id = 99)
        val om = PersonManager(
            PersonGenerator.DEFAULT,
            ProviderGenerator.DEFAULT_TEAM,
            unallocated,
            ProviderGenerator.DEFAULT_PROVIDER.id,
            id = 99
        )
        assertNull(om.manager().code)
        assertNull(om.manager().name)
        assertNull(om.manager().email)
    }

    @ParameterizedTest
    @MethodSource("levelMapping")
    fun `level mapping`(code: String?, level: Int) {
        val registration = code?.let {
            val rLevel = ReferenceDataGenerator.generate(it)
            RegistrationGenerator.generate(RegistrationGenerator.TYPE_MAPPA, rLevel, LocalDate.now())
        }
        assertThat(registration.level(), equalTo(level))
    }

    companion object {
        @JvmStatic
        fun resourceMapping() = listOf(
            Arguments.of(null, null),
            Arguments.of("R", Resourcing.ENHANCED),
            Arguments.of("A", Resourcing.NORMAL),
            Arguments.of("N", null)
        )

        @JvmStatic
        fun levelMapping() = listOf(
            Arguments.of(null, 0),
            Arguments.of("M0", 0),
            Arguments.of("M1", 1),
            Arguments.of("M2", 2),
            Arguments.of("M3", 3),
            Arguments.of("T3", 0)
        )
    }
}
