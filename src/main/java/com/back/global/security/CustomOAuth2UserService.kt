package com.back.global.security

import com.back.domain.member.member.service.MemberService
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@RequiredArgsConstructor
@Slf4j
class CustomOAuth2UserService(
    private val memberService: MemberService
) : DefaultOAuth2UserService() {


    // 카카오톡 로그인이 성공할 때 마다 이 함수가 실행된다.
    @Transactional
    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        var oauthUserId: String? = ""
        val providerTypeCode = userRequest.getClientRegistration().getRegistrationId().uppercase(Locale.getDefault())

        var nickname = ""
        var profileImgUrl = ""
        var username = ""

        when (providerTypeCode) {
            "KAKAO" -> {
                val attributes = oAuth2User.getAttributes()
                val attributesProperties = attributes.get("properties") as MutableMap<String?, Any?>

                oauthUserId = oAuth2User.getName()
                nickname = attributesProperties.get("nickname") as String
                profileImgUrl = attributesProperties.get("profile_image") as String
            }

            "GOOGLE" -> {
                oauthUserId = oAuth2User.getName()
                nickname = oAuth2User.getAttributes().get("name") as String
                profileImgUrl = oAuth2User.getAttributes().get("picture") as String
            }

            "NAVER" -> {
                val attributes = oAuth2User.getAttributes()
                val attributesProperties = attributes.get("response") as MutableMap<String?, Any?>

                oauthUserId = attributesProperties.get("id") as String?
                nickname = attributesProperties.get("nickname") as String
                profileImgUrl = attributesProperties.get("profile_image") as String
            }
        }

        username = providerTypeCode + "__${oauthUserId}"
        val password = ""

        val member = memberService!!.modifyOrJoin(username, password, nickname, profileImgUrl).data

        return SecurityUser(
            member!!.id,
            member.username,
            member.password,
            member.name,
            member.authorities
        )
    }
}