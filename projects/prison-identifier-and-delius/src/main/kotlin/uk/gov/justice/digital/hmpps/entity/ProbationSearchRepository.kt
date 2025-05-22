package uk.gov.justice.digital.hmpps.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.client.ProbationMatchRequest
import uk.gov.justice.digital.hmpps.utils.SearchHelpers
import uk.gov.justice.digital.hmpps.utils.SearchHelpers.allLenientDateVariations
import java.time.LocalDate

interface ProbationSearchRepository : JpaRepository<Person, Long> {
    @Query(
        """
            select distinct p from Person p
            where
                (:croNumber is null or (:croNumber is not null and lower(trim(p.croNumber)) = lower(:croNumber)))
                and
                (
                :pncNumber is null or (
                    lower(trim(p.pncNumber)) = lower(trim(:pncNumber)) or
                    lower(trim(substr(p.pncNumber,3))) = lower(trim(substr(:pncNumber,3))) or
                    lower(trim(substr(p.pncNumber,3))) = lower(trim(:pncNumber)) or
                    lower(trim(p.pncNumber)) = lower(trim(substr(:pncNumber,3)))
                    )
                )
                and
                (:nomsNumber is null or (:nomsNumber is not null and lower(trim(p.nomsNumber)) = lower(:nomsNumber)))
                and
                (:activeSentence != true or (:activeSentence = true and p.currentDisposal = true ))
                and
                (:forename is null or (:forename is not null and lower(trim(p.forename)) = lower(:forename)))
                and 
                (:surname is null or (:surname is not null and lower(trim(p.surname)) = lower(:surname)))
                and 
                (:dateOfBirth is null or (:dateOfBirth is not null and p.dateOfBirth = :dateOfBirth))
                and exists (select 1 from OffenderManager om
                            where om.personId = p.id
                            and om.active = true
                            and om.softDeleted = false)                
                
        """
    )
    fun personFullMatchAllSupplied(
        pncNumber: String?,
        croNumber: String?,
        nomsNumber: String?,
        activeSentence: Boolean = false,
        forename: String?,
        surname: String?,
        dateOfBirth: LocalDate? = null,
    ): List<Person>

    @Query(
        """
            select distinct p from Person p join Alias a on p.id = a.offenderId
            where
                (:croNumber is null or (:croNumber is not null and lower(trim(p.croNumber)) = lower(:croNumber)))
                and
                (
                :pncNumber is null or (
                    lower(trim(p.pncNumber)) = lower(trim(:pncNumber)) or
                    lower(trim(substr(p.pncNumber,3))) = lower(trim(substr(:pncNumber,3))) or
                    lower(trim(substr(p.pncNumber,3))) = lower(trim(:pncNumber)) or
                    lower(trim(p.pncNumber)) = lower(trim(substr(:pncNumber,3)))
                ))
                and
                (:nomsNumber is null or (:nomsNumber is not null and lower(trim(p.nomsNumber)) = lower(:nomsNumber)))
                and
                (:activeSentence != true or (:activeSentence = true and p.currentDisposal = true ))
                and
                (:forename is null or (:forename is not null and lower(trim(a.forename)) = lower(:forename)))
                and 
                (:surname is null or (:surname is not null and lower(trim(a.surname)) = lower(:surname)))
                and 
                (:dateOfBirth is null or (:dateOfBirth is not null and a.dateOfBirth = :dateOfBirth))
                and exists (select 1 from OffenderManager om
                            where om.personId = p.id
                            and om.active = true
                            and om.softDeleted = false)                
                
        """
    )
    fun personFullMatchAliasAllSupplied(
        pncNumber: String?,
        croNumber: String?,
        nomsNumber: String?,
        activeSentence: Boolean = false,
        forename: String?,
        surname: String?,
        dateOfBirth: LocalDate? = null,
    ): List<Person>

    @Query(
        """
            select distinct p 
            from Person p 
            where :nomsNumber is not null and lower(trim(p.nomsNumber)) = lower(trim(:nomsNumber))
            and (:activeSentence != true or (:activeSentence = true and p.currentDisposal = true ))
            and exists (select 1 from OffenderManager om
                        where om.personId = p.id
                        and om.active = true
                        and om.softDeleted = false)
        """
    )
    fun findPersonByNomsNumber(nomsNumber: String?, activeSentence: Boolean): List<Person>

    @Query(
        """
            select distinct p 
            from Person p left join Alias a on a.offenderId = p.id
            where :croNumber is not null and lower(trim(p.croNumber)) = lower(trim(:croNumber))
            and (:activeSentence != true or (:activeSentence = true and p.currentDisposal = true ))
            and (
                ((:surname is not null and (
                    lower(trim(p.surname)) = lower(trim(:surname)) 
                    or lower(trim(a.surname)) = lower(trim(:surname))
                ))
                or (:dateOfBirth is not null and ( 
                    p.dateOfBirth = :dateOfBirth
                    or a.dateOfBirth = :dateOfBirth)
                )) or (:dateOfBirth is null and :surname is null)   
            )
            and exists (select 1 from OffenderManager om
                        where om.personId = p.id
                        and om.active = true
                        and om.softDeleted = false)
        """
    )
    fun findPersonByCroNumber(
        croNumber: String?,
        surname: String?,
        dateOfBirth: LocalDate?,
        activeSentence: Boolean
    ): List<Person>

    @Query(
        """
            select distinct p 
            from Person p left join Alias a on a.offenderId = p.id
            where :pncNumber is not null and 
            (    
                lower(trim(p.pncNumber)) = lower(trim(:pncNumber)) or
                lower(trim(substr(p.pncNumber,3))) = lower(trim(substr(:pncNumber,3))) or
                lower(trim(substr(p.pncNumber,3))) = lower(trim(:pncNumber)) or
                lower(trim(p.pncNumber)) = lower(trim(substr(:pncNumber,3)))
            )
            and (
                ((:surname is not null and (
                    lower(trim(p.surname)) = lower(trim(:surname)) 
                    or lower(trim(a.surname)) = lower(trim(:surname))
                ))
                or (:dateOfBirth is not null and ( 
                    p.dateOfBirth = :dateOfBirth
                    or a.dateOfBirth = :dateOfBirth)
                )) or (:dateOfBirth is null and :surname is null)      
            )
            and (:activeSentence != true or (:activeSentence = true and p.currentDisposal = true ))
            and exists (select 1 from OffenderManager om
                        where om.personId = p.id
                        and om.active = true
                        and om.softDeleted = false)
        """
    )
    fun findPersonByPncNumber(
        pncNumber: String?,
        surname: String?,
        dateOfBirth: LocalDate?,
        activeSentence: Boolean
    ): List<Person>

    @Query(
        """
            select distinct p 
            from Person p left join Alias a on a.offenderId = p.id
            where 
            (:activeSentence != true or (:activeSentence = true and p.currentDisposal = true ))
            and
            (
                (
                    (:forename is null or (:forename is not null and lower(trim(a.forename)) = lower(:forename)))
                    and 
                    (:surname is null or (:surname is not null and lower(trim(a.surname)) = lower(:surname)))
                    and 
                    (:dateOfBirth is null or (:dateOfBirth is not null and a.dateOfBirth = :dateOfBirth))
                )
                or
                (
                    (:forename is null or (:forename is not null and lower(trim(p.forename)) = lower(:forename)))
                    and 
                    (:surname is null or (:surname is not null and lower(trim(p.surname)) = lower(:surname)))
                    and 
                    (:dateOfBirth is null or (:dateOfBirth is not null and p.dateOfBirth = :dateOfBirth))
                )
            )
            and exists (select 1 from OffenderManager om
                        where om.personId = p.id
                        and om.active = true
                        and om.softDeleted = false)
        """
    )
    fun findPersonByName(
        forename: String?,
        surname: String?,
        dateOfBirth: LocalDate?,
        activeSentence: Boolean,
    ): List<Person>

    @Query(
        """
            select distinct p 
            from Person p
            where
            (:activeSentence != true or (:activeSentence = true and p.currentDisposal = true ))
            and
            (
                (:surname is null or (:surname is not null and lower(trim(p.surname)) = lower(:surname)))
                and 
                (:dateOfBirth is null or (:dateOfBirth is not null and p.dateOfBirth = :dateOfBirth))
            )
            and exists (select 1 from OffenderManager om
                        where om.personId = p.id
                        and om.active = true
                        and om.softDeleted = false)
        """
    )
    fun findPersonByPartialName(
        surname: String?,
        dateOfBirth: LocalDate?,
        activeSentence: Boolean,
    ): List<Person>

    @Query(
        """
            select distinct p 
            from Person p left join Alias a on a.offenderId = p.id
            where
            (:activeSentence != true or (:activeSentence = true and p.currentDisposal = true ))
            and
            (
                (:forename is null or (:forename is not null and (lower(trim(p.forename)) = lower(:forename) or lower(trim(a.forename)) = lower(:forename))))
                and
                (:surname is null or (:surname is not null and lower(trim(p.surname)) = lower(:surname)))
                and 
                (p.dateOfBirth in (:dateOfBirths))
            )
            and exists (select 1 from OffenderManager om
                        where om.personId = p.id
                        and om.active = true
                        and om.softDeleted = false)
        """
    )
    fun findPersonByPartialNameLenientDob(
        forename: String?,
        surname: String?,
        dateOfBirths: List<LocalDate>,
        activeSentence: Boolean = false
    ): List<Person>
}

fun ProbationSearchRepository.fullSearch(request: ProbationMatchRequest): List<Person> = personFullMatchAllSupplied(
    pncNumber = request.pncNumber?.takeIf { it.isNotEmpty() }?.let { SearchHelpers.formatPncNumber(it) },
    nomsNumber = request.nomsNumber.takeIf { it.isNotEmpty() },
    croNumber = request.croNumber?.takeIf { it.isNotEmpty() },
    activeSentence = request.activeSentence,
    forename = request.firstName.takeIf { it.isNotEmpty() },
    surname = request.surname.takeIf { it.isNotEmpty() },
    dateOfBirth = request.dateOfBirth
)

fun ProbationSearchRepository.fullSearchAlias(request: ProbationMatchRequest): List<Person> =
    personFullMatchAliasAllSupplied(
        pncNumber = request.pncNumber?.takeIf { it.isNotEmpty() }?.let { SearchHelpers.formatPncNumber(it) },
        nomsNumber = request.nomsNumber.takeIf { it.isNotEmpty() },
        croNumber = request.croNumber?.takeIf { it.isNotEmpty() },
        activeSentence = request.activeSentence,
        forename = request.firstName.takeIf { it.isNotEmpty() },
        surname = request.surname.takeIf { it.isNotEmpty() },
        dateOfBirth = request.dateOfBirth
    )

fun ProbationSearchRepository.searchByNoms(request: ProbationMatchRequest): List<Person> =
    findPersonByNomsNumber(request.nomsNumber.takeIf { it.isNotEmpty() }, request.activeSentence)

fun ProbationSearchRepository.searchByCro(request: ProbationMatchRequest): List<Person> =
    findPersonByCroNumber(
        request.croNumber?.takeIf { it.isNotEmpty() },
        request.surname.takeIf { it.isNotEmpty() },
        request.dateOfBirth,
        request.activeSentence
    )

fun ProbationSearchRepository.searchByPnc(request: ProbationMatchRequest): List<Person> =
    findPersonByPncNumber(
        request.pncNumber?.takeIf { it.isNotEmpty() }?.let { SearchHelpers.formatPncNumber(it) },
        request.surname.takeIf { it.isNotEmpty() },
        request.dateOfBirth,
        request.activeSentence
    )

fun ProbationSearchRepository.searchByName(request: ProbationMatchRequest): List<Person> =
    findPersonByName(
        request.firstName.takeIf { it.isNotEmpty() },
        request.surname.takeIf { it.isNotEmpty() },
        request.dateOfBirth,
        request.activeSentence
    )

fun ProbationSearchRepository.searchByPartialName(request: ProbationMatchRequest): List<Person> =
    findPersonByPartialName(request.surname.takeIf { it.isNotEmpty() }, request.dateOfBirth, request.activeSentence)

fun ProbationSearchRepository.searchByPartialNameLenientDob(request: ProbationMatchRequest): List<Person> {
    return findPersonByPartialNameLenientDob(
        request.firstName.takeIf { it.isNotEmpty() },
        request.surname.takeIf { it.isNotEmpty() }, allLenientDateVariations(request.dateOfBirth)
    )
}
