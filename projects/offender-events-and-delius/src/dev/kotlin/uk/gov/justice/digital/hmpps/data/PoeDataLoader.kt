package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.offender.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.offender.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.DatasetRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class PoeDataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val offenderRepository: OffenderRepository,
    private val contactRepository: ContactRepository,
    private val registrationRepository: RegistrationRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val datasetRepository: DatasetRepository,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        val offender = offenderRepository.save(OffenderGenerator.DEFAULT)
        contactRepository.save(Contact(id = 101))
        contactRepository.save(Contact(id = 102, softDeleted = true))
        contactRepository.save(Contact(id = 201, visorExported = true, softDeleted = true))

        val contact202 = contactRepository.save(Contact(id = 202, visorExported = true))

        datasetRepository.save(DatasetGenerator.DOMAIN_EVENT_TYPE)
        referenceDataRepository.saveAll(
            listOf(
                DomainEventTypeGenerator.MAPPA_UPDATED,
                DomainEventTypeGenerator.MAPPA_DELETED
            )
        )

        val category = referenceDataRepository.findByCode(
            code = "M1",
            datasetCode = "MAPPA CATEGORY"
        )

        if (category == null) {
            return
        }

        val type = RegisterTypeGenerator.MAPPA

        registrationRepository.save(
            RegistrationGenerator.mappaRegistration(
                offenderId = offender.id,
                contact = contact202,
                type = type,
                category = category
            )
        )
    }
}
