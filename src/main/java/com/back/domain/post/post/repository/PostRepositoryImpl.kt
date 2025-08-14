package com.back.domain.post.post.repository

import com.back.domain.post.post.entity.Post
import com.back.domain.post.post.entity.QPost
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import com.back.global.jpa.queryDsl.QuerydslSortConverter
import com.querydsl.core.BooleanBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

class PostRepositoryImpl(
    private val queryFactory: JPAQueryFactory
):PostRepositoryCustom {

    override fun findPageableByKeyword(pageable: Pageable, keyword: String?): Page<Post> {
        val specification = QuerydslSortConverter.toOrderSpecifiers(pageable.sort, Post::class.java, "post")
        val builder = BooleanBuilder()
        if (!keyword.isNullOrEmpty()) builder.and(QPost.post.title.containsIgnoreCase(keyword).or(QPost.post.content.containsIgnoreCase(keyword))
        )
        val posts:List<Post> = queryFactory
            .selectFrom(QPost.post)
            .where(builder)
            .orderBy(*specification.toTypedArray())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        val total = queryFactory
            .select(QPost.post.count())
            .from(QPost.post)
            .fetchOne() ?: 0L

        return PageImpl(posts, pageable, total)
    }

}