package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.ServiceContext
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteNomisTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.NomisNsiTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.repository.DisposalRepository
import uk.gov.justice.digital.hmpps.data.repository.DisposalTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.InstitutionRepository
import uk.gov.justice.digital.hmpps.data.repository.NomisTypeNsiTypeRepository
import uk.gov.justice.digital.hmpps.data.repository.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val caseNoteNomisTypeRepository: CaseNoteNomisTypeRepository,
    private val probationAreaRepository: ProbationAreaRepository,
    private val institutionRepository: InstitutionRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val offenderRepository: OffenderRepository,
    private val disposalTypeRepository: DisposalTypeRepository,
    private val eventRepository: EventRepository,
    private val disposalRepository: DisposalRepository,
    private val caseNoteRepository: CaseNoteRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nomisTypeNsiTypeRepository: NomisTypeNsiTypeRepository,
    private val nsiRepository: NsiRepository
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.servicePrincipal =
            ServicePrincipal(UserGenerator.APPLICATION_USER.username, UserGenerator.APPLICATION_USER.id)

        businessInteractionRepository.save(BusinessInteractionGenerator.CASE_NOTES_MERGE)

        caseNoteTypeRepository.save(CaseNoteTypeGenerator.DEFAULT)
        caseNoteTypeRepository.save(CaseNoteNomisTypeGenerator.NEG.type)
        caseNoteNomisTypeRepository.save(CaseNoteNomisTypeGenerator.NEG)

        institutionRepository.save(ProbationAreaGenerator.DEFAULT.institution!!)
        probationAreaRepository.save(ProbationAreaGenerator.DEFAULT)
        teamRepository.save(TeamGenerator.DEFAULT)
        StaffGenerator.DEFAULT = staffRepository.save(StaffGenerator.DEFAULT)

        offenderRepository.save(OffenderGenerator.DEFAULT)

        eventRepository.save(EventGenerator.CUSTODIAL_EVENT)
        disposalTypeRepository.save(EventGenerator.CUSTODIAL_EVENT.disposal!!.disposalType)
        disposalRepository.save(EventGenerator.CUSTODIAL_EVENT.disposal!!)

        nsiTypeRepository.save(NsiGenerator.EVENT_CASE_NOTE_NSI.type)
        nomisTypeNsiTypeRepository.save(NomisNsiTypeGenerator.DEFAULT)
        nsiRepository.save(NsiGenerator.EVENT_CASE_NOTE_NSI)

        CaseNoteGenerator.EXISTING = caseNoteRepository.save(CaseNoteGenerator.EXISTING)
    }
}
