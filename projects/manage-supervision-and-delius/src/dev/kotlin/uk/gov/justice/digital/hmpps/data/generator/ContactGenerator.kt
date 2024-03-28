package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_BOROUGH
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.USER
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactDocument
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object ContactGenerator {

    val DEFAULT_PROVIDER = generateProvider("N01")
    val DEFAULT_BOROUGH = generateBorough("N01B")
    val DEFAULT_DISTRICT = generateDistrict("N01D")
    val LOCATION_BRK_1 = generateOfficeLocation(
        code = "TVP_BRK",
        description = "Bracknell Office",
        buildingNumber = "21",
        streetName = "Some Place",
        district = "District 1",
        town = "Hearth",
        postcode = "H34 7TH",
        ldu = DEFAULT_DISTRICT
    )
    val DEFAULT_STAFF = generateStaff("N01BDT1", "John", "Smith")

    val BREACH_CONTACT_TYPE = generateContactType("BRE02", false, "Breach Contact Type")
    val BREACH_ENFORCEMENT_ACTION = generateEnforcementAction("BRE02", "Breach Enforcement Action", BREACH_CONTACT_TYPE)

    val APPT_CT_1 = generateContactType("C089", true, "Alcohol Key Worker Session (NS)")
    val OTHER_CT = generateContactType("XXXX", false, "Non attendance contact type")
    val APPT_CT_2 = generateContactType("CODI", true, "Initial Appointment on Doorstep (NS)")
    val APPT_CT_3 = generateContactType("CODC", true, "Planned Doorstep Contact (NS)")

    val PREVIOUS_APPT_CONTACT_ABSENT = generateContact(
        OVERVIEW,
        APPT_CT_1,
        ZonedDateTime.of(LocalDateTime.now().minusDays(1), ZoneId.of("Europe/London")),
        attended = false,
        action = BREACH_ENFORCEMENT_ACTION,
        startTime = null
    )
    val PREVIOUS_APPT_CONTACT = generateContact(
        OVERVIEW,
        APPT_CT_1,
        ZonedDateTime.of(LocalDateTime.now().minusHours(1), ZoneId.of("Europe/London"))
    )
    val FIRST_NON_APPT_CONTACT = generateContact(
        OVERVIEW,
        OTHER_CT,
        ZonedDateTime.of(LocalDateTime.now().plusHours(1), ZoneId.of("Europe/London")),
    )
    val FIRST_APPT_CONTACT = generateContact(
        OVERVIEW,
        APPT_CT_2,
        ZonedDateTime.of(LocalDateTime.now().plusHours(2), ZoneId.of("Europe/London"))
    )
    val NEXT_APPT_CONTACT = generateContact(
        OVERVIEW,
        APPT_CT_3,
        ZonedDateTime.of(LocalDateTime.now().plusHours(3), ZoneId.of("Europe/London")),
    )

    val CONTACT_DOCUMENT_1 = generateContactDocument(
        OVERVIEW.id,
        "B001",
        "contact.doc",
        "DOCUMENT",
        primaryKeyId = NEXT_APPT_CONTACT.id,
        contact = NEXT_APPT_CONTACT
    )
    val CONTACT_DOCUMENT_2 = generateContactDocument(
        OVERVIEW.id,
        "B002",
        "contact2.doc",
        "DOCUMENT",
        primaryKeyId = NEXT_APPT_CONTACT.id,
        contact = NEXT_APPT_CONTACT
    )

    fun generateContactDocument(
        personId: Long,
        alfrescoId: String,
        name: String,
        documentType: String,
        primaryKeyId: Long? = null,
        contact: Contact?
    ): ContactDocument {
        val doc = ContactDocument(contact)
        doc.id = IdGenerator.getAndIncrement()
        doc.lastUpdated = ZonedDateTime.now().minusDays(1)
        doc.alfrescoId = alfrescoId
        doc.name = name
        doc.personId = personId
        doc.primaryKeyId = primaryKeyId
        doc.type = documentType
        return doc
    }

    fun generateEnforcementAction(code: String, description: String, contactType: ContactType) =
        EnforcementAction(
            id = IdGenerator.getAndIncrement(),
            code = code,
            description = description,
            contactType = contactType
        )

    fun generateContact(
        person: Person,
        contactType: ContactType,
        startDateTime: ZonedDateTime,
        rarActivity: Boolean? = null,
        attended: Boolean? = null,
        complied: Boolean? = null,
        sensitive: Boolean? = null,
        requirement: Requirement? = null,
        notes: String? = null,
        action: EnforcementAction? = null,
        startTime: ZonedDateTime? = ZonedDateTime.of(LocalDate.EPOCH, startDateTime.toLocalTime(), startDateTime.zone)
    ) = Contact(
        id = IdGenerator.getAndIncrement(),
        personId = person.id,
        type = contactType,
        date = startDateTime.toLocalDate(),
        startTime = startTime,
        rarActivity = rarActivity,
        attended = attended,
        sensitive = sensitive,
        complied = complied,
        requirement = requirement,
        lastUpdated = ZonedDateTime.now().minusDays(1),
        lastUpdatedUser = USER,
        staff = DEFAULT_STAFF,
        location = LOCATION_BRK_1,
        notes = notes,
        action = action
    )

    private fun generateContactType(code: String, attendance: Boolean, description: String) =
        ContactType(IdGenerator.getAndIncrement(), code, attendance, description)

    fun generateOfficeLocation(
        code: String,
        description: String,
        buildingName: String? = null,
        buildingNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        telephoneNumber: String? = null,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        ldu: District,
        id: Long = IdGenerator.getAndIncrement()
    ) = OfficeLocation(
        code,
        description,
        buildingName,
        buildingNumber,
        streetName,
        district,
        town,
        county,
        postcode,
        telephoneNumber,
        startDate,
        endDate,
        ldu,
        id
    )
}

fun generateBorough(
    code: String,
    description: String = "Description of $code",
    id: Long = IdGenerator.getAndIncrement(),
) = Borough(code, description, id)

fun generateDistrict(
    code: String,
    description: String = "Description of $code",
    borough: Borough = DEFAULT_BOROUGH,
    id: Long = IdGenerator.getAndIncrement()
) = District(code, description, borough, id)

fun generateProvider(
    code: String,
    description: String = "Description of $code",
    id: Long = IdGenerator.getAndIncrement(),
    endDate: LocalDate? = null
) = Provider(code, description, id, endDate)

fun generateStaff(code: String, forename: String, surname: String, id: Long = IdGenerator.getAndIncrement()) =
    Staff(code, forename, surname, DEFAULT_PROVIDER, id)