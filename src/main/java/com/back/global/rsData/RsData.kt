package com.back.global.rsData

import com.fasterxml.jackson.annotation.JsonIgnore


data class RsData<T>(
    val resultCode: String?,
    val statusCode: Int,
    val msg: String?,
    val data: T?
) {
    @JvmOverloads
    constructor(resultCode: String, msg: String?, data: T? = null) : this(
        resultCode,
        resultCode.split("-".toRegex(), limit = 2).toTypedArray()[0].toInt(),
        msg,
        data
    )
}
