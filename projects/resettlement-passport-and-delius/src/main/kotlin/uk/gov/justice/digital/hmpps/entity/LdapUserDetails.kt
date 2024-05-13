package uk.gov.justice.digital.hmpps.entity

import org.springframework.ldap.odm.annotations.Attribute
import org.springframework.ldap.odm.annotations.Entry
import org.springframework.ldap.odm.annotations.Id

@Entry(objectClasses = ["inetOrgPerson", "top"])
class LdapUserDetails(
    @Id
    val dn: javax.naming.Name,

    @Attribute(name = "mail")
    val email: String?,

    @Attribute(name = "telephoneNumber")
    val telephone: String?
)
