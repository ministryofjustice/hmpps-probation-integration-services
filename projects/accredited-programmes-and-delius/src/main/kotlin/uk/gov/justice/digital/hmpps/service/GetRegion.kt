package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.CodedValue
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.PduOfficeLocations
import uk.gov.justice.digital.hmpps.model.RegionWithMembers
import uk.gov.justice.digital.hmpps.repository.OfficeLocationRepository
import uk.gov.justice.digital.hmpps.repository.PduRepository
import uk.gov.justice.digital.hmpps.repository.RegionMember
import uk.gov.justice.digital.hmpps.repository.StaffRepository

@Service
class GetRegion(
    private val staffRepository: StaffRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val pduRepository: PduRepository,
) {
    fun withMembers(code: String): RegionWithMembers =
        staffRepository.findRegionMembers(code)
            .takeIf { it.isNotEmpty() }
            ?.groupBy { it.regionCode to it.regionDescription }
            ?.map {
                RegionWithMembers(
                    it.key.first,
                    it.key.second,
                    pdus = it.value.asPdus()
                )
            }?.single() ?: throw NotFoundException("No members found for region")

    fun pduOfficeLocations(code: String): PduOfficeLocations {
        val pdu = pduRepository.getByCode(code)!!
        return PduOfficeLocations(
            pdu.code, pdu.description,
            officeLocationRepository.findByPduCode(code)
                .map { CodedValue(it.code, it.description) })
    }
}

private fun List<RegionMember>.asPdus() =
    groupBy { it.pduCode to it.pduDescription }
        .map {
            RegionWithMembers.Pdu(
                it.key.first,
                it.key.second,
                it.value.asTeams()
            )
        }

private fun List<RegionMember>.asTeams() =
    groupBy { it.teamCode to it.teamDescription }
        .map {
            RegionWithMembers.Team(
                it.key.first,
                it.key.second,
                it.value.asMembers()
            )
        }

private fun List<RegionMember>.asMembers() = map {
    RegionWithMembers.Team.Member(it.staffCode, Name(it.forename, it.surname))
}