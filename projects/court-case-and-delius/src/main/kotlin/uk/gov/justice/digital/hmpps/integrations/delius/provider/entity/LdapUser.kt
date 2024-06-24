package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import org.springframework.ldap.odm.annotations.*
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

    @Attribute(name = "userHomeArea")
    val homeArea: String?,

    @Attribute(name = "mail")
    val email: String?,

    @Attribute(name = "telephoneNumber")
    val telephoneNumber: String?,

    @Attribute(name = "endDate")
    val endDate: String?,

    @Transient
    var roles: List<String>
) {
    val enabled: Boolean
        get() = endDate == null || LocalDate.parse(endDate.substring(0, 8), ofPattern("yyyyMMdd")).isAfter(now())
}
