package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import jakarta.persistence.GenerationType.SEQUENCE
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "r_offence")
@SequenceGenerator(name = "offence_id_seq", sequenceName = "offence_id_seq", allocationSize = 1)
data class ReferenceOffence(

    @Column(columnDefinition = "char(5)")
    val code: String,

    val description: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean?,

    @Column(columnDefinition = "char(3)")
    val mainCategoryCode: String,

    val mainCategoryDescription: String,
    val mainCategoryAbbreviation: String?,

    val ogrsOffenceCategoryId: Long?,

    @Column(columnDefinition = "char(2)")
    val subCategoryCode: String,

    val subCategoryDescription: String,

    @Column(name = "form_20_code")
    val form20Code: String?,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_sexual_offence")
    val schedule15SexualOffence: Boolean?,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_violent_offence")
    val schedule15ViolentOffence: Boolean?,

    @Convert(converter = YesNoConverter::class)
    val childAbduction: Boolean?,

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "offence_id_seq")
    @Column(name = "offence_id")
    val id: Long = 0
) {

    @Column
    val partitionAreaId: Long = 0

    @Version
    var rowVersion: Long = 0

    @CreatedDate
    @Column(name = "created_datetime")
    var created: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdBy: Long = 0

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastModified: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastModifiedBy: Long = 0
}

interface OffenceRepository : JpaRepository<ReferenceOffence, Long> {
    fun findOffenceByCode(code: String): ReferenceOffence?
}