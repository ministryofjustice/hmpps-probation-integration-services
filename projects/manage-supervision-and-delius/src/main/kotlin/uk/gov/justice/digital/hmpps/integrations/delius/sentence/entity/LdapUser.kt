package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import org.springframework.ldap.odm.annotations.Attribute
import org.springframework.ldap.odm.annotations.DnAttribute
import org.springframework.ldap.odm.annotations.Entry
import org.springframework.ldap.odm.annotations.Id
import org.springframework.ldap.odm.annotations.Transient
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter.ofPattern
import javax.naming.Name

@Entry(objectClasses = ["inetOrgPerson", "top"])
class LdapUser(
    @Id
    val dn: Name,

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 0)
    val username: String,

    @Attribute(name = "givenName")
    val forename: String,

    @Attribute(name = "sn")
    val surname: String,

    @Attribute(name = "mail")
    val email: String?,

    @Attribute(name = "telephoneNumber")
    val telephone: String?,

    @Attribute(name = "endDate")
    val endDate: String?,

    @Transient
    var roles: List<String>
) {
    val enabled: Boolean
        get() = endDate == null || LocalDate.parse(endDate.substring(0, 8), ofPattern("yyyyMMdd")).isAfter(now())
}