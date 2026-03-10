package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
@Table(name = "registration")
class Registration(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deRegistered: Boolean,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

interface RegistrationRepository : JpaRepository<Registration, Long> {

    @EntityGraph(attributePaths = ["type"])
    fun findByPersonId(personId: Long): List<Registration>

    @Query("""select case 
       when r.category.code = 'M1' then 1
       when r.category.code = 'M2' then 2
       when r.category.code = 'M3' then 3
       when r.category.code = 'M4' then 4
       else 0 end
        from Registration r
        where r.personId = :personId and r.type.code = 'MAPP' order by r.id desc""")
    fun findByMappaCategoryByPersonId(personId: Long, pageRequest: PageRequest = PageRequest.of(0,1)): Int?

    @EntityGraph(attributePaths = ["type", "category"])
    fun findFirstByPersonIdAndTypeCodeOrderByIdDesc(personId: Long, typeCode: String): Registration?
}

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(

    val code: String,

    val description: String,

    val colour: String,

    @Id
    @Column(name = "register_type_id")
    val id: Long,
) {

}
