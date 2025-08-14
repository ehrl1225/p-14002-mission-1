package com.back.global.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.Map

class SecurityUser(
    val id: Int,
    username: String?,
    password: String?,
    val nickname: String?,
    authorities: MutableCollection<out GrantedAuthority?>
) : User(username, if (password != null) password else "", authorities), OAuth2User {
    override fun getAttributes(): MutableMap<String?, Any?> {
        return Map.of<String?, Any?>()
    }

    override fun getName(): String? {
        return getUsername()
    }
}