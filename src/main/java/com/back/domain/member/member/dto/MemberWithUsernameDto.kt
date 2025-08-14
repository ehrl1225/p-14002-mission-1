package com.back.domain.member.member.dto

import com.back.domain.member.member.entity.Member
import lombok.Getter

@Getter
class MemberWithUsernameDto(member: Member) : MemberDto(member) {
    val username: String

    init {
        this.username = member.username
    }
}
