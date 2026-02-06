package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator.COURT_APPEARANCE
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import java.time.ZonedDateTime

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {

        BusinessInteractionCode.entries.forEach {
            save(BusinessInteraction(IdGenerator.getAndIncrement(), it.code, ZonedDateTime.now()))
        }
        saveAll(
            AppointmentGenerator.ATTENDED_COMPLIED,
            AppointmentGenerator.POP_RESCHEDULED_OUTCOME,
            AppointmentGenerator.SERVICE_RESCHEDULED_OUTCOME,
            *AppointmentGenerator.APPOINTMENT_TYPES.toTypedArray(),
            *AppointmentGenerator.CONTACT_TYPE_OUTCOMES.toTypedArray(),
            ContactGenerator.DEFAULT_PROVIDER,
            ContactGenerator.DEFAULT_BOROUGH,
            ContactGenerator.DEFAULT_DISTRICT,
            ContactGenerator.DEFAULT_STAFF,
            ContactGenerator.LIMITED_ACCESS_STAFF,
            ContactGenerator.STAFF_1,
            ContactGenerator.DEFAULT_TEAM,
            ContactGenerator.LOCATION_BRK_1,
        )

        save(ContactGenerator.USER)

        save(ContactGenerator.USER_1)

        save(ContactGenerator.USER_2)

        save(ContactGenerator.LIMITED_ACCESS_USER)

        saveAll(
            PersonGenerator.GENDER_MALE,
            PersonGenerator.MAPPA_TYPE,
            PersonGenerator.MAPPA_CATEGORY,
            PersonGenerator.MAPPA_LEVEL
        )

        PersonGenerator.DISABILITIES.forEach { save(it.type) }
        PersonGenerator.PROVISIONS.forEach { save(it.type) }
        PersonGenerator.PERSONAL_CIRCUMSTANCES.forEach {
            save(it.type)
            save(it.subType)
        }

        saveAll(PersonGenerator.DISABILITIES)
        saveAll(PersonGenerator.PROVISIONS)
        saveAll(PersonGenerator.PERSONAL_CIRCUMSTANCES)
        save(PersonGenerator.OVERVIEW)
        save(PersonGenerator.E_SUP_PERSON)
        save(CourtGenerator.BHAM)
        save(PersonGenerator.EVENT_1)
        save(PersonGenerator.EVENT_2)
        save(PersonGenerator.INACTIVE_EVENT_1)
        save(PersonGenerator.INACTIVE_EVENT_2)
        save(PersonGenerator.INACTIVE_EVENT_3)
        save(PersonGenerator.INACTIVE_EVENT_NO_TIME_UNIT)

        save(AdditionalSentenceGenerator.REF_DISQ)
        save(AdditionalSentenceGenerator.REF_FINE)
        save(
            AdditionalSentenceGenerator.generateSentence(
                3,
                null,
                null,
                PersonGenerator.EVENT_1,
                AdditionalSentenceGenerator.REF_DISQ
            )
        )
        save(CourtGenerator.DEFAULT)

        save(CourtReportGenerator.COURT_APPEARANCE)
        save(CourtReportGenerator.DEFAULT_TYPE)
        save(CourtReportGenerator.EVENT_DOCUMENT)
        save(CourtReportGenerator.COURT_DOCUMENT)
        save(CourtReportGenerator.COURT_REPORT)
        save(COURT_APPEARANCE)

        save(CourtReportGenerator.DEFAULT_TYPE)
        save(CourtReportGenerator.COURT_REPORT)

        saveAll(
            OffenderManagerGenerator.BOROUGH,
            OffenderManagerGenerator.DISTRICT,
            OffenderManagerGenerator.TEAM,
            OffenderManagerGenerator.TEAM_1,
            OffenderManagerGenerator.TEAM_2,
            OffenderManagerGenerator.STAFF_ROLE,
            OffenderManagerGenerator.STAFF_1,
            OffenderManagerGenerator.STAFF_2,
            OffenderManagerGenerator.STAFF_3,
            OffenderManagerGenerator.STAFF_4,
            OffenderManagerGenerator.STAFF_USER_1,
            OffenderManagerGenerator.STAFF_USER_2,
            OffenderManagerGenerator.STAFF_USER_3,
            OffenderManagerGenerator.PI_USER,
            OffenderManagerGenerator.STAFF_TEAM,
            OffenderManagerGenerator.STAFF_TEAM_1,
            OffenderManagerGenerator.RESPONSIBLE_OFFICER_OM_ACTIVE,
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE,
            OffenderManagerGenerator.RESPONSIBLE_OFFICER_OM_INACTIVE,
            OffenderManagerGenerator.RESPONSIBLE_OFFICER_OM_UNALLOCATED,
            OffenderManagerGenerator.OFFENDER_MANAGER_INACTIVE,
            OffenderManagerGenerator.OFFENDER_MANAGER_UNALLOCATED,
            OffenderManagerGenerator.PRISON_OFFENDER_MANAGER_ACTIVE,
            OffenderManagerGenerator.PRISON_OFFENDER_MANAGER_INACTIVE,
            OffenderManagerGenerator.RESPONSIBLE_OFFICER,
            OffenderManagerGenerator.DEFAULT_LOCATION,
            OffenderManagerGenerator.TEAM_OFFICE,
            OffenderManagerGenerator.TEAM_OFFICE_1,
            OffenderManagerGenerator.PAU_USER_RECORD1,
            OffenderManagerGenerator.PROVIDER_2,
            OffenderManagerGenerator.PAU_USER_RECORD2,
            OffenderManagerGenerator.PROVIDER_3,
            OffenderManagerGenerator.PAU_USER_RECORD3,
            OffenderManagerGenerator.PAU_USER_RECORD4,
            PersonGenerator.DEFAULT_DISPOSAL_TYPE,
            LicenceConditionGenerator.LIC_COND_MAIN_CAT,
            LicenceConditionGenerator.LIC_COND_SUB_CAT,
            LicenceConditionGenerator.LC_WITH_NOTES,
            LicenceConditionGenerator.LC_WITHOUT_NOTES,
            LicenceConditionGenerator.LC_WITH_NOTES_WITHOUT_ADDED_BY,
            LicenceConditionGenerator.LC_WITH_1500_CHAR_NOTE,
            PersonGenerator.TERMINATION_REASON,
            PersonGenerator.REF_DATA_YEARS,
            PersonGenerator.REF_DATA_MONTHS,
            PersonGenerator.ACTIVE_ORDER,
            PersonGenerator.INACTIVE_ORDER_1,
            PersonGenerator.INACTIVE_ORDER_2,
            PersonGenerator.INACTIVE_ORDER_3,
            PersonGenerator.INACTIVE_ORDER_4,
            ContactGenerator.COMMUNICATION_CATEGORY_RD,
            ContactGenerator.BREACH_CONTACT_TYPE,
            ContactGenerator.BREACH_ENFORCEMENT_ACTION,
            ContactGenerator.APPT_CT_1,
            ContactGenerator.OTHER_CT,
            ContactGenerator.APPT_CT_2,
            ContactGenerator.APPT_CT_3,
            ContactGenerator.EMAIL_POP_CT,
            ContactGenerator.EVENT_LEVEL_CT,
            ContactGenerator.RQMNT_LEVEL_CT,
            ContactGenerator.E_SUPERVISION_TYPE,
            ContactGenerator.E_SUPERVISION_CONTACT,
            ContactGenerator.PREVIOUS_APPT_CONTACT,
            ContactGenerator.FIRST_NON_APPT_CONTACT,
            ContactGenerator.NEXT_APPT_CONTACT,
            ContactGenerator.FIRST_APPT_CONTACT,
            ContactGenerator.ACCEPTABLE_ABSENCE,
            ContactGenerator.POSSIBLE_OUTCOME_1,
            ContactGenerator.POSSIBLE_OUTCOME_2,
            ContactGenerator.POSSIBLE_OUTCOME_3,
            ContactGenerator.POSSIBLE_OUTCOME_4,
            ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT,
            ContactGenerator.PREVIOUS_COMMUNICATION_CONTACT,
            ContactGenerator.COMMUNICATION_CATEGORY,
            ContactGenerator.CONTACT_DOCUMENT_1,
            ContactGenerator.CONTACT_DOCUMENT_2,
            ContactGenerator.CONTACT_DOCUMENT_3,
            PersonGenerator.OFFENCE_1,
            PersonGenerator.MAIN_OFFENCE_1,
            PersonGenerator.OFFENCE_2,
            PersonGenerator.MAIN_OFFENCE_2,
            PersonGenerator.OFFENCE_3,
            PersonGenerator.MAIN_OFFENCE_3,
            PersonGenerator.OFFENCE_4,
            PersonGenerator.MAIN_OFFENCE_4,
            PersonGenerator.MAIN_OFFENCE_5,
            PersonGenerator.MAIN_OFFENCE_6,
            PersonGenerator.ADD_OFF_1,
            PersonGenerator.ADDITIONAL_OFFENCE_1,
            PersonGenerator.ADD_OFF_2,
            PersonGenerator.ADDITIONAL_OFFENCE_2,
            PersonGenerator.UNIT,
            PersonGenerator.MAIN_CAT_F,
            PersonGenerator.MAIN_CAT_W,
            PersonGenerator.MAIN_CAT_F_TYPE,
            PersonGenerator.MAIN_CAT_W_TYPE,
            PersonGenerator.REQUIREMENT,
            PersonGenerator.REQUIREMENT_UNPAID_WORK,
            PersonGenerator.REQUIREMENT_NULL_MAIN_CATEGORY,
            PersonGenerator.REQUIREMENT_CONTACT_1,
            PersonGenerator.REQUIREMENT_CONTACT_2,
            PersonGenerator.REGISTER_TYPE_1,
            PersonGenerator.REGISTER_TYPE_2,
            PersonGenerator.MAPPA_TYPE,
            PersonGenerator.REGISTRATION_1,
            PersonGenerator.REGISTRATION_2,
            PersonGenerator.REGISTRATION_3,
            PersonGenerator.MAPPA_REGISTRATION,
            PersonGenerator.REGISTRATION_REVIEW_1,
            PersonGenerator.REGISTRATION_REVIEW_2,
            PersonGenerator.REGISTRATION_REVIEW_3,
            PersonGenerator.DEREGISTRATION_1,
            PersonGenerator.NSI_BREACH_TYPE,
            PersonGenerator.NSI_OPD_TYPE,
            PersonGenerator.NSI_OPD_SUB_TYPE,
            PersonGenerator.NSI_STATUS,
            PersonGenerator.BREACH_PREVIOUS_ORDER_1,
            PersonGenerator.BREACH_PREVIOUS_ORDER_2,
            PersonGenerator.BREACH_ON_ACTIVE_ORDER,
            PersonGenerator.OPD_NSI,
            UnpaidWorkApptGenerator.UNPAID_WORK_DETAILS_1,
            UnpaidWorkApptGenerator.APPT1,
            UnpaidWorkApptGenerator.APPT2,
            CustodyGenerator.CUSTODY_1,
            CustodyGenerator.RELEASE_1,
            CustodyGenerator.RELEASE_2,
            CustodyGenerator.RELEASE_3,
            ContactGenerator.RQMNT_CONTACT_TYPE
        )
        personalDetailsData()
    }

    fun personalDetailsData() {
        saveAll(
            PersonDetailsGenerator.ADDRESS_TYPE,
            PersonDetailsGenerator.ADDRESS_STATUS,
            PersonDetailsGenerator.GENDER_FEMALE,
            PersonDetailsGenerator.RELIGION_DEFAULT,
            PersonDetailsGenerator.SEXUAL_ORIENTATION,
            PersonDetailsGenerator.LANGUAGE_RD,
            PersonDetailsGenerator.GENDER_IDENTITY_RD,
            PersonDetailsGenerator.PERSONAL_DETAILS,
            PersonDetailsGenerator.RESTRICTION,
            PersonDetailsGenerator.EXCLUSION,
            PersonDetailsGenerator.RESTRICTION_EXCLUSION,
            PersonDetailsGenerator.DISABILITY_1_RD,
            PersonDetailsGenerator.DISABILITY_2_RD,
            PersonDetailsGenerator.PERSONAL_CIRCUMSTANCE_1_RD,
            PersonDetailsGenerator.PERSONAL_CIRCUMSTANCE_SUBTYPE_1,
            PersonDetailsGenerator.PERSONAL_CIRCUMSTANCE_2_RD,
            PersonDetailsGenerator.PERSONAL_CIRCUMSTANCE_SUBTYPE_2,
            PersonDetailsGenerator.PROVISION_1_RD,
            PersonDetailsGenerator.PROVISION_2_RD,
            PersonDetailsGenerator.DISABILITY_1,
            PersonDetailsGenerator.DISABILITY_2,
            PersonDetailsGenerator.PROVISION_1,
            PersonDetailsGenerator.PROVISION_2,
            PersonDetailsGenerator.PERSONAL_CIRC_1,
            PersonDetailsGenerator.PERSONAL_CIRC_2,
            PersonDetailsGenerator.PERSONAL_CIRC_PREV,
            PersonDetailsGenerator.RELATIONSHIP_TYPE,
            PersonDetailsGenerator.CONTACT_ADDRESS,
            PersonDetailsGenerator.PERSONAL_CONTACT_1,
            PersonDetailsGenerator.PERSON_ADDRESS_STATUS_1,
            PersonDetailsGenerator.PERSON_PREVIOUS_ADDRESS_STATUS,
            PersonDetailsGenerator.PERSON_ADDRESS_TYPE_1,
            PersonDetailsGenerator.PERSON_ADDRESS_1,
            PersonDetailsGenerator.PERSON_ADDRESS_STATUS_2,
            PersonDetailsGenerator.PERSON_ADDRESS_TYPE_2,
            PersonDetailsGenerator.PERSON_ADDRESS_2,
            PersonDetailsGenerator.NULL_ADDRESS,
            PersonDetailsGenerator.PREVIOUS_ADDRESS,
            PersonDetailsGenerator.PREVIOUS_ADDRESS_1,
            PersonDetailsGenerator.PREVIOUS_ADDRESS_2,
            PersonDetailsGenerator.PREVIOUS_ADDRESS_3,
            PersonDetailsGenerator.PREVIOUS_ADDRESS_4,
            PersonDetailsGenerator.DOCUMENT_1,
            PersonDetailsGenerator.DOCUMENT_2,
            PersonDetailsGenerator.ALIAS_1,
            PersonDetailsGenerator.ALIAS_2,
        )
        save(PersonGenerator.PERSON_1)
        save(PersonGenerator.PERSON_2)
        save(PersonGenerator.CL_EXCLUDED)
        save(PersonGenerator.CL_RESTRICTED)
        save(PersonGenerator.CL_RESTRICTED_EXCLUDED)
        save(PersonGenerator.CASELOAD_PERSON_1)
        save(PersonGenerator.CASELOAD_PERSON_2)
        save(PersonGenerator.CASELOAD_PERSON_3)

        save(PersonGenerator.CASELOAD_LIMITED_ACCESS_EXCLUSION)
        save(PersonGenerator.CASELOAD_LIMITED_ACCESS_RESTRICTION)
        save(PersonGenerator.CASELOAD_LIMITED_ACCESS_BOTH)
        save(PersonGenerator.CASELOAD_LIMITED_ACCESS_NEITHER)

        save(LimitedAccessGenerator.EXCLUSION)
        save(LimitedAccessGenerator.RESTRICTION)
        save(LimitedAccessGenerator.BOTH_EXCLUSION)
        save(LimitedAccessGenerator.BOTH_RESTRICTION)

        save(AppointmentGenerator.PERSON_APPOINTMENT)
        save(AppointmentGenerator.LATE_NIGHT_APPOINTMENT)

        saveAll(
            PersonGenerator.RESCHEDULED_PERSON_1,
            PersonGenerator.RESCHEDULED_PERSON_2,
            PersonGenerator.RECREATE_APPT_PERSON_1,
            PersonGenerator.RECREATE_APPT_PERSON_2,
        )
    }
}
