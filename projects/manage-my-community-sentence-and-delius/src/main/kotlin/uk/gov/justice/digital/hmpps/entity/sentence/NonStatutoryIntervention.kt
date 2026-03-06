package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@Table(name = "nsi")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class NonStatutoryIntervention(
    @Id
    @Column(name = "nsi_id")
    val id: Long,
    @Column(name = "rqmnt_id")
    val requirementId: Long?,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val activeFlag: Boolean = true,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)
