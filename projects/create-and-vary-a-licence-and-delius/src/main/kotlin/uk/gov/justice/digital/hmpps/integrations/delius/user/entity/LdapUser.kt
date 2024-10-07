package uk.gov.justice.digital.hmpps.integrations.delius.user.entity

import org.springframework.ldap.odm.annotations.*
import javax.naming.Name

@Entry(objectClasses = ["inetOrgPerson", "top"])
class LdapUser(
    @Id
    val dn: Name,

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 0)
    val username: String,

    @Attribute(name = "mail")
    val email: String?,

    @Attribute(name = "telephoneNumber")
    val telephoneNumber: String?
)