package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository

@Service
class AddressService(
    auditedInteractionService: AuditedInteractionService,
    private val personAddressRepository: PersonAddressRepository
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun insertAddress(address: PersonAddress): PersonAddress = audit(BusinessInteractionCode.INSERT_ADDRESS) { audit ->
        val savedAddress = personAddressRepository.save(address)
        audit["offenderId"] = address.personId
        savedAddress
    }
}