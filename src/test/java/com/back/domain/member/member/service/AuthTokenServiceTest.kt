package com.back.domain.member.member.service

import com.back.standard.util.Ut
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.Map

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthTokenServiceTest {
    @Autowired
    private val memberService: MemberService? = null

    @Autowired
    private val authTokenService: AuthTokenService? = null

    @Value("\${custom.jwt.secretKey}")
    private val jwtSecretKey: String? = null

    @Value("\${custom.accessToken.expirationSeconds}")
    private val accessTokenExpirationSeconds = 0

    @Test
    @DisplayName("authTokenService 서비스가 존재한다.")
    fun t1() {
        Assertions.assertThat<AuthTokenService?>(authTokenService).isNotNull()
    }

    @Test
    @DisplayName("jjwt 최신 방식으로 JWT 생성, {name=\"Paul\", age=23}")
    fun t2() {
        // 토큰 만료기간: 1년
        val expireMillis = 1000L * accessTokenExpirationSeconds

        val keyBytes = jwtSecretKey!!.toByteArray(StandardCharsets.UTF_8)
        val secretKey = Keys.hmacShaKeyFor(keyBytes)

        // 발행 시간과 만료 시간 설정
        val issuedAt = Date()
        val expiration = Date(issuedAt.getTime() + expireMillis)

        val payload = Map.of<String?, Any?>(
            "name", "Paul",
            "age", 23
        )

        val jwt = Jwts.builder()
            .claims(payload) // 내용
            .issuedAt(issuedAt) // 생성날짜
            .expiration(expiration) // 만료날짜
            .signWith(secretKey) // 키 서명
            .compact()

        Assertions.assertThat(jwt).isNotBlank()

        println("jwt = " + jwt)

        // 키가 유효한지 테스트
        val parsedPayload = Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parse(jwt)
            .getPayload() as MutableMap<String?, Any?>?

        Assertions.assertThat<String?, Any?>(parsedPayload)
            .containsAllEntriesOf(payload)
    }

    @Test
    @DisplayName("Ut.jwt.toString 를 통해서 JWT 생성, {name=\"Paul\", age=23}")
    fun t3() {
        val payload = Map.of<String?, Any?>("name", "Paul", "age", 23)

        val jwt = Ut.jwt.toString(
            jwtSecretKey!!,
            accessTokenExpirationSeconds,
            payload
        )

        Assertions.assertThat(jwt).isNotBlank()

        Assertions.assertThat(Ut.jwt.isValid(jwtSecretKey, jwt))
            .isTrue()

        val parsedPayload = Ut.jwt.payload(jwtSecretKey, jwt)

        Assertions.assertThat<String?, Any?>(parsedPayload).containsAllEntriesOf(payload)
    }

    @Test
    @DisplayName("authTokenService.genAccessToken(member);")
    fun t4() {
        val memberUser1 = memberService!!.findByUsername("user1")!!.get()

        val accessToken = authTokenService!!.genAccessToken(memberUser1)

        Assertions.assertThat(accessToken).isNotBlank()

        println("accessToken = " + accessToken)

        val parsedPayload = authTokenService.payload(accessToken)

        Assertions.assertThat<String?, Any?>(parsedPayload)
            .containsAllEntriesOf(
                Map.of(
                    "id", memberUser1.id,
                    "username", memberUser1.username,
                    "name", memberUser1.name
                )
            )
    }
}