package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByPersonIdAndEventId
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.getCrsProvider
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.RequirementMainCategory
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
    private val nsiManagerService: NsiManagerService,
) {
    fun new(
        crn: String,
        rs: ReferralStarted,
        additions: (nsi: Nsi) -> Unit,
    ): Nsi? {
        val person = personRepository.getByCrn(crn)
        val sentence = disposalRepository.getByPersonIdAndEventId(person.id, rs.sentenceId)
        val req =
            requirementRepository.findForPersonAndEvent(
                person.id,
                sentence.id,
                RequirementMainCategory.Code.REHAB_ACTIVITY_TYPE.value,
            ).firstOrNull()

        val nsiTypeCode =
            ContractTypeNsiType.MAPPING[rs.contractType]
                ?: throw IllegalArgumentException("Unexpected Contract Type")
        val type = nsiTypeRepository.getByCode(nsiTypeCode)
        val status = nsiStatusRepository.getByCode(NsiStatus.Code.IN_PROGRESS.value)
        val nsi =
            nsiRepository.save(
                Nsi(
                    person = person,
                    intendedProviderId = providerRepository.getCrsProvider().id,
                    type = type,
                    eventId = sentence.event.id,
                    requirementId = req?.id,
                    referralDate = rs.startedAt.toLocalDate(),
                    actualStartDate = rs.startedAt,
                    status = status,
                    statusDate = rs.startedAt,
                    externalReference = rs.urn,
                    notes = rs.notes,
                ),
            )
        val manager = nsiManagerService.createNewManager(nsi)
        additions(nsi.withManager(manager))
        return nsiRepository.findByPersonCrnAndExternalReference(crn, rs.urn)
    }
}
