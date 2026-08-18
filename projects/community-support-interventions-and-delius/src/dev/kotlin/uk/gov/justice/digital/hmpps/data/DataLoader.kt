package uk.gov.justice.digital.hmpps.data

import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.support.LdapNameBuilder
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
import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes

@Component
class DataLoader(dataManager: DataManager, private val ldapTemplate: LdapTemplate) : BaseDataLoader(dataManager) {
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
        save(CommunityManagerGenerator.PDU)
        save(CommunityManagerGenerator.DISTRICT)
        save(CommunityManagerGenerator.TEAM)
        save(CommunityManagerGenerator.TEAM_OFFICE_LOCATION)
        save(CommunityManagerGenerator.COMMUNITY_MANAGER)

        // Create UserPreferences LDAP entry with the team's actual ID
        val username = CommunityManagerGenerator.STAFF_USER.userName
        val dn = LdapNameBuilder.newInstance()
            .add("cn", username)
            .add("cn", "UserPreferences")
            .build()
        val attributes = BasicAttributes(true).apply {
            put(BasicAttribute("objectclass", "top").also { it.add("UserPreferences") })
            put("cn", "UserPreferences")
            put("defaultTeam", CommunityManagerGenerator.TEAM.id.toString())
        }
        ldapTemplate.bind(dn, null, attributes)
    }
}
