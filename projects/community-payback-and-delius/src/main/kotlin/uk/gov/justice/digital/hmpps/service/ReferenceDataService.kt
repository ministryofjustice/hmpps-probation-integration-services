package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType.Code.UNPAID_WORK_APPOINTMENT
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.toCodeDescription
import uk.gov.justice.digital.hmpps.model.CodeDescription
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class ReferenceDataService(
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
) {
    fun getProjectTypes(): List<CodeDescription> =
        referenceDataRepository.findByDatasetCode(Dataset.UPW_PROJECT_TYPE)
            .map { it.toCodeDescription() }

    fun getUpwAppointmentOutcomes(): List<CodeDescription> =
        contactOutcomeRepository.findForTypeCode(UNPAID_WORK_APPOINTMENT.value).map {
            CodeDescription(it.code, it.description)
        }

    fun getNonWorkingDays(): List<LocalDate> =
        referenceDataRepository.findByDatasetCode(Dataset.NON_WORKING_DAYS).map {
            val dateFormatter = DateTimeFormatter.ofPattern("ddMMyy")
            LocalDate.parse(it.code, dateFormatter)
        }
}