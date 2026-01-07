package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import java.time.ZonedDateTime

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        businessInteractions()
        basicDetailsData()
        warningData()
        appointmentData()
        save(DocumentGenerator.DEFAULT_BREACH_NOTICE)
        save(DocumentGenerator.UNSENTENCED_BREACH_NOTICE)
        save(DocumentGenerator.DELETED_BREACH_NOTICE)
        lao()
        pssData()
    }

    private fun businessInteractions() {
        saveAll(
            BusinessInteractionCode.entries.map {
                BusinessInteraction(
                    IdGenerator.getAndIncrement(),
                    it.code,
                    ZonedDateTime.now()
                )
            }
        )
    }

    private fun basicDetailsData() {
        saveAll(
            PersonGenerator.DS_ADDRESS_STATUS,
            PersonGenerator.DEFAULT_ADDRESS_STATUS,
            ProviderGenerator.DEFAULT_PROVIDER,
            OfficeLocationGenerator.DEFAULT_LOCATION,
            StaffGenerator.DEFAULT_STAFF,
            StaffGenerator.DEFAULT_SU,
            PersonGenerator.DEFAULT_PERSON,
            PersonGenerator.DEFAULT_ADDRESS,
            PersonGenerator.END_DATED_ADDRESS,
            PersonGenerator.DEFAULT_PERSON_MANAGER,
        )
    }

    private fun warningData() {
        saveAll(
            WarningGenerator.DS_BREACH_NOTICE_TYPE,
            *WarningGenerator.NOTICE_TYPES.toTypedArray(),
            WarningGenerator.DS_BREACH_REASON,
            *WarningGenerator.BREACH_REASONS.toTypedArray(),
            WarningGenerator.DS_BREACH_CONDITION_TYPE,
            *WarningGenerator.CONDITION_TYPES.toTypedArray(),
            WarningGenerator.DS_BREACH_SENTENCE_TYPE,
            *WarningGenerator.SENTENCE_TYPES.toTypedArray(),
            WarningGenerator.ENFORCEABLE_CONTACT_TYPE,
            WarningGenerator.ENFORCEABLE_CONTACT_OUTCOME,
            EventGenerator.DEFAULT_EVENT,
            EventGenerator.DEFAULT_DISPOSAL_TYPE,
            EventGenerator.DEFAULT_DISPOSAL,
            EventGenerator.UNSENTENCED_EVENT,
            EventGenerator.DEFAULT_RQMNT_CATEGORY,
            EventGenerator.UNPAID_RQMT_CATEGORY,
            EventGenerator.DS_REQUIREMENT_SUB_CATEGORY,
            *EventGenerator.UNPAID_RQMT_SUB_CATEOGORY_RECORDS.toTypedArray(),
            EventGenerator.DEFAULT_RQMNT_SUB_CATEGORY,
            EventGenerator.DEFAULT_RQMNT,
            *WarningGenerator.ENFORCEABLE_CONTACTS.toTypedArray(),
            *WarningGenerator.ENFORCEABLE_CONTACTS_UNPAID.toTypedArray(),
            WarningGenerator.UPW_APPOINTMENT,
            *EventGenerator.UNPAID_WORK_RQMTS.toTypedArray(),
        )
    }

    private fun pssData() {
        saveAll(
            EventGenerator.DEFAULT_PSS_CATEGORY,
            EventGenerator.DEFAULT_PSS_SUB_CATEGORY,
            PersonGenerator.PSS_PERSON,
            PersonGenerator.PSS_PERSON_MANAGER,
            EventGenerator.PSS_EVENT,
            EventGenerator.PSS_DISPOSAL,
            EventGenerator.PSS_CUSTODY,
            EventGenerator.PSS_REQUIREMENT,
            AppointmentGenerator.PSS_APPOINTMENT,
            WarningGenerator.PSS_ENFORCEABLE_CONTACT,
            DocumentGenerator.PSS_BREACH_NOTICE
        )
    }

    private fun appointmentData() {
        saveAll(
            AppointmentGenerator.APPOINTMENT_CONTACT_TYPE,
            AppointmentGenerator.APPOINTMENT_OUTCOME,
            *AppointmentGenerator.OTHER_APPOINTMENTS.toTypedArray(),
            *AppointmentGenerator.FUTURE_APPOINTMENTS.toTypedArray(),
        )
    }

    private fun lao() {
        saveAll(
            UserGenerator.TEST_USER,
            UserGenerator.LIMITED_ACCESS_USER,
            UserGenerator.NON_LAO_USER,
            PersonGenerator.EXCLUSION,
            PersonGenerator.RESTRICTION,
            PersonGenerator.RESTRICTION_EXCLUSION,
        )
        saveAll(
            LimitedAccessGenerator.EXCLUSION,
            LimitedAccessGenerator.RESTRICTION,
            LimitedAccessGenerator.BOTH_EXCLUSION,
            LimitedAccessGenerator.BOTH_RESTRICTION
        )
    }
}
