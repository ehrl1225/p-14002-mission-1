package com.back.domain.member.member.controller

import com.back.domain.member.member.service.MemberService
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiV1AdmMemberControllerTest {
    @Autowired
    private val memberService: MemberService? = null

    @Autowired
    private val mvc: MockMvc? = null

    @Test
    @DisplayName("다건조회")
    @WithUserDetails("admin")
    @Throws(Exception::class)
    fun t1() {
        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/members")
            )
            .andDo(MockMvcResultHandlers.print())

        val members = memberService!!.findAll()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1AdmMemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItems"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(members.size))

        for (i in members.indices) {
            val member = members.get(i)
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$[${i}].id").value(member.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[${i}].createDate")
                        .value<String?>(Matchers.startsWith(member.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[${i}].modifyDate")
                        .value<String?>(Matchers.startsWith(member.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$[${i}].name").value(member.name))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].username").value(member.username))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].isAdmin").value(member.isAdmin))
        }
    }

    @Test
    @DisplayName("다건조회, without permission")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t3() {
        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/members")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("권한이 없습니다."))
    }

    @Test
    @DisplayName("단건조회")
    @WithUserDetails("admin")
    @Throws(Exception::class)
    fun t2() {
        val id = 1

        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/members/" + id)
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberService!!.findById(id).get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1AdmMemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItem"))
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
    @DisplayName("단건조회, without permission")
    @WithUserDetails("user1")
    @Throws(Exception::class)
    fun t4() {
        val id = 1

        val resultActions = mvc!!
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/members/" + id)
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberService!!.findById(id).get()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("권한이 없습니다."))
    }
}
