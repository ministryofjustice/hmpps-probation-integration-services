package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.getCrsProvider
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.RequirementMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.getByPersonIdAndEventId
import uk.gov.justice.digital.hmpps.integrations.delius.referral.getByCode

@Service
class CreateNsi(
    private val personRepository: PersonRepository,
    private val disposalRepository: DisposalRepository,
    private val requirementRepository: RequirementRepository,
    private val providerRepository: ProviderRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiRepository: NsiRepository,
    private val nsiManagerService: NsiManagerService
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun new(crn: String, rs: ReferralStarted): Nsi {
        val person = personRepository.getByCrn(crn)
        val sentence = disposalRepository.getByPersonIdAndEventId(person.id, rs.sentenceId)
        val req = requirementRepository.findForPersonAndEvent(
            person.id,
            sentence.id,
            RequirementMainCategory.Code.REHAB_ACTIVITY_TYPE.value
        ).firstOrNull()

        val nsiTypeCode = ContractTypeNsiType.MAPPING[rs.contractType]
            ?: throw IllegalArgumentException("Unexpected Contract Type")
        val type = nsiTypeRepository.getByCode(nsiTypeCode)
        val status = nsiStatusRepository.getByCode(NsiStatus.Code.IN_PROGRESS.value)

        val nsi = nsiRepository.save(
            Nsi(
                person = person,
                intendedProviderId = providerRepository.getCrsProvider().id,
                type = type,
                eventId = sentence.eventId,
                requirementId = req?.id,
                referralDate = rs.startedAt,
                actualStartDate = rs.startedAt,
                status = status,
                statusDate = rs.startedAt,
                externalReference = rs.urn,
                notes = rs.notes
            )
        )
        val manager = nsiManagerService.createNewManager(nsi)
        nsiRepository.findByPersonCrnAndExternalReference(crn, rs.urn)
        return nsi.withManager(manager)
    }
}
