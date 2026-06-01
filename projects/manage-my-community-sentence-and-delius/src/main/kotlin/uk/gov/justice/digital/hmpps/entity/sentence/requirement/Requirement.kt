package uk.gov.justice.digital.hmpps.entity.sentence.requirement

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "rqmnt")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Requirement(
    @Id
    @Column(name = "rqmnt_id")
    val id: Long,
    val length: Int?,
    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,
    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory,
    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val subCategory: ReferenceData?,
    @Column(name = "start_date")
    val imposedDate: LocalDate? = null,
    @Column(name = "expected_start_date")
    val expectedStartDate: LocalDate? = null,
    @Column(name = "expected_end_date")
    val expectedEndDate: LocalDate? = null,
    @Column(name = "commencement_date")
    val actualStartDate: LocalDate? = null,
    @Column(name = "termination_date")
    val actualEndDate: LocalDate? = null,
    @Column(name = "last_updated_datetime")
    val lastUpdatedDatetime: ZonedDateTime,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val activeFlag: Boolean = true,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
) {
    companion object {
        const val UPW = "W"
        const val RAR = "F"
    }
}