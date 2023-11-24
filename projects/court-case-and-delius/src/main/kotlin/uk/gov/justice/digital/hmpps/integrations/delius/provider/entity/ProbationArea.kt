package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
@Table(name = "probation_area")
class ProbationAreaEntity(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    val description: String,

    @Column(columnDefinition = "char(3)")
    val code: String,

    @Column(columnDefinition = "char(1)")
    val establishment: String?,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @OneToMany(mappedBy = "probationArea")
    val boroughs: List<Borough> = listOf()
)

@Immutable
@Entity
@Table(name = "district")
class District(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Column(name = "code")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,

    @Id
    @Column(name = "district_id")
    val id: Long
)

@Immutable
@Entity
@Table
class LocalDeliveryUnit(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Column(name = "code")
    val code: String,

    val description: String,

    @Id
    @Column(name = "local_delivery_unit_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "borough")
class Borough(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Id
    @Column(name = "borough_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationAreaEntity,

    @Column(name = "code")
    val code: String,

    @OneToMany(mappedBy = "borough")
    val districts: List<District> = listOf()

)
