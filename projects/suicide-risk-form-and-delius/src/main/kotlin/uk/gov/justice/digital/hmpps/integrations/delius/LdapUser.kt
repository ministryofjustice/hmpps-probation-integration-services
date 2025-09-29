package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.ldap.odm.annotations.Attribute
import org.springframework.ldap.odm.annotations.DnAttribute
import org.springframework.ldap.odm.annotations.Entry
import org.springframework.ldap.odm.annotations.Id
import uk.gov.justice.digital.hmpps.model.Name

@Entry(objectClasses = ["inetOrgPerson", "top"])
class LdapUser(
    @Id
    val dn: Name,

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 0)
    val username: String,

    @Attribute(name = "givenName")
    val firstName: String,

    @Attribute(name = "sn")
    val surname: String,

    @Attribute(name = "mail")
    val email: String?,

    @Attribute(name = "telephoneNumber")
    val telephoneNumber: String?,

    @Attribute(name = "userHomeArea")
    val userHomeArea: String?
)

fun LdapUser.name() = Name(firstName, null, surname)