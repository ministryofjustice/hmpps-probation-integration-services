package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and (finish_date is null or finish_date > current_date)")
class Disability(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: PersonDetail,

    @ManyToOne
    @JoinColumn(name = "disability_type_id", updatable = false)
    val type: ReferenceData,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String?,

    @Column(name = "start_date")
    val startDate: LocalDate,

    @Column(name = "finish_date")
    val endDate: LocalDate?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "disability_id")
    val id: Long
)
