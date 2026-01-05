package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.event
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.manager
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.entity.ProbationAreaUser
import uk.gov.justice.digital.hmpps.entity.ProbationAreaUserId

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(ProviderGenerator.LONDON)
        save(ProviderGenerator.WALES)
        save(TeamGenerator.DEFAULT.localAdminUnit.probationDeliveryUnit)
        save(TeamGenerator.DEFAULT.localAdminUnit)
        save(TeamGenerator.DEFAULT)
        save(StaffGenerator.TEST_STAFF)
        save(UserGenerator.TEST_USER)
        save(ProbationAreaUser(ProbationAreaUserId(UserGenerator.TEST_USER, ProviderGenerator.LONDON)))
        save(PersonGenerator.PERSON1)
        save(PersonGenerator.PERSON1.event())
        save(PersonGenerator.PERSON1.manager())
        save(PersonGenerator.PERSON2_DUPLICATE)
        save(PersonGenerator.PERSON2_DUPLICATE.event())
        save(PersonGenerator.PERSON2_DUPLICATE.manager())
        save(PersonGenerator.PERSON3_DUPLICATE)
        save(PersonGenerator.PERSON3_DUPLICATE.event())
        save(PersonGenerator.PERSON3_DUPLICATE.manager())
        save(PersonGenerator.PERSON4_INVALID)
        save(PersonGenerator.PERSON4_INVALID.event())
        save(PersonGenerator.PERSON4_INVALID.manager())
        save(PersonGenerator.PERSON5_INVALID)
        save(PersonGenerator.PERSON5_INVALID.event())
        save(PersonGenerator.PERSON5_INVALID.manager())
        save(PersonGenerator.PERSON6_EMPTY)
        save(PersonGenerator.PERSON6_EMPTY.event())
        save(PersonGenerator.PERSON6_EMPTY.manager())
        save(PersonGenerator.PERSON7_NO_EVENT)
        save(PersonGenerator.PERSON7_NO_EVENT.manager())
    }
}
