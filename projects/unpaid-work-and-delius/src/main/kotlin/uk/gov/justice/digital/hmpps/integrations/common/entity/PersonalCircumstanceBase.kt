package uk.gov.justice.digital.hmpps.integrations.common.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import java.time.LocalDate

@MappedSuperclass
@Immutable
open class PersonalCircumstanceBase(
    @Id
    @Column(name = "personal_circumstance_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "circumstance_type_id", updatable = false)
    val type: PersonalCircumstanceType,
    @ManyToOne
    @JoinColumn(name = "circumstance_sub_type_id", updatable = false)
    val subType: PersonalCircumstanceSubType?,
    @Column(name = "notes", columnDefinition = "clob")
    val notes: String?,
    @Column(name = "start_date")
    val start: LocalDate? = null,
    @Column(name = "end_date")
    val endDate: LocalDate? = null,
    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
    @Convert(converter = YesNoConverter::class)
    val evidenced: Boolean? = false,
)
