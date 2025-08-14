package com.back.global.jpa.entity

import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Setter
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass // 엔티티의 부모 클래스에는 이걸 달아야 한다.
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED)
    var id: Int = 0

    @CreatedDate
    var createDate: LocalDateTime? = null

    @LastModifiedDate
    var modifyDate: LocalDateTime? = null

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as BaseEntity
        return id == that.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }
}