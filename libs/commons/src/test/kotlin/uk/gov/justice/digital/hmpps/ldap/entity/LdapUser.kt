package uk.gov.justice.digital.hmpps.ldap.entity

import org.springframework.ldap.odm.annotations.Attribute
import org.springframework.ldap.odm.annotations.DnAttribute
import org.springframework.ldap.odm.annotations.Entry
import org.springframework.ldap.odm.annotations.Id
import javax.naming.Name

@Entry(objectClasses = ["inetOrgPerson", "top"], base = "ou=Users")
class LdapUser(
    @Id
    val dn: Name,

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 1)
    val username: String,

    @Attribute(name = "mail")
    val email: String?
)
