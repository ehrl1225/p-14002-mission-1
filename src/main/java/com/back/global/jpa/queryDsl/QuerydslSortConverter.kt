package com.back.global.jpa.queryDsl

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import org.springframework.data.domain.Sort

class QuerydslSortConverter {
    companion object{

        fun <T : Any> toOrderSpecifiers(
            sort: Sort,
            entityClass: Class<T>,
            alias: String
        ): List<OrderSpecifier<*>> {
            val pathBuilder = PathBuilder(entityClass, alias)
            val specifiers = mutableListOf<OrderSpecifier<*>>()

            for (order in sort) {
                val direction = if (order.isAscending) Order.ASC else Order.DESC

                val expression = pathBuilder.get(order.property)
                val comparableExpression = try{
                    expression as Expression<Comparable<*>>
                }catch(e:ClassCastException){
                    throw IllegalArgumentException("Property '${order.property}' is not Comparable and cannot be used for sorting")
                }

                specifiers.add(OrderSpecifier(direction, comparableExpression))
            }

            return specifiers
        }

    }
}