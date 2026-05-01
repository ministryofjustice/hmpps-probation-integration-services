package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import java.time.ZonedDateTime

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            BusinessInteraction(id(), BusinessInteractionCode.INSERT_ADDRESS.code, ZonedDateTime.now()),
            BusinessInteraction(id(), BusinessInteractionCode.UPDATE_ADDRESS.code, ZonedDateTime.now()),
            BusinessInteraction(id(), BusinessInteractionCode.DELETE_ADDRESS.code, ZonedDateTime.now()),
            PersonGenerator.DEFAULT_DATASET,
            PersonGenerator.ADDRESS_STATUS,
            PersonGenerator.ADDRESS_TYPE,
            PersonGenerator.RELIGION_HISTORY_UPDATER,
            PersonGenerator.TITLE,
            PersonGenerator.GENDER,
            PersonGenerator.GENDER_IDENTITY,
            PersonGenerator.ETHNICITY,
            PersonGenerator.RELIGION,
            PersonGenerator.RELIGION_HX,
            PersonGenerator.NATIONALITY,
            PersonGenerator.MAIN_ADDRESS,
            PersonGenerator.PREVIOUS_ADDRESS,
            PersonGenerator.SEXUAL_ORIENTATION,
            PersonGenerator.DRIVERS_LICENCE,
            PersonGenerator.MIN_PERSON,
            PersonGenerator.FULL_PERSON,
            PersonGenerator.UPDATABLE_PERSON,
            *PersonGenerator.FULL_PERSON_ALIASES.toTypedArray(),
            *PersonGenerator.FULL_PERSON_ADDRESSES.toTypedArray(),
            *PersonGenerator.UPDATABLE_PERSON_ADDRESSES.toTypedArray(),
            *PersonGenerator.FULL_PERSON_EXCLUSIONS.map { it.user }.toTypedArray(),
            *PersonGenerator.FULL_PERSON_EXCLUSIONS.toTypedArray(),
            *PersonGenerator.FULL_PERSON_RESTRICTIONS.map { it.user }.toTypedArray(),
            *PersonGenerator.FULL_PERSON_RESTRICTIONS.toTypedArray(),
            *PersonGenerator.FULL_PERSON_IDENTIFIERS.toTypedArray(),
            *PersonGenerator.FULL_PERSON_RELIGION_HISTORY.toTypedArray(),
            *PersonGenerator.SENTENCES.toTypedArray()
        )
    }
}
