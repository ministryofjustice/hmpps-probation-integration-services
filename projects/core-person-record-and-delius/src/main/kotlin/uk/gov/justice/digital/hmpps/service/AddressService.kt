package uk.gov.justice.digital.hmpps.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integration.delius.entity.AddressRepository

@Service
class AddressService(private val addressRepository: AddressRepository) {
    fun getAddress(id: Long) = addressRepository.findByIdOrNull(id)?.asAddress().orNotFoundBy("id", id)
}