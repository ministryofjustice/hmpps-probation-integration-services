package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.SEQUENCE
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "r_detailed_offence")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "detailed_offence_id_seq", sequenceName = "detailed_offence_id_seq", allocationSize = 1)
class DetailedOffence(
    @Id
    @Column(name = "detailed_offence_id")
    @GeneratedValue(strategy = SEQUENCE, generator = "detailed_offence_id_seq")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    var version: Long = 0,

    @Column(name = "cja_code", columnDefinition = "varchar2(10)")
    var code: String, // Criminal Justice System code

    @Column(name = "offence_description")
    var description: String?, // Criminal Justice System title

    @Column
    var startDate: LocalDate,

    @Column
    var endDate: LocalDate?,

    @Column(name = "ho_code", columnDefinition = "varchar2(6)")
    var homeOfficeCode: String?,

    @Column(name = "ho_description")
    var homeOfficeDescription: String?,

    @Column(columnDefinition = "varchar2(8)")
    var pnldCode: String? = null, // Police National Legal Database code

    @Column
    var legislation: String?,

    @ManyToOne
    @JoinColumn(name = "court_category_id")
    var category: ReferenceData,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_sexual_offence")
    var schedule15SexualOffence: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "schedule15_violent_offence")
    var schedule15ViolentOffence: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "cja_2003_s327_4a")
    var cjaSection327Subsection4A: Boolean? = null, // Offences listed in CJA 2003 section 327, subsection 4A.

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime? = null,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime? = null
)
