package com.back.domain.member.member.controller

import com.back.domain.member.member.service.MemberService
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {
    @Autowired
    private val memberService: MemberService? = null

    @Autowired
    private val mvc: MockMvc? = null

    @Test
    @DisplayName("회원가입")
    @Throws(Exception::class)
    fun t1() {
        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "username": "usernew",
                                            "password": "1234",
                                            "nickname": "무명"
                                        }
                                        
                                        """.trimIndent()
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberService!!.findByUsername("usernew")!!.get()
        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("join"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value("${member.name}님 환영합니다. 회원가입이 완료되었습니다.")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(member.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value<String?>(Matchers.startsWith(member.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifyDate")
                    .value<String?>(Matchers.startsWith(member.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(member.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.isAdmin").value(member.isAdmin))
    }

    @Test
    @DisplayName("로그인")
    @Throws(Exception::class)
    fun t2() {
        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                            "username": "user1",
                                            "password": "1234"
                                        }
                                        
                                        """.trimIndent()
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberService!!.findByUsername("user1")!!.get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("login"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${member.name}님 환영합니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.item").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.item.id").value(member.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.item.createDate")
                    .value<String?>(Matchers.startsWith(member.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.item.modifyDate")
                    .value<String?>(Matchers.startsWith(member.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.item.name").value(member.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.item.isAdmin").value(member.isAdmin))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.apiKey").value(member.apiKey))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").isNotEmpty())

        resultActions.andExpect(
            ResultMatcher { result: MvcResult? ->
                val apiKeyCookie = result!!.getResponse().getCookie("apiKey")
                Assertions.assertThat(apiKeyCookie!!.getValue()).isEqualTo(member.apiKey)
                Assertions.assertThat(apiKeyCookie.getPath()).isEqualTo("/")
                Assertions.assertThat(apiKeyCookie.getAttribute("HttpOnly")).isEqualTo("true")

                val accessTokenCookie = result.getResponse().getCookie("accessToken")
                Assertions.assertThat(accessTokenCookie!!.getValue()).isNotBlank()
                Assertions.assertThat(accessTokenCookie.getPath()).isEqualTo("/")
                Assertions.assertThat(accessTokenCookie.getAttribute("HttpOnly")).isEqualTo("true")
            }
        )
    }


    @Test
    @DisplayName("내 정보")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t3() {
        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.get("/api/v1/members/me")
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberService!!.findByUsername("user1")!!.get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("me"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(member.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.createDate")
                    .value<String?>(Matchers.startsWith(member.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.modifyDate")
                    .value<String?>(Matchers.startsWith(member.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(member.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(member.username))
            .andExpect(MockMvcResultMatchers.jsonPath("$.isAdmin").value(member.isAdmin))
    }

    @Test
    @DisplayName("내 정보, with apiKey Cookie")
    @Throws(Exception::class)
    fun t4() {
        val actor = memberService!!.findByUsername("user1")!!.get()
        val actorApiKey = actor.apiKey

        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.get("/api/v1/members/me")
                    .cookie(Cookie("apiKey", actorApiKey))
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberService.findByUsername("user1")!!.get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("me"))
            .andExpect(MockMvcResultMatchers.status().isOk())
    }


    @Test
    @DisplayName("로그아웃")
    @Throws(Exception::class)
    fun t6() {
        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/members/logout")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("logout"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그아웃 되었습니다."))
            .andExpect(ResultMatcher { result: MvcResult? ->
                val apiKeyCookie = result!!.getResponse().getCookie("apiKey")
                Assertions.assertThat(apiKeyCookie!!.getValue()).isEmpty()
                Assertions.assertThat(apiKeyCookie.getMaxAge()).isEqualTo(0)
                Assertions.assertThat(apiKeyCookie.getPath()).isEqualTo("/")
                Assertions.assertThat(apiKeyCookie.isHttpOnly()).isTrue()

                val accessTokenCookie = result.getResponse().getCookie("accessToken")
                Assertions.assertThat(accessTokenCookie!!.getValue()).isEmpty()
                Assertions.assertThat(accessTokenCookie.getMaxAge()).isEqualTo(0)
                Assertions.assertThat(accessTokenCookie.getPath()).isEqualTo("/")
                Assertions.assertThat(accessTokenCookie.isHttpOnly()).isTrue()
            })
    }

    @Test
    @DisplayName("엑세스 토큰이 만료되었거나 유효하지 않다면 apiKey를 통해서 재발급")
    @Throws(Exception::class)
    fun t7() {
        val actor = memberService!!.findByUsername("user1")!!.get()
        val actorApiKey = actor.apiKey

        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.get("/api/v1/members/me")
                    .header("Authorization", "Bearer " + actorApiKey + " wrong-access-token")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("me"))
            .andExpect(MockMvcResultMatchers.status().isOk())

        resultActions.andExpect(
            ResultMatcher { result: MvcResult? ->
                val accessTokenCookie = result!!.getResponse().getCookie("accessToken")
                Assertions.assertThat(accessTokenCookie!!.getValue()).isNotBlank()
                Assertions.assertThat(accessTokenCookie.getPath()).isEqualTo("/")
                Assertions.assertThat(accessTokenCookie.getAttribute("HttpOnly")).isEqualTo("true")

                val headerAuthorization = result.getResponse().getHeader("Authorization")
                Assertions.assertThat(headerAuthorization).isNotBlank()
                Assertions.assertThat(headerAuthorization).isEqualTo(accessTokenCookie.getValue())
            }
        )
    }

    @Test
    @DisplayName("Authorization 헤더가 Bearer 형식이 아닐 때 오류")
    @Throws(Exception::class)
    fun t8() {
        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.get("/api/v1/members/me")
                    .header("Authorization", "key")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Authorization 헤더가 Bearer 형식이 아닙니다."))
    }
}
