package com.back.domain.member.member.entity

import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

@Entity
class Member(
    @Column(unique = true)
    var username: String,
    var password: String,
    var name: String,
): BaseEntity() {

    @Column(unique = true)
    var apiKey: String? = null
    var profileImgUrl: String? = null
        private set

    constructor(username: String, password: String, nickname: String, profileImgUrl: String):this(username, password, nickname) {
        this.profileImgUrl = profileImgUrl
        this.apiKey = UUID.randomUUID().toString()
    }

    constructor(id:Int, username: String, name:String):this(username, "", name) {
        this.id = id
    }

    fun modifyApiKey(apiKey: String?) {
        this.apiKey = apiKey
    }

    val isAdmin: Boolean
        get() {
            if ("system" == username) return true
            if ("admin" == username) return true

            return false
        }

    val authorities: MutableCollection<out GrantedAuthority?>
        get() = this.authoritiesAsStringList
            .stream()
            .map<SimpleGrantedAuthority?> { role: String? -> SimpleGrantedAuthority(role) }
            .toList()

    private val authoritiesAsStringList: MutableList<String?>
        get() {
            val authorities: MutableList<String?> = ArrayList<String?>()

            if (this.isAdmin) authorities.add("ROLE_ADMIN")

            return authorities
        }

    fun modify(nickname: String, profileImgUrl: String) {
        this.name = nickname
        this.profileImgUrl = profileImgUrl
    }

    val profileImgUrlOrDefault: String
        get() {
            if (profileImgUrl == null) return "https://placehold.co/600x600?text=U_U"

            return profileImgUrl!!
        }
}
