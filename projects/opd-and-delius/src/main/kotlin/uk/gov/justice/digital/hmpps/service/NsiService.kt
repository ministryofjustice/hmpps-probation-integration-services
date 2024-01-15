package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.messaging.OpdAssessment

@Transactional
@Service
class NsiService(
    private val nsiRepository: NsiRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiSubTypeRepository: NsiSubTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiManagerRepository: NsiManagerRepository,
    private val contactService: ContactService
) {
    fun findOpdNsi(personId: Long) =
        nsiRepository.findNsiByPersonIdAndTypeCode(personId, NsiType.Code.OPD_COMMUNITY_PATHWAY.value)

    fun createNsi(opdAssessment: OpdAssessment, com: PersonManager) {
        val type = nsiTypeRepository.getByCode(NsiType.Code.OPD_COMMUNITY_PATHWAY.value)
        val subType = opdAssessment.result.subTypeCode?.value?.let { nsiSubTypeRepository.nsiSubType(it) }
        val status = nsiStatusRepository.getByCode(NsiStatus.Code.PENDING_CONSULTATION.value)
        val nsi = Nsi(
            com.person,
            opdAssessment.date.toLocalDate(),
            type,
            subType,
            status,
            opdAssessment.date,
            opdAssessment.date,
            com.providerId
        )
        nsi.appendNotes(opdAssessment.notes)
        nsiRepository.save(nsi)

        nsiManagerRepository.save(
            NsiManager(
                nsi,
                com.providerId,
                com.teamId,
                com.staffId,
                opdAssessment.date
            )
        )
        contactService.createContact(com, status.contactType, nsi.id, opdAssessment)
    }
}
