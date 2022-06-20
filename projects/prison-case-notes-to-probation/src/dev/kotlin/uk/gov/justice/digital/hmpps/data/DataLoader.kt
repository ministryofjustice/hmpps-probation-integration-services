package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseNoteNomisTypeGenerator
import uk.gov.justice.digital.hmpps.data.repository.CaseNoteTypeRepository
import uk.gov.justice.digital.hmpps.data.generator.OffenderGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val userRepository: UserRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val caseNoteNomisTypeRepository: CaseNoteNomisTypeRepository,
    private val caseNoteRepository: CaseNoteRepository,
    private val offenderRepository: OffenderRepository
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        caseNoteTypeRepository.save(CaseNoteNomisTypeGenerator.DEFAULT.type)
        caseNoteNomisTypeRepository.save(CaseNoteNomisTypeGenerator.DEFAULT)
        offenderRepository.save(OffenderGenerator.DEFAULT)
        caseNoteRepository.save(CaseNoteGenerator.EXISTING)
    }
}