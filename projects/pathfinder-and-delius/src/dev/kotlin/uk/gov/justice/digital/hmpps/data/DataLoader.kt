package uk.gov.justice.digital.hmpps.data

import UserGenerator
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ConvictionEventGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.KeyDateGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val userRepository: UserRepository,
    private val em: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveUserToDb() {
        userRepository.save(UserGenerator.APPLICATION_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        em.saveAll(
            ProbationAreaGenerator.DEFAULT_PA,
            ProbationAreaGenerator.DEFAULT_BOROUGH,
            ProbationAreaGenerator.DEFAULT_LDU,
            CourtAppearanceGenerator.DEFAULT_CA_TYPE,
            CourtAppearanceGenerator.DEFAULT_COURT,
            CourtAppearanceGenerator.DEFAULT_PERSON,
            CourtAppearanceGenerator.DEFAULT_EVENT,
            CourtAppearanceGenerator.DEFAULT_CA,
            ConvictionEventGenerator.PERSON,
            ConvictionEventGenerator.OFFENCE_OTHER,
            ConvictionEventGenerator.OFFENCE_MAIN,
            ConvictionEventGenerator.DEFAULT_EVENT,
            ConvictionEventGenerator.MAIN_OFFENCE,
            ConvictionEventGenerator.OTHER_OFFENCE,
            ConvictionEventGenerator.DISPOSAL_TYPE,
            ConvictionEventGenerator.DISPOSAL,
            DetailsGenerator.RELIGION,
            DetailsGenerator.PERSON,
            DetailsGenerator.DEFAULT_PA,
            DetailsGenerator.DISTRICT,
            DetailsGenerator.TEAM,
            DetailsGenerator.STAFF,
            DetailsGenerator.PERSON_MANAGER,
            ConvictionEventGenerator.PERSON_2,
            ConvictionEventGenerator.EVENT_2,
            ConvictionEventGenerator.MAIN_OFFENCE_2,
            ConvictionEventGenerator.OTHER_OFFENCE_2,
            ConvictionEventGenerator.DISPOSAL_2,
            KeyDateGenerator.SED_KEYDATE,
            KeyDateGenerator.CUSTODY,
            KeyDateGenerator.KEYDATE
        )

        em.createNativeQuery("""
             update event set offender_id = ${DetailsGenerator.PERSON.id} 
             where event_id = ${ConvictionEventGenerator.EVENT_2.id}""".trimMargin())
            .executeUpdate()
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}
