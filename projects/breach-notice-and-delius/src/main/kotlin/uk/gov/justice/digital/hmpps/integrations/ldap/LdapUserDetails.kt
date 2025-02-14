package uk.gov.justice.digital.hmpps.integrations.ldap

import org.springframework.ldap.odm.annotations.Attribute
import org.springframework.ldap.odm.annotations.Entry
import org.springframework.ldap.odm.annotations.Id

@Entry(objectClasses = ["inetOrgPerson", "top"])
class LdapUserDetails(
    @Id
    val dn: javax.naming.Name,

    @Attribute(name = "telephoneNumber")
    val telephone: String?
)