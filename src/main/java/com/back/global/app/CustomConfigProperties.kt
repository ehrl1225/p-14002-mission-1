package com.back.global.app

import lombok.Getter
import lombok.Setter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "custom")
class CustomConfigProperties {
    var notProdMembers: MutableList<NotProdMember>? = null

    data class NotProdMember(
        @JvmField val username: String?,
        @JvmField val apiKey: String?,
        @JvmField val nickname: String?,
        @JvmField val profileImgUrl: String?
    )
}
