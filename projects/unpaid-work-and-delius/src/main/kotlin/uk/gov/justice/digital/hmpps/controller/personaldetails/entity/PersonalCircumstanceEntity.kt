package uk.gov.justice.digital.hmpps.controller.personaldetails.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.controller.common.entity.PersonalCircumstanceSubType
import uk.gov.justice.digital.hmpps.controller.common.entity.PersonalCircumstanceType
import java.time.LocalDate

@Entity
@Immutable
@Where(clause = "soft_deleted = 0 and (end_date is null or end_date > current_date)")
@Table(name = "personal_circumstance")
class PersonalCircumstanceEntity(
    @Id @Column(name = "personal_circumstance_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "circumstance_type_id", updatable = false)
    val type: PersonalCircumstanceType,

    @ManyToOne
    @JoinColumn(name = "circumstance_sub_type_id", updatable = false)
    val subType: PersonalCircumstanceSubType?,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String,

    @Column(name = "start_date")
    val start: LocalDate? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Convert(converter = YesNoConverter::class)
    val evidenced: Boolean = false,

)
