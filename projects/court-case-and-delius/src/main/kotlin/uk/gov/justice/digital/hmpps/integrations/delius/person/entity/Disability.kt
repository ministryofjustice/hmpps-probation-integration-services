package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "disability")
class Disability(
    @Id
    @Column(name = "disability_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "disability_type_id")
    val type: ReferenceData,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "disability_condition_id")
    val condition: ReferenceData? = null,

    val startDate: LocalDate,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    val finishDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    )
