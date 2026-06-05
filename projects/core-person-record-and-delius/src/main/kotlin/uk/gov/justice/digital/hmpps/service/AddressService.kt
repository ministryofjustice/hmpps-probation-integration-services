package uk.gov.justice.digital.hmpps.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode.*
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.client.PersonClient
import uk.gov.justice.digital.hmpps.client.model.CanonicalAddress
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integration.delius.entity.AddressRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integration.delius.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
@Transactional
class AddressService(
    auditedInteractionService: AuditedInteractionService,
    private val telemetryService: TelemetryService,
    private val personClient: PersonClient,
    private val personRepository: PersonRepository,
    private val addressRepository: AddressRepository,
    private val referenceDataRepository: ReferenceDataRepository,
) : AuditableService(auditedInteractionService) {
    fun getAddress(id: Long) = addressRepository.findByIdOrNull(id)?.asAddress().orNotFoundBy("id", id)

    fun createAddress(crn: String, cprId: String) = audit(INSERT_ADDRESS) {
        val personId = personRepository.findIdByCrn(crn).orNotFoundBy("crn", crn)
        addressRepository.save(personClient.getAddress(crn, cprId).toEntity(personId))
            .trackEvent("AddressCreated", "crn" to crn, "cprAddressId" to cprId)
    }

    fun updateAddress(crn: String, cprId: String, deliusId: Long) = audit(UPDATE_ADDRESS) {
        val personId = personRepository.findIdByCrn(crn).orNotFoundBy("crn", crn)
        val address = personClient.getAddress(crn, cprId)
        val existing = addressRepository.findByIdOrNull(deliusId).orNotFoundBy("id", deliusId)
        addressRepository.save(address.toEntity(personId, existing))
            .trackEvent("AddressUpdated", "crn" to crn, "cprAddressId" to cprId)
    }

    fun deleteAddress(crn: String, cprId: String, deliusId: Long) = audit(DELETE_ADDRESS) {
        addressRepository.findByIdOrNull(deliusId).orNotFoundBy("id", deliusId)
            .apply { softDeleted = true }
            .trackEvent("AddressDeleted", "crn" to crn, "cprAddressId" to cprId)
    }

    private fun CanonicalAddress.toEntity(personId: Long, existing: PersonAddress? = null) = PersonAddress(
        id = existing?.id,
        version = existing?.version ?: 0,
        personId = personId,
        addressNumber = buildingNumber,
        buildingName = listOfNotNull(subBuildingName, buildingName).joinToString(" "),
        streetName = thoroughfareName,
        townCity = postTown,
        county = null,
        district = dependentLocality,
        postcode = postcode,
        uprn = uprn?.toLongOrNull(),
        telephoneNumber = existing?.telephoneNumber,
        noFixedAbode = noFixedAbode ?: false,
        notes = listOfNotNull(existing?.notes, comment).takeIf { it.isNotEmpty() }?.joinToString("\n\n"),
        startDate = startDate,
        endDate = endDate,
        status = requireNotNull(status.code?.let { referenceDataRepository.getAddressStatus(it) }) { "Address status is required" },
        type = usages.filter { it.isActive }.apply { require(size <= 1) { "Cannot handle multiple address types" } }
            .singleOrNull()?.usageCode?.code?.let { referenceDataRepository.getAddressType(it) },
        typeVerified = typeVerified,
    )

    private fun PersonAddress.trackEvent(name: String, vararg properties: Pair<String, String>) = also {
        telemetryService.trackEvent(
            name, properties.toMap() + mapOf(
                "deliusAddressId" to id.toString(),
                "addressStatus" to status.code,
                "addressType" to type?.code,
                "startDate" to startDate?.toString(),
                "endDate" to endDate?.toString(),
            )
        )
    }
}
