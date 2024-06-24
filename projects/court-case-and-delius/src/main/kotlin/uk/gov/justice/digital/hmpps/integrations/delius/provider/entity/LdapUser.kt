package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import org.springframework.ldap.odm.annotations.Attribute
import org.springframework.ldap.odm.annotations.DnAttribute
import org.springframework.ldap.odm.annotations.Entry
import org.springframework.ldap.odm.annotations.Id
import javax.naming.Name

@Entry(objectClasses = ["inetOrgPerson", "top"])
class LdapUser(

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 0)
    val username: String,

    @Attribute(name = "sn")
    val surname: String,

    @Attribute(name = "givenName")
    val forename: String,

    @Attribute(name = "mail")
    val email: String?,

    @Attribute(name = "telephoneNumber")
    val telephoneNumber: String?,

    @Id
    val dn: Name,
)
