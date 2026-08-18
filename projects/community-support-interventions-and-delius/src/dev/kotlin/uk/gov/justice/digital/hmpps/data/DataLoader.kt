package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisabilityGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager, private val entityManager: EntityManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        save(ReferenceDataGenerator.LANGUAGE_ENGLISH)
        save(ReferenceDataGenerator.DISABILITY_TYPE_VISUAL)
        save(PersonGenerator.PERSON1)
        save(PersonGenerator.PERSON2)
        save(RegistrationGenerator.HOIE_TYPE)
        save(RegistrationGenerator.HOME_OFFICE_INTEREST)
        save(PersonalCircumstanceGenerator.TYPE_EMPLOYMENT)
        save(PersonalCircumstanceGenerator.SUB_TYPE_FULL_TIME)
        save(PersonalCircumstanceGenerator.CIRCUMSTANCE)
        save(DisabilityGenerator.DISABILITY)
        save(NsiGenerator.OPD_TYPE)
        save(NsiGenerator.OPD_STATUS)
        save(NsiGenerator.OPD_NSI)
        setupCommunityManagerData()
    }

    private fun setupCommunityManagerData() {
        save(CommunityManagerGenerator.JOB_ROLE)
        save(CommunityManagerGenerator.STAFF)
        save(CommunityManagerGenerator.STAFF_USER)
        save(CommunityManagerGenerator.OFFICE_LOCATION)
        save(CommunityManagerGenerator.TEAM)
        save(CommunityManagerGenerator.COMMUNITY_MANAGER)
        save(CommunityManagerGenerator.PDU)

        // Create the district table and link it to borough and team for the PduRepository native query
        entityManager.createNativeQuery("CREATE TABLE IF NOT EXISTS district (district_id BIGINT PRIMARY KEY, borough_id BIGINT)").executeUpdate()
        entityManager.createNativeQuery("ALTER TABLE team ADD COLUMN IF NOT EXISTS district_id BIGINT").executeUpdate()
        entityManager.createNativeQuery(
            "MERGE INTO district (district_id, borough_id) VALUES (1, ${CommunityManagerGenerator.PDU.id})"
        ).executeUpdate()
        entityManager.createNativeQuery(
            "UPDATE team SET district_id = 1 WHERE team_id = ${CommunityManagerGenerator.TEAM.id}"
        ).executeUpdate()

        // Add default_team_location_flag column to team_office_location for the @SQLJoinTableRestriction on Team.officeLocations
        entityManager.createNativeQuery(
            "ALTER TABLE team_office_location ADD COLUMN IF NOT EXISTS default_team_location_flag INT DEFAULT 1"
        ).executeUpdate()
    }
}
