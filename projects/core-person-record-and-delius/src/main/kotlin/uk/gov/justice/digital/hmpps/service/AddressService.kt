package uk.gov.justice.digital.hmpps.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode.*
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.client.PersonClient
import uk.gov.justice.digital.hmpps.client.model.CanonicalAddress
import uk.gov.justice.digital.hmpps.client.model.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.client.model.CanonicalAddressUsage
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

    private fun CanonicalAddress.toEntity(personId: Long, existing: PersonAddress? = null) =
        existing?.also { existing ->
            existing.addressNumber = buildingNumber
            existing.buildingName = listOfNotNull(subBuildingName, buildingName).joinToString(" ").truncate()
            existing.streetName = thoroughfareName?.truncate()
            existing.townCity = postTown
            existing.county = county
            existing.district = dependentLocality?.truncate()
            existing.postcode = postcode
            existing.uprn = uprn?.toLongOrNull()
            existing.noFixedAbode = noFixedAbode ?: false
            existing.notes = listOfNotNull(existing.notes, comment).takeIf { it.isNotEmpty() }?.joinToString("\n\n")
            existing.startDate = startDateTime
            existing.endDate = endDateTime
            existing.status = status.toEntity()
            existing.type = usages.toEntity()
            existing.typeVerified = typeVerified
        } ?: PersonAddress(
            personId = personId,
            addressNumber = buildingNumber,
            buildingName = listOfNotNull(subBuildingName, buildingName).joinToString(" ").truncate(),
            streetName = thoroughfareName?.truncate(),
            townCity = postTown,
            county = county,
            district = dependentLocality?.truncate(),
            postcode = postcode,
            telephoneNumber = null,
            uprn = uprn?.toLongOrNull(),
            noFixedAbode = noFixedAbode ?: false,
            notes = comment?.takeIf { it.isNotEmpty() },
            startDate = startDateTime,
            endDate = endDateTime,
            status = status.toEntity(),
            type = usages.toEntity(),
            typeVerified = typeVerified,
        )

    private fun CanonicalAddressStatus.toEntity() =
        requireNotNull(code?.let { referenceDataRepository.getAddressStatus(it) }) { "Address status is required" }

    private fun List<CanonicalAddressUsage>.toEntity() =
        filter { it.isActive }.apply { require(size <= 1) { "Cannot handle multiple address types" } }
            .singleOrNull()?.usageCode?.code?.let { referenceDataRepository.getAddressType(it) }

    private fun String.truncate(predicate: String.() -> Boolean = { length in 36..80 }) =
        if (predicate()) "${substring(0, 34)}…" else this

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
