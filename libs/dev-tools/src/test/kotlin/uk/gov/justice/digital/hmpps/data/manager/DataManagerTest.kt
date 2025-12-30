package uk.gov.justice.digital.hmpps.data.manager

import jakarta.persistence.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class DataManagerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var entityManager: EntityManager

    @InjectMocks
    private lateinit var dataManager: DataManager

    @Test
    fun `save single entity merges and flushes`() {
        val entity = ParentEntity(1L, "test")
        whenever(entityManager.merge<Any>(any())).thenAnswer { it.arguments[0] }

        val result = dataManager.save(entity)

        assertThat(result).isEqualTo(entity)
        inOrder(entityManager).run {
            verify(entityManager).merge(entity)
            verify(entityManager).flush()
        }
    }

    @Test
    fun `saveAll with varargs filters nulls and saves each entity`() {
        val entity1 = ParentEntity(1L, "test1")
        val entity2 = ParentEntity(2L, "test2")
        whenever(entityManager.merge<Any>(any())).thenAnswer { it.arguments[0] }

        val results = dataManager.saveAll(entity1, null, entity2)

        assertThat(results).hasSize(2)
        inOrder(entityManager).run {
            verify(entityManager).merge(entity1)
            verify(entityManager).flush()
            verify(entityManager).merge(entity2)
            verify(entityManager).flush()
        }
    }

    @Test
    fun `saveAll with collection saves each entity`() {
        val entity1 = ParentEntity(1L, "test1")
        val entity2 = ParentEntity(2L, "test2")
        whenever(entityManager.merge<Any>(any())).thenAnswer { it.arguments[0] }

        val results = dataManager.saveAll(listOf(entity1, null, entity2))

        assertThat(results).hasSize(2)
        inOrder(entityManager).run {
            verify(entityManager).merge(entity1)
            verify(entityManager).flush()
            verify(entityManager).merge(entity2)
            verify(entityManager).flush()
        }
    }

    @Test
    fun `mergeGraph handles entity with ManyToOne relationship`() {
        val parent = ParentEntity(1L, "parent")
        val child = ChildEntity(2L, "child", parent)

        whenever(entityManager.entityManagerFactory.persistenceUnitUtil.getIdentifier(parent)).thenReturn(1L)
        whenever(entityManager.find(ParentEntity::class.java, 1L)).thenReturn(null)
        whenever(entityManager.merge<Any>(any())).thenAnswer { it.arguments[0] }

        dataManager.save(child)

        inOrder(entityManager).run {
            verify(entityManager).merge(parent)
            verify(entityManager).merge(child)
            verify(entityManager).flush()
        }
    }

    @Test
    fun `mergeGraph handles entity with OneToMany relationship`() {
        val parent = ParentEntity(1L, "parent")
        val child = ChildEntity(2L, "child", parent)
        parent.children = mutableListOf(child)

        whenever(entityManager.entityManagerFactory.persistenceUnitUtil.getIdentifier(parent)).thenReturn(1L)
        whenever(entityManager.find(ParentEntity::class.java, 1L)).thenReturn(null)
        whenever(entityManager.merge<Any>(any())).thenAnswer { it.arguments[0] }

        dataManager.save(parent)

        inOrder(entityManager).run {
            verify(entityManager).merge(parent)
            verify(entityManager).merge(child)
            verify(entityManager).flush()
        }
    }

    @Test
    fun `mergeGraph handles existing entity with version field`() {
        val existingEntity = VersionedEntity(1L, "existing", 5)
        val newEntity = VersionedEntity(1L, "updated", 0)

        whenever(entityManager.entityManagerFactory.persistenceUnitUtil.getIdentifier(newEntity)).thenReturn(1L)
        whenever(entityManager.find(VersionedEntity::class.java, 1L)).thenReturn(existingEntity)
        whenever(entityManager.merge<Any>(any())).thenAnswer { it.arguments[0] }

        dataManager.save(newEntity)

        assertThat(newEntity.version).isEqualTo(5)
        verify(entityManager).merge(newEntity)
        verify(entityManager).flush()
    }

    @Test
    fun `mergeGraph reuses existing parent entity`() {
        val existingParent = ParentEntity(1L, "existing")
        val child = ChildEntity(2L, "child", ParentEntity(1L, "new"))

        whenever(entityManager.entityManagerFactory.persistenceUnitUtil.getIdentifier(any()))
            .thenAnswer { (it.arguments[0] as ParentEntity).id }
        whenever(entityManager.find(ParentEntity::class.java, 1L)).thenReturn(existingParent)
        whenever(entityManager.merge<Any>(any())).thenAnswer { it.arguments[0] }

        dataManager.save(child)

        verify(entityManager).merge(child)
        verify(entityManager, never()).merge(child.parent)
        verify(entityManager, never()).merge(existingParent)
    }

    @Test
    fun `mergeGraph handles Set collection type`() {
        val parent = ParentWithSet(1L, "parent")
        val child = ChildWithSet(2L, "child", parent)
        parent.children = mutableSetOf(child)

        whenever(entityManager.entityManagerFactory.persistenceUnitUtil.getIdentifier(parent)).thenReturn(1L)
        whenever(entityManager.find<Any>(any(), any())).thenReturn(null)
        whenever(entityManager.merge<Any>(any())).thenAnswer { it.arguments[0] }

        val result = dataManager.save(parent)

        assertThat(result.children).isInstanceOf(MutableSet::class.java)
        inOrder(entityManager).run {
            verify(entityManager).merge(parent)
            verify(entityManager).merge(child)
            verify(entityManager).flush()
        }
    }

    @Test
    fun `mergeGraph handles null entity`() {
        val result = dataManager.save<Any?>(null)
        assertThat(result).isNull()
        verify(entityManager, never()).merge<Any>(any())
    }

    // Test entities
    @Entity
    data class ParentEntity(
        @Id val id: Long,
        val name: String,
        @OneToMany(mappedBy = "parent")
        var children: MutableList<ChildEntity> = mutableListOf()
    )

    @Entity
    data class ChildEntity(
        @Id val id: Long,
        val name: String,
        @ManyToOne
        var parent: ParentEntity?
    )

    @Entity
    data class VersionedEntity(
        @Id val id: Long,
        val name: String,
        @Version var version: Int = 0
    )

    @Entity
    data class ParentWithSet(
        @Id val id: Long,
        val name: String,
        @OneToMany(mappedBy = "parent")
        var children: MutableSet<ChildWithSet> = mutableSetOf()
    )

    @Entity
    data class ChildWithSet(
        @Id val id: Long,
        val name: String,
        @ManyToOne
        var parent: ParentWithSet?
    )
}
