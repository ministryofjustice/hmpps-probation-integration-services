package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.PersonDetail
import uk.gov.justice.digital.hmpps.integration.delius.entity.*

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val additionalIdentifierRepository: AdditionalIdentifierRepository,
    private val aliasRepository: AliasRepository,
    private val addressRepository: AddressRepository,
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository,
    private val disposalRepository: DisposalRepository,
    private val religionHistoryRepository: ReligionHistoryRepository
) {
    fun getPersonDetail(crn: String): PersonDetail = personRepository.getByCrn(crn).withDetail()

    fun getPersonDetail(id: Long): PersonDetail = personRepository.getByPersonId(id).withDetail()

    fun getAllPersonDetails(pageable: Pageable) = personRepository.findAll(pageable).withDetail()

    private fun Person.withDetail(
        aliases: List<Alias> = aliasRepository.findByPersonId(id),
        addresses: List<PersonAddress> = addressRepository.findAllByPersonIdOrderByStartDateDesc(id),
        exclusions: List<Exclusion> = exclusionRepository.findByPersonId(id),
        restrictions: List<Restriction> = restrictionRepository.findByPersonId(id),
        sentences: List<Disposal> = disposalRepository.findByPersonId(id),
        additionalIdentifiers: List<AdditionalIdentifier> = additionalIdentifierRepository.findByPersonId(id),
        religionHistory: List<ReligionHistory> = religionHistoryRepository.findAllByPersonId(id),
    ) = this.detail(
        aliases = aliases.map(Alias::asModel),
        addresses = addresses.mapNotNull(PersonAddress::asAddress),
        exclusions = exclusions.exclusionsAsLimitedAccess(exclusionMessage),
        restrictions = restrictions.restrictionsAsLimitedAccess(restrictionMessage),
        sentences = sentences.map(Disposal::asModel),
        additionalIdentifiers = additionalIdentifiers.map(AdditionalIdentifier::asModel),
        religionHistory = religionHistory.map(ReligionHistory::asModel)
    )

    private fun Page<Person>.withDetail(): Page<PersonDetail> {
        val ids = map { it.id }.toSet()
        val aliases = aliasRepository.findByPersonIdIn(ids).groupBy { it.personId }
        val addresses = addressRepository.findAllByPersonIdInOrderByStartDateDesc(ids).groupBy { it.personId }
        val exclusions = exclusionRepository.findByPersonIdIn(ids).groupBy { it.personId }
        val restrictions = restrictionRepository.findByPersonIdIn(ids).groupBy { it.personId }
        val sentences = disposalRepository.findByPersonIdIn(ids).groupBy { it.personId }
        val additionalIdentifiers = additionalIdentifierRepository.findByPersonIdIn(ids).groupBy { it.personId }
        val religionHistory = religionHistoryRepository.findAllByPersonIdIn(ids).groupBy { it.personId }
        return map {
            it.withDetail(
                aliases = aliases[it.id] ?: emptyList(),
                addresses = addresses[it.id] ?: emptyList(),
                exclusions = exclusions[it.id] ?: emptyList(),
                restrictions = restrictions[it.id] ?: emptyList(),
                sentences = sentences[it.id] ?: emptyList(),
                additionalIdentifiers = additionalIdentifiers[it.id] ?: emptyList(),
                religionHistory = religionHistory[it.id] ?: emptyList(),
            )
        }
    }
}