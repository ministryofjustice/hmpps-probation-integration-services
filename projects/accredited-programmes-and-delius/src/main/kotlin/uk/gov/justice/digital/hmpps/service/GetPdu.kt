package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.CodedValue
import uk.gov.justice.digital.hmpps.model.PduOfficeLocations
import uk.gov.justice.digital.hmpps.repository.OfficeLocationRepository
import uk.gov.justice.digital.hmpps.repository.PduRepository

@Service
class GetPdu(
    private val officeLocationRepository: OfficeLocationRepository,
    private val pduRepository: PduRepository,
) {

    fun pduOfficeLocations(code: String): PduOfficeLocations {
        val pdu = pduRepository.getByCode(code)!!
        return PduOfficeLocations(
            pdu.code, pdu.description,
            officeLocationRepository.findByPduCode(code)
                .map { CodedValue(it.code, it.description) })
    }
}