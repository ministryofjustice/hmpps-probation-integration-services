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
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "r_offence")
@SequenceGenerator(name = "offence_id_seq", sequenceName = "offence_id_seq", allocationSize = 1)
class ReferenceOffence(

    @Column(columnDefinition = "char(5)")
    var code: String,

    var description: String,

    @Convert(converter = YesNoConverter::class)
    var selectable: Boolean?,

    @Column(columnDefinition = "char(3)")
    var mainCategoryCode: String,

    var mainCategoryDescription: String,
    var mainCategoryAbbreviation: String?,

    var ogrsOffenceCategoryId: Long?,

    @Column(columnDefinition = "char(2)")
    var subCategoryCode: String,

    var subCategoryDescription: String,

    @Column(name = "form_20_code")
    var form20Code: String?,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_sexual_offence")
    var schedule15SexualOffence: Boolean?,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_violent_offence")
    var schedule15ViolentOffence: Boolean?,

    @Convert(converter = YesNoConverter::class)
    var childAbduction: Boolean?,

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "offence_id_seq")
    @Column(name = "offence_id")
    val id: Long = 0
) {

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
    fun findByCode(code: String): ReferenceOffence?
}

fun OffenceRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("High-level offence", "code", code)