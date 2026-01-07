package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.asLaoPerson
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager
import uk.gov.justice.digital.hmpps.entity.Exclusion
import uk.gov.justice.digital.hmpps.entity.Restriction
import java.time.LocalDate

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            UserGenerator.JOHN_SMITH,
            PersonGenerator.DEFAULT_GENDER,
            PersonGenerator.DEFAULT,
            SentenceGenerator.DEFAULT_COURT,
            SentenceGenerator.DEFAULT_EVENT,
            SentenceGenerator.DEFAULT_SENTENCE,
            SentenceGenerator.DEFAULT_COURT_APPEARANCE,
            ProviderGenerator.DEFAULT,
            ManagerGenerator.DEFAULT_PERSON_MANAGER,
            ManagerGenerator.DEFAULT_RESPONSIBLE_OFFICER,
            PersonGenerator.EXCLUDED,
            PersonGenerator.RESTRICTED,
            SentenceGenerator.generateOgrsAssessment(LocalDate.now().minusDays(1), 3),
            SentenceGenerator.generateOgrsAssessment(LocalDate.now().minusDays(5), 1),
            SentenceGenerator.generateOgrsAssessment(LocalDate.now(), 5, softDeleted = true),
            PersonGenerator.WITH_RELEASE_DATE,
            ManagerGenerator.RELEASED_PERSON_MANAGER,
            SentenceGenerator.RELEASE_DATE_TYPE,
            SentenceGenerator.RELEASED_EVENT,
            SentenceGenerator.RELEASED_COURT_APPEARANCE,
            SentenceGenerator.RELEASED_SENTENCE,
            SentenceGenerator.RELEASED_CUSTODY,
            SentenceGenerator.RELEASE_DATE,
            SentenceGenerator.generateEvent(PersonGenerator.EXCLUDED),
            SentenceGenerator.generateEvent(PersonGenerator.RESTRICTED),
            Exclusion(
                PersonGenerator.EXCLUDED.asLaoPerson(),
                UserGenerator.JOHN_SMITH.asLaoUser(),
                null,
                IdGenerator.getAndIncrement()
            ),
            Restriction(
                PersonGenerator.RESTRICTED.asLaoPerson(),
                UserGenerator.JOHN_SMITH.asLaoUser(),
                null,
                IdGenerator.getAndIncrement()
            ),
        )
    }
}
