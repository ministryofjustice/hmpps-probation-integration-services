package uk.gov.justice.digital.hmpps.integrations.delius

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ManagementTierEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OGRSAssessment
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.messaging.OgrsScore
import java.time.LocalDate
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class RiskAssessmentServiceTest {
    @Mock
    private lateinit var eventRepository: EventRepository

    @Mock
    private lateinit var personRepository: PersonRepository

    @Mock
    private lateinit var ogrsAssessmentRepository: OGRSAssessmentRepository

    @Mock
    private lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    private lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    private lateinit var contactRepository: ContactRepository

    @Mock
    private lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    private lateinit var managementTierEventRepository: ManagementTierEventRepository

    @InjectMocks
    private lateinit var riskAssessmentService: RiskAssessmentService

    @Test
    fun `when person not found tracked`() {
        val crn = PersonGenerator.DEFAULT.crn
        whenever(personRepository.findByCrn(crn))
            .thenReturn(null)

        assertThrows<NotFoundException> {
            riskAssessmentService.addOrUpdateRiskAssessment(
                crn,
                1,
                ZonedDateTime.now(),
                OgrsScore(1, 1),
            )
        }
    }

    @Test
    fun `when event not found tracked`() {
        val crn = PersonGenerator.DEFAULT.crn
        whenever(personRepository.findByCrn(crn))
            .thenReturn(PersonGenerator.DEFAULT)

        whenever(eventRepository.findByCrn(crn, "1"))
            .thenReturn(null)

        assertThrows<DeliusValidationError> {
            riskAssessmentService.addOrUpdateRiskAssessment(
                crn,
                1,
                ZonedDateTime.now(),
                OgrsScore(1, 1),
            )
        }
    }

    @Test
    fun `when event not active`() {
        val crn = PersonGenerator.DEFAULT.crn
        whenever(personRepository.findByCrn(crn))
            .thenReturn(PersonGenerator.DEFAULT)

        whenever(eventRepository.findByCrn(crn, "1"))
            .thenReturn(EventGenerator.generate(active = false))

        assertThrows<DeliusValidationError> {
            riskAssessmentService.addOrUpdateRiskAssessment(
                crn,
                1,
                ZonedDateTime.now(),
                OgrsScore(1, 1),
            )
        }
    }

    @Test
    fun `when update assessment with older assessment date nothing happens`() {
        val crn = PersonGenerator.DEFAULT.crn
        val event = EventGenerator.DEFAULT
        whenever(personRepository.findByCrn(crn))
            .thenReturn(PersonGenerator.DEFAULT)

        whenever(eventRepository.findByCrn(crn, "1"))
            .thenReturn(event)

        whenever(ogrsAssessmentRepository.findByEvent(event))
            .thenReturn(OGRSAssessment(1, LocalDate.now().minusYears(1), event, 1, 1, 1))

        riskAssessmentService.addOrUpdateRiskAssessment(
            crn,
            1,
            ZonedDateTime.now().minusYears(2),
            OgrsScore(1, 1),
        )
        verify(ogrsAssessmentRepository, Mockito.times(0)).save(any())
    }
}
