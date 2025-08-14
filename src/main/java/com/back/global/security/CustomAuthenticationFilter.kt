package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import com.back.standard.util.Ut
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.function.Supplier

@Component
@RequiredArgsConstructor
class CustomAuthenticationFilter(
    private val memberService: MemberService,
    private val rq: Rq
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.debug("Processing request for " + request.getRequestURI())

        try {
            work(request, response, filterChain)
        } catch (e: ServiceException) {
            val rsData = e.rsData
            response.setContentType("application/json;charset=UTF-8")
            response.setStatus(rsData.statusCode)
            response.getWriter().write(
                Ut.json.toString(rsData)
            )
        }
    }

    @Throws(ServletException::class, IOException::class)
    private fun work(request: HttpServletRequest, response: HttpServletResponse?, filterChain: FilterChain) {
        // API 요청이 아니라면 패스
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response)
            return
        }

        // 인증, 인가가 필요없는 API 요청이라면 패스
        if (mutableListOf<String?>("/api/v1/members/login", "/api/v1/members/logout", "/api/v1/members/join").contains(
                request.getRequestURI()
            )
        ) {
            filterChain.doFilter(request, response)
            return
        }

        val apiKey: String
        val accessToken: String

        val headerAuthorization = rq?.getHeader("Authorization", "")?:""

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) throw ServiceException(
                "401-2",
                "Authorization 헤더가 Bearer 형식이 아닙니다."
            )

            val headerAuthorizationBits = headerAuthorization.split(" ".toRegex(), limit = 3).toTypedArray()

            apiKey = headerAuthorizationBits[1]
            accessToken = if (headerAuthorizationBits.size == 3) headerAuthorizationBits[2] else ""
        } else {
            apiKey = rq?.getCookieValue("apiKey", "")?:""
            accessToken = rq?.getCookieValue("accessToken", "")?:""
        }

        logger.debug("apiKey : " + apiKey)
        logger.debug("accessToken : " + accessToken)

        val isApiKeyExists = !apiKey.isBlank()
        val isAccessTokenExists = !accessToken.isBlank()

        if (!isApiKeyExists && !isAccessTokenExists) {
            filterChain.doFilter(request, response)
            return
        }

        var member: Member? = null
        var isAccessTokenValid = false

        if (isAccessTokenExists) {
            val payload = memberService!!.payload(accessToken)

            if (payload != null) {
                val id = payload.get("id") as Int
                val username = payload.get("username") as String
                val name = payload.get("name") as String
                member = Member(id, username, name)

                isAccessTokenValid = true
            }
        }

        if (member == null) {
            member = memberService!!
                .findByApiKey(apiKey)
                ?.orElseThrow<ServiceException?>{throw ServiceException("401-3", "API 키가 유효하지 않습니다.") }
        }

        if (isAccessTokenExists && !isAccessTokenValid) {
            val actorAccessToken = memberService!!.genAccessToken(member!!)

            rq?.setCookie("accessToken", actorAccessToken)
            rq?.setHeader("Authorization", actorAccessToken)
        }

        val user: UserDetails = SecurityUser(
            member!!.id,
            member!!.username,
            "",
            member!!.name,
            member!!.authorities
        )

        val authentication: Authentication = UsernamePasswordAuthenticationToken(
            user,
            user.getPassword(),
            user.getAuthorities()
        )

        // 이 시점 이후부터는 시큐리티가 이 요청을 인증된 사용자의 요청이다.
        SecurityContextHolder
            .getContext()
            .setAuthentication(authentication)

        filterChain.doFilter(request, response)
    }
}
