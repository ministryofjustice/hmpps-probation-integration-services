package uk.gov.justice.digital.hmpps.data.manager

import jakarta.persistence.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.util.*
import jakarta.persistence.Version as JpaVersion
import org.springframework.data.annotation.Version as SpringVersion

interface DataManagerInterface {
    fun <T> saveAll(vararg entities: T?): List<T>
    fun <T> saveAll(entities: Collection<T>): List<T>
    fun <T> save(entity: T): T
}

@Component
@Transactional
@ConditionalOnClass(EntityManager::class)
class DataManager(private val entityManager: EntityManager) : DataManagerInterface {
    override fun <T> saveAll(vararg entities: T?) = saveAll(entities.toList())
    override fun <T> saveAll(entities: Collection<T>) = entities.filterNotNull().map { save(it) }
    override fun <T> save(entity: T): T = mergeGraph(entity)
        .also { entityManager.flush() }

    /**
     * This method attempts to simplify test data setup by being more lenient than the standard EntityManager
     * merge/persist. More specifically, it handles entities where the relationships have not been saved yet, by
     * cascading as required. This allows developers to construct the entire object graph and save it once, rather than
     * needing to save each entity individually in the right order.
     *
     * For example, the following just works:
     * ```kotlin
     * dataManager.save(
     *     Event(
     *         id = id(),
     *         person = PERSON,
     *         disposal = Disposal(id = id(), type = DISPOSAL_TYPE),
     *         mainOffence = MainOffence(id = id(), offence = OFFENCE),
     *         additionalOffences = listOf(AdditionalOffence(id = id(), offence = OFFENCE))
     *     )
     * )
     * ```
     * and is simpler than the following, which relies on the developer correctly figuring out the order of operations:
     * ```kotlin
     * entityManager.merge(PERSON)
     * entityManager.merge(OFFENCE)
     * entityManager.merge(DISPOSAL_TYPE)
     * entityManager.merge(Disposal(id = id(), type = DISPOSAL_TYPE))
     * entityManager.merge(MainOffence(id = id(), offence = OFFENCE))
     * entityManager.merge(AdditionalOffence(id = id(), offence = OFFENCE))
     * entityManager.merge(Event(...))
     * ```
     *
     * The following code is a bit of a hack and relies heavily on reflection, and definitely should not be used in
     * production, but it comes in handy for setting up test data.
     */
    fun <T> mergeGraph(entity: T, visited: MutableSet<Any> = Collections.newSetFromMap(IdentityHashMap())): T {
        if (entity == null || !visited.add(entity)) return entity

        // Reset the version to avoid optimistic locking
        entity.versionField()?.let { versionField ->
            entity.id()?.let { entityManager.find(entity.javaClass, it) }
                ?.let { existing ->
                    versionField.isAccessible = true
                    versionField.set(entity, versionField.get(existing) ?: 0)
                }
        }

        // Handle upstream dependencies (ensure we merge the parent before the child)
        entity.upstreamRelationships.forEach { field ->
            field.isAccessible = true
            val value = field.get(entity) ?: return@forEach
            if (Collection::class.java.isAssignableFrom(field.type)) {
                val managedUpstream = (value as Collection<*>).filterNotNull()
                    .map { mergeGraphIfNew(it, visited) }
                    .toType(field.type)
                field.set(entity, managedUpstream)
            } else {
                val managedUpstream = mergeGraphIfNew(value, visited)
                field.set(entity, managedUpstream)
            }
        }

        // Handle downstream dependencies (nullify children and keep a list of the original values so we can merge them after)
        val childFields = entity.downstreamRelationships.mapNotNull { field ->
            field.isAccessible = true
            val value = field.get(entity) ?: return@mapNotNull null
            if (Collection::class.java.isAssignableFrom(field.type)) {
                field.set(entity, emptyCollectionOfType(field.type))
                field to (value as Collection<*>).filterNotNull()
            } else {
                field.set(entity, null)
                field to listOf(value)
            }
        }

        // Merge the current entity
        val managedEntity = entityManager.merge(entity)

        // For the upstream relationships, update the parent's back-reference to the newly managed child
        managedEntity.upstreamRelationships.forEach { parentField ->
            parentField.isAccessible = true
            val parent = parentField.get(managedEntity) ?: return@forEach
            parent.downstreamRelationships.filter { it.type == managedEntity.javaClass }.forEach { childField ->
                childField.isAccessible = true
                childField.set(parent, managedEntity)
            }
        }

        // For the downstream relationships, save the child entity and reattach to the parent
        childFields.forEach { (field, values) ->
            val mergedChildren = values.map { child ->
                // Update the child's back-reference to the newly managed parent
                runCatching { child.javaClass.getDeclaredField(field.mappedBy!!) }.getOrNull()?.apply {
                    isAccessible = true
                    set(child, managedEntity)
                }

                // Recurse downstream
                mergeGraph(child, visited)
            }.toType(field.type)

            // Reattach to both the original and the managed entity
            field.set(managedEntity, mergedChildren)
            field.set(entity, mergedChildren)
        }

        return managedEntity
    }

    private fun mergeGraphIfNew(entity: Any, visited: MutableSet<Any>) =
        entityManager.find(entity.javaClass, entity.id()) ?: mergeGraph(entity, visited)

    private val AccessibleObject.mappedBy
        get() = when {
            isAnnotationPresent(ManyToMany::class.java) -> getAnnotation(ManyToMany::class.java).mappedBy
            isAnnotationPresent(OneToMany::class.java) -> getAnnotation(OneToMany::class.java).mappedBy
            isAnnotationPresent(OneToOne::class.java) -> getAnnotation(OneToOne::class.java).mappedBy
            else -> ""
        }.takeIf { it != "" }

    private val <T : Any> T.upstreamRelationships: List<Field>
        get() = this.javaClass.declaredFields.filter {
            it.isAnnotationPresent(ManyToOne::class.java)
                || (it.isAnnotationPresent(OneToOne::class.java) && it.mappedBy == null)
                || (it.isAnnotationPresent(ManyToMany::class.java) && it.mappedBy == null)
        }

    private val <T : Any> T.downstreamRelationships: List<Field>
        get() = this.javaClass.declaredFields.filter { it.mappedBy != null }.filterNotNull()

    private fun emptyCollectionOfType(type: Class<*>): Collection<Any> = when {
        Set::class.java.isAssignableFrom(type) -> mutableSetOf()
        List::class.java.isAssignableFrom(type) -> mutableListOf()
        else -> throw IllegalStateException("Unsupported collection type: $type")
    }

    private fun <T : Any> List<T>.toType(type: Class<*>): Any = when {
        Set::class.java.isAssignableFrom(type) -> toMutableSet()
        List::class.java.isAssignableFrom(type) -> toMutableList()
        Collection::class.java.isAssignableFrom(type) -> throw IllegalStateException("Unsupported collection type: $type")
        else -> single()
    }

    private fun <T : Any> T.id(): Any? = entityManager.entityManagerFactory.persistenceUnitUtil.getIdentifier(this)
    private fun <T : Any> T.versionField(): Field? = javaClass.declaredFields
        .find { it.isAnnotationPresent(JpaVersion::class.java) || it.isAnnotationPresent(SpringVersion::class.java) }
}
