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
    private val addressRepository: AddressRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository,
) {
    fun getPersonDetail(crn: String): PersonDetail = personRepository.getByCrn(crn).withDetail()

    fun getPersonDetail(id: Long): PersonDetail = personRepository.getByPersonId(id).withDetail()

    fun getAllPersonDetails(pageable: Pageable): Page<PersonDetail> =
        personRepository.findAll(pageable).map { it.withDetail() }

    private fun Person.withDetail() = this.detail(
        aliases = aliasRepository.findByPersonId(id).map(Alias::asModel),
        addresses = addressRepository.findAllByPersonIdOrderByStartDateDesc(id).mapNotNull(PersonAddress::asAddress),
        exclusions = exclusionRepository.findByPersonId(id).exclusionsAsLimitedAccess(exclusionMessage),
        restrictions = restrictionRepository.findByPersonId(id).restrictionsAsLimitedAccess(restrictionMessage),
    )
}