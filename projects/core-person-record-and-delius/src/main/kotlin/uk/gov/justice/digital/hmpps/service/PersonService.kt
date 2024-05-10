package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.PersonDetail
import uk.gov.justice.digital.hmpps.integration.delius.entity.*

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val aliasRepository: AliasRepository,
    private val addressRepository: AddressRepository
) {
    fun getPersonDetail(crn: String): PersonDetail = personRepository.getByCrn(crn).withDetail()

    fun getPersonDetail(id: Long): PersonDetail = personRepository.getByPersonId(id).withDetail()

    fun getAllPersonDetails(pageable: Pageable): Page<PersonDetail> =
        personRepository.findAll(pageable).map { it.withDetail() }

    private fun Person.withDetail(): PersonDetail =
        detail(
            aliasRepository.findByPersonId(id).map(Alias::asModel),
            addressRepository.mainAddresses(id).mapNotNull(PersonAddress::asAddress)
        )
}