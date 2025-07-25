package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.repository.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nsi.entity.NsiEvent
import uk.gov.justice.digital.hmpps.integrations.delius.nsi.entity.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.assessment.OASYSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.ogrs.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.rsr.RsrScoreHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseEntityRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.registration.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.requirement.RequirementRepository
import java.time.LocalDate

@Component
@ConditionalOnProperty("seed.database")
class DataLoaderForAPI(
    private val caseEntityRepository: CaseEntityRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val referenceDataSetRepository: ReferenceDataSetRepository,
    private val oasysAssessmentRepository: OASYSAssessmentRepository,
    private val ogrsAssessmentRepository: OGRSAssessmentRepository,
    private val eventRepository: EventRepository,
    private val registerRepository: RegistrationRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val disposalRepository: DisposalRepository,
    private val disposalTypeRepository: DisposalTypeRepository,
    private val requirementRepository: RequirementRepository,
    private val requirementMainCategoryRepository: RequirementMainCategoryRepository,
    private val nsiEventRepository: NsiEventRepository,
    private val nsiRepository: NsiRepository,
    private val rsrScoreHistoryRepository: RsrScoreHistoryRepository
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        referenceDataSetRepository.save(ReferenceDataSetGenerator.GENDER)
        referenceDataSetRepository.save(ReferenceDataSetGenerator.REGISTER_LEVEL)
        referenceDataSetRepository.save(ReferenceDataSetGenerator.REGISTER_TYPE_FLAG)
        referenceDataSetRepository.save(ReferenceDataSetGenerator.RSR_TYPE)
        referenceDataRepository.save(ReferenceDataGenerator.GENDER_MALE)
        referenceDataRepository.save(ReferenceDataGenerator.TIER_ONE)
        referenceDataRepository.save(ReferenceDataGenerator.LEVEL_ONE)
        referenceDataRepository.save(ReferenceDataGenerator.FLAG)
        referenceDataRepository.save(ReferenceDataGenerator.STATIC_RSR)
        referenceDataRepository.save(ReferenceDataGenerator.DYNAMIC_RSR)
        registerTypeRepository.save(RegisterTypeGenerator.DEFAULT)

        referenceDataSetRepository.save(ReferenceDataSetGenerator.NSI_OUTCOME)
        referenceDataRepository.saveAll(ReferenceDataGenerator.ENFORCEMENT_OUTCOMES)

        val case = caseEntityRepository.save(CaseEntityGenerator.DEFAULT)
        val event = eventRepository.save(EventGenerator.DEFAULT)
        disposalTypeRepository.save(DisposalTypeGenerator.DEFAULT)
        disposalRepository.saveAll(listOf(DisposalGenerator.DEFAULT))
        requirementMainCategoryRepository.save(RequirementGenerator.RequirementMainCategoryGenerator.DEFAULT)
        requirementRepository.save(RequirementGenerator.DEFAULT)
        ogrsAssessmentRepository.save(OgrsAssessmentGenerator.DEFAULT)
        oasysAssessmentRepository.save(OasysAssessmentGenerator.DEFAULT)
        registerRepository.save(RegistrationGenerator.DEFAULT)
        rsrScoreHistoryRepository.saveAll(RsrScoreHistoryGenerator.HISTORY)

        val nsiEvent = nsiEventRepository.findById(event.id).orElseThrow()
        nsiRepository.save(
            NsiGenerator.generate(
                case.id,
                nsiEvent,
                LocalDate.now().minusDays(360),
                ReferenceDataGenerator.ENFORCEMENT_OUTCOMES.first()
            )
        )
    }
}

interface NsiEventRepository : JpaRepository<NsiEvent, Long>
