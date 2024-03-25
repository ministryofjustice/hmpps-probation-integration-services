package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.EVENT
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.EventRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonManager
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.model.CourtAppearance
import uk.gov.justice.digital.hmpps.model.LengthUnit
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.OffenceCategory
import uk.gov.justice.digital.hmpps.model.Sentence
import uk.gov.justice.digital.hmpps.model.Supervision
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class CaseDetailsServiceTest {
    @Mock
    lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    lateinit var registrationRepository: RegistrationRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var ldapTemplate: LdapTemplate

    @InjectMocks
    lateinit var caseDetailsService: CaseDetailsService

    @Test
    fun `supervisions throws error on missing person`() {
        assertThrows<NotFoundException> { caseDetailsService.getSupervisions("MISSING") }
            .run { assertThat(message, equalTo("Person with crn of MISSING not found")) }
    }

    @Test
    fun `returns supervisions`() {
        whenever(personManagerRepository.findByPersonCrn(PERSON.crn)).thenReturn(DataGenerator.PERSON_MANAGER)
        whenever(eventRepository.findByPersonIdOrderByConvictionDateDesc(PERSON.id)).thenReturn(listOf(EVENT))
        val response = caseDetailsService.getSupervisions(PERSON.crn).supervisions
        assertThat(response, hasSize(1))
        assertThat(
            response[0],
            equalTo(
                Supervision(
                    number = 1,
                    active = true,
                    date = LocalDate.of(2023, 1, 2),
                    sentence = Sentence(
                        description = "ORA Suspended Sentence Order",
                        date = LocalDate.of(2023, 3, 4),
                        length = 6,
                        lengthUnits = LengthUnit.Months,
                        custodial = true
                    ),
                    mainOffence = Offence(
                        date = LocalDate.of(2023, 1, 1),
                        count = 1,
                        code = "12345",
                        description = "Test offence",
                        mainCategory = OffenceCategory("123", "Test"),
                        subCategory = OffenceCategory("45", "offence"),
                        schedule15SexualOffence = true,
                        schedule15ViolentOffence = null
                    ),
                    additionalOffences = listOf(
                        Offence(
                            date = null,
                            count = 3,
                            code = "12345",
                            description = "Test offence",
                            mainCategory = OffenceCategory("123", "Test"),
                            subCategory = OffenceCategory("45", "offence"),
                            schedule15SexualOffence = true,
                            schedule15ViolentOffence = null
                        )
                    ),
                    courtAppearances = listOf(
                        CourtAppearance(
                            type = "Sentence",
                            date = ZonedDateTime.of(LocalDate.of(2023, 2, 3), LocalTime.of(10, 0, 0), EuropeLondon),
                            court = "Manchester Crown Court",
                            plea = "Not guilty"
                        )
                    )
                )
            )
        )
    }
}
