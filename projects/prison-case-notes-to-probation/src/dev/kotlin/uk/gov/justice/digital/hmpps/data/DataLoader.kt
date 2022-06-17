package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.repository.CaseNoteTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteNomisType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offender
import uk.gov.justice.digital.hmpps.integrations.delius.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.UserRepository
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicLong

@Component
@Profile("dev")
class DataLoader(
    private val userRepository: UserRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val caseNoteNomisTypeRepository: CaseNoteNomisTypeRepository,
    private val caseNoteRepository: CaseNoteRepository,
    private val offenderRepository: OffenderRepository
) : CommandLineRunner {

    private val idGenerator = AtomicLong(1)
    override fun run(vararg args: String?) {
        val user = userRepository.save(User(idGenerator.getAndIncrement(), "case-notes-to-probation"))
        val caseNoteType = caseNoteTypeRepository.save(
            CaseNoteType(idGenerator.getAndIncrement(), "CaseNote", "A case note from nomis", false)
        )
        caseNoteNomisTypeRepository.save(CaseNoteNomisType("NEG", caseNoteType))

        val offender = Offender(idGenerator.getAndIncrement(), "GA52214")
        offenderRepository.save(offender)

        val now = ZonedDateTime.now()
        caseNoteRepository.save(
            CaseNote(
                idGenerator.getAndIncrement(),
                offender.id,
                12345,
                caseNoteType,
                "A Case Note from Nomis",
                now,
                now,
                now,
                user.id,
                user.id,
                now,
                0
            )
        )
    }
}