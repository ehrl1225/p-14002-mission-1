package com.back.domain.post.post.dto

import com.back.domain.post.post.entity.Post
import lombok.Getter
import java.time.LocalDateTime


open class PostDto(post: Post) {
    val id: Int
    val createDate: LocalDateTime?
    val modifyDate: LocalDateTime?
    val authorId: Int
    val authorName: String
    val title: String?

    init {
        id = post.id
        createDate = post.createDate
        modifyDate = post.modifyDate
        authorId = post.author.id
        authorName = post.author.name
        title = post.title
    }
}