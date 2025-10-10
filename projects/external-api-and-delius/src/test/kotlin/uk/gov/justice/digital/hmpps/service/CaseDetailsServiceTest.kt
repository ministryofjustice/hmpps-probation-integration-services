package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.EVENT
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON_MANAGER
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.EventRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.KeyDateRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.OgrsAssessmentRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class CaseDetailsServiceTest {
    @Mock
    lateinit var responsibleOfficerRepository: ResponsibleOfficerRepository

    @Mock
    lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    lateinit var personAddressRepository: PersonAddressRepository

    @Mock
    lateinit var registrationRepository: RegistrationRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var courtAppearanceRepository: CourtAppearanceRepository

    @Mock
    lateinit var keyDateRepository: KeyDateRepository

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var ogrsAssessmentRepository: OgrsAssessmentRepository

    @Mock
    lateinit var ldapTemplate: LdapTemplate

    @Mock
    lateinit var limitedAccessService: LimitedAccessService

    @InjectMocks
    lateinit var caseDetailsService: CaseDetailsService

    @Test
    fun `supervisions throws error on missing person`() {
        assertThrows<NotFoundException> { caseDetailsService.getSupervisions("MISSING") }
            .run { assertThat(message, equalTo("Person with crn of MISSING not found")) }
    }

    @Test
    fun `returns crn for nomsid`() {
        val crn = "X000001"
        val nomsId = "A0001DY"
        whenever(personRepository.findByNomsId(nomsId)).thenReturn(crn)
        assertThat(caseDetailsService.getCrnForNomsId(nomsId).crn, equalTo(crn))
    }

    @Test
    fun `returns not found for nomsid`() {
        assertThrows<NotFoundException> { caseDetailsService.getCrnForNomsId("MISSING") }
            .run { assertThat(message, equalTo("Person with nomsId of MISSING not found")) }
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

    @Test
    fun `when active ro is not linked to active manager find com`() {
        val person = PersonGenerator.DEFAULT
        val event = EVENT
        whenever(responsibleOfficerRepository.findByPersonCrn(person.crn)).thenReturn(null)
        whenever(personManagerRepository.findByPersonCrn(person.crn)).thenReturn(PERSON_MANAGER)
        whenever(eventRepository.findByPersonCrnAndNumber(person.crn, event.number)).thenReturn(event)
        whenever(courtAppearanceRepository.findByEventIdOrderByDateDesc(event.id))
            .thenReturn(SentenceGenerator.generateCourtAppearance(event))

        val res = caseDetailsService.getCaseDetails(person.crn, event.number.toInt())
        assertNotNull(res.responsibleProvider)
        assertThat(res.responsibleProvider?.code, equalTo(DEFAULT_PROVIDER.code))
        assertThat(res.responsibleProvider?.name, equalTo(DEFAULT_PROVIDER.description))
    }
}
