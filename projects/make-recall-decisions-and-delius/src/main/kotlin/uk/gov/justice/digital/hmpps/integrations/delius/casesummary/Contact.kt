package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.ContactTypeSummary
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Table(name = "contact")
@Entity(name = "CaseSummaryContact")
@SQLRestriction("soft_deleted = 0")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column
    val description: String?,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: ContactOutcome? = null,

    @OneToMany(mappedBy = "contact")
    val documents: List<ContactDocument>,

    @Lob
    @Column
    val notes: String? = null,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime?,

    @Column
    @Convert(converter = YesNoConverter::class)
    val sensitive: Boolean?,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "r_contact_type")
@Entity(name = "CaseSummaryContactType")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String,

    @Column(name = "sgc_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val systemGenerated: Boolean
)

@Immutable
@Table(name = "r_contact_outcome_type")
@Entity(name = "CaseSummaryContactOutcome")
class ContactOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,

    @Column
    val description: String
)

@Immutable
@Table(name = "document")
@Entity(name = "CaseSummaryContactDocument")
@SQLRestriction("table_name = 'CONTACT' and soft_deleted = 0")
class ContactDocument(
    @Id
    @Column(name = "document_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "primary_key_id", referencedColumnName = "contact_id")
    @NotFound(action = NotFoundAction.IGNORE)
    val contact: Contact,

    @Column(name = "alfresco_document_id")
    val alfrescoId: String,

    @Column(name = "document_name")
    val name: String,

    @Column(name = "last_saved")
    val lastUpdated: ZonedDateTime,

    @Column(name = "table_name")
    val tableName: String = "CONTACT",

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface CaseSummaryContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select c from CaseSummaryContact c
        join fetch c.type
        left join fetch c.outcome
        left join fetch c.documents
        where c.personId = :personId
        and c.date <= :toDate
        and (:fromDate is null or c.date >= :fromDate)
        and (:typesCount = 0 or c.type.code in :types)
        and (:includeSystemGenerated = true or c.type.systemGenerated = false)
        order by c.date desc, c.startTime desc
        """
    )
    fun findContacts(
        personId: Long,
        fromDate: LocalDate?,
        toDate: LocalDate,
        includeSystemGenerated: Boolean,
        types: List<String>,
        typesCount: Int = types.count()
    ): List<Contact>

    @Query(
        """
        select new uk.gov.justice.digital.hmpps.api.model.ContactTypeSummary(
            c.type.code, c.type.description, cast(count(c) as int)
        )
        from CaseSummaryContact c
        where c.personId = :personId
        and c.date <= current_date
        group by c.type.code, c.type.description
        order by c.type.code
        """
    )
    fun summarizeContactTypes(personId: Long): List<ContactTypeSummary>
}

fun CaseSummaryContactRepository.searchContacts(
    personId: Long,
    query: String?,
    fromDate: LocalDate?,
    toDate: LocalDate,
    types: List<String>,
    includeSystemGenerated: Boolean
): List<Contact> {
    val contacts = findContacts(personId, fromDate, toDate, includeSystemGenerated, types)
    // Oracle doesn't allow query comparisons on CLOBs, so we have to filter the notes ourselves:
    return if (query.isNullOrBlank()) contacts else contacts.filter { it.notes?.contains(query, true) == true }
}
