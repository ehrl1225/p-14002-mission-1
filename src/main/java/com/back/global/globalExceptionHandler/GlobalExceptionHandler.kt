package com.back.global.globalExceptionHandler

import com.back.global.exception.ServiceException
import com.back.global.rsData.RsData
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.function.Function
import java.util.stream.Collectors

@RestControllerAdvice
@RequiredArgsConstructor
class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException::class)
    fun handle(ex: NoSuchElementException?): ResponseEntity<RsData<Void?>?> {
        return ResponseEntity<RsData<Void?>?>(
            RsData<Void?>(
                "404-1",
                "해당 데이터가 존재하지 않습니다."
            ),
            HttpStatus.NOT_FOUND
        )
    }


    @ExceptionHandler(ConstraintViolationException::class)
    fun handle(ex: ConstraintViolationException): ResponseEntity<RsData<Void?>?> {
        val message = ex.getConstraintViolations()
            .stream()
            .map<String?> { violation: ConstraintViolation<*>? ->
                val field: String? =
                    violation!!.getPropertyPath().toString().split("\\.".toRegex(), limit = 2).toTypedArray()[1]
                val messageTemplateBits: Array<String?> = violation.getMessageTemplate()
                    .split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val code = messageTemplateBits[messageTemplateBits.size - 2]
                val _message = violation.getMessage()
                "${field}-${code}-${_message}"
            }
            .sorted(Comparator.comparing<String?, String?>(Function { obj: String? -> obj.toString() }))
            .collect(Collectors.joining("\n"))

        return ResponseEntity<RsData<Void?>?>(
            RsData<Void?>(
                "400-1",
                message
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(ex: MethodArgumentNotValidException): ResponseEntity<RsData<Void?>?> {
        val message = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .filter { error: ObjectError? -> error is FieldError }
            .map<FieldError?> { error: ObjectError? -> error as FieldError }
            .map<String?> { error: FieldError? -> error!!.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage() }
            .sorted(Comparator.comparing<String?, String?>(Function { obj: String? -> obj.toString() }))
            .collect(Collectors.joining("\n"))

        return ResponseEntity<RsData<Void?>?>(
            RsData<Void?>(
                "400-1",
                message
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handle(ex: HttpMessageNotReadableException?): ResponseEntity<RsData<Void?>?> {
        return ResponseEntity<RsData<Void?>?>(
            RsData<Void?>(
                "400-1",
                "요청 본문이 올바르지 않습니다."
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handle(ex: MissingRequestHeaderException): ResponseEntity<RsData<Void?>?> {
        return ResponseEntity<RsData<Void?>?>(
            RsData<Void?>(
                "400-1",
                "${ex.getHeaderName()}-NotBlank-${ex.getLocalizedMessage()}"
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ServiceException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handle(ex: ServiceException): ResponseEntity<RsData<Void?>?> {
        val rsData = ex.rsData

        return ResponseEntity<RsData<Void?>?>(
            rsData,
            ResponseEntity
                .status(rsData.statusCode)
                .build<Any?>()
                .getStatusCode()
        )
    }
}