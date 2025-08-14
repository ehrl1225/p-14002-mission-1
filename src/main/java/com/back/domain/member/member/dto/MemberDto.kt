package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Getter
import java.time.LocalDateTime


open class MemberDto(member: Member) {
    val id: Int
    val createDate: LocalDateTime?
    val modifyDate: LocalDateTime?
    val name: String?

    @JsonProperty("isAdmin")
    val admin: Boolean
    val profileImageUrl: String?

    init {
        id = member.id
        createDate = member.createDate
        modifyDate = member.modifyDate
        name = member.name
        admin = member.isAdmin
        profileImageUrl = member.profileImgUrlOrDefault
    }
}
