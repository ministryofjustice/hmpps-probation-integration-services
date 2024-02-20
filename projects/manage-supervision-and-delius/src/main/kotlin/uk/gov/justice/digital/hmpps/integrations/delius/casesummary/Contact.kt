package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "contact")
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

    @Column(name = "contact_end_time")
    var endTime: ZonedDateTime? = null,

    @Column
    @Convert(converter = YesNoConverter::class)
    val sensitive: Boolean?,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String,

    @Column(name = "sgc_flag", columnDefinition = "number")
    val systemGenerated: Boolean
)

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,

    @Column
    val description: String
)

@Immutable
@Entity
@Table(name = "document")
@SQLRestriction("table_name = 'CONTACT' and soft_deleted = 0")
class ContactDocument(
    @Id
    @Column(name = "document_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "primary_key_id", referencedColumnName = "contact_id")
    val contact: Contact,

    @Column(name = "alfresco_document_id")
    val alfrescoId: String,

    @Column(name = "document_name")
    val name: String,

    @Column(name = "last_saved")
    val lastUpdated: ZonedDateTime,

    @Column
    val tableName: String = "CONTACT",

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)
