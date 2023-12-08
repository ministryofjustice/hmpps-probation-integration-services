package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiManager

@Service
class NsiManagerService(
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val nsiManagerRepository: NsiManagerRepository,
) {
    fun createNewManager(nsi: Nsi) =
        nsiManagerRepository.save(
            NsiManager(
                nsi,
                nsi.intendedProviderId!!,
                teamRepository.getByCode(Team.INTENDED_TEAM_CODE).id,
                staffRepository.getByCode(Staff.INTENDED_STAFF_CODE).id,
                nsi.statusDate,
            ),
        )
}
