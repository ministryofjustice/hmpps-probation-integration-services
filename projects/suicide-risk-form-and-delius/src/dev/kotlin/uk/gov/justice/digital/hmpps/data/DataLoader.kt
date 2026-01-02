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
        BusinessInteractionCode.entries
            .map { BusinessInteraction(IdGenerator.getAndIncrement(), it.code, ZonedDateTime.now()) }
            .forEach { save(it) }
        save(PersonGenerator.DS_ADDRESS_STATUS)
        save(PersonGenerator.DEFAULT_ADDRESS_STATUS)
        save(PersonGenerator.DEFAULT_PERSON)
        save(PersonGenerator.DEFAULT_ADDRESS)

        save(ProviderGenerator.N00)
        save(StaffGenerator.DEFAULT)
        save(UserGenerator.LIMITED_ACCESS_USER)
        save(UserGenerator.NON_LAO_USER)
        save(UserGenerator.DEFAULT)
        save(PersonGenerator.RESTRICTION)
        save(PersonGenerator.EXCLUSION)
        save(PersonGenerator.RESTRICTION_EXCLUSION)
        save(LimitedAccessGenerator.RESTRICTION)
        save(LimitedAccessGenerator.EXCLUSION)
        save(LimitedAccessGenerator.BOTH_RESTRICTION)
        save(LimitedAccessGenerator.BOTH_EXCLUSION)

        save(ReferenceDataGenerator.REGISTER_TYPE_FLAG_DATASET)
        save(ReferenceDataGenerator.SAFEGUARDING_FLAG)
        save(ReferenceDataGenerator.INFORMATION_FLAG)
        save(ReferenceDataGenerator.REGISTER_LEVEL_DATASET)
        save(ReferenceDataGenerator.HIGH_RISK_REGISTER_LEVEL)
        save(ReferenceDataGenerator.APPOINTMENT_CONTACT_TYPE)
        save(ReferenceDataGenerator.APPOINTMENT_OUTCOME)
        save(RegistrationGenerator.SUICIDE_SELF_HARM_RISK_TYPE)
        save(RegistrationGenerator.CONTACT_SUSPENDED_TYPE)
        save(RegistrationGenerator.SUICIDE_SELF_HARM_REGISTRATION)
        save(PersonGenerator.PERSON_NO_REGISTRATIONS)
        save(OfficeLocationGenerator.DEFAULT)
        save(DocumentGenerator.DEFAULT_SUICIDE_RISK_FORM)
        save(DocumentGenerator.DELETED_SUICIDE_RISK_FORM)
        save(ContactGenerator.DEFAULT_CONTACT)
    }
}
