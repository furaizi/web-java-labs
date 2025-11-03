package org.example.lab1_2.web.advice

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.net.URI
import java.time.OffsetDateTime


@RestControllerAdvice
class GlobalErrorHandler {

    data class Violation(
        val field: String,
        val reason: String
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onBodyValidation(
        ex: MethodArgumentNotValidException,
        req: HttpServletRequest
    ): ProblemDetail {
        val violations = ex.bindingResult
            .fieldErrors
            .map { Violation(it.field, it.defaultMessage ?: "invalid") }

        val detail = buildString {
            append("Validation failed for '")
            append(ex.bindingResult.objectName)
            append("': ")
            append(violations.joinToString { "${it.field} - ${it.reason}" })
        }

        return problem(
            status = HttpStatus.BAD_REQUEST,
            instance = req.requestURI,
            detail = detail,
            errors = violations
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun onConstraintViolation(
        ex: ConstraintViolationException,
        req: HttpServletRequest
    ): ProblemDetail {
        val violations = ex.constraintViolations
            .map { Violation(it.propertyPath.toString(), it.message) }

        val detail = "Validation failed: " +
                violations.joinToString { "${it.field} — ${it.reason}" }

        return problem(
            status = HttpStatus.BAD_REQUEST,
            instance = req.requestURI,
            detail = detail,
            errors = violations
        )
    }

    @ExceptionHandler(BindException::class)
    fun onBindException(
        ex: BindException,
        req: HttpServletRequest
    ): ProblemDetail {
        val violations = ex.bindingResult
            .fieldErrors
            .map { Violation(it.field, it.defaultMessage ?: "invalid") }

        val detail = "Validation failed: " +
                violations.joinToString { "${it.field} — ${it.reason}" }

        return problem(
            status = HttpStatus.BAD_REQUEST,
            instance = req.requestURI,
            detail = detail,
            errors = violations
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class)
    fun onBadInput(
        ex: Exception,
        req: HttpServletRequest
    ): ProblemDetail {
        val detail = when (ex) {
            is HttpMessageNotReadableException ->
                ex.mostSpecificCause.message ?: ex.message
            is MethodArgumentTypeMismatchException ->
                "Parameter '${ex.name}' has invalid value '${ex.value}'"
            else -> ex.message
        }

        return problem(
            status = HttpStatus.BAD_REQUEST,
            instance = req.requestURI,
            detail = detail
        )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun onMissingParam(
        ex: MissingServletRequestParameterException,
        req: HttpServletRequest
    ): ProblemDetail =
        problem(
            status = HttpStatus.BAD_REQUEST,
            instance = req.requestURI,
            detail = "Required request parameter '${ex.parameterName}' is missing"
        )

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun onMethodNotAllowed(
        ex: HttpRequestMethodNotSupportedException,
        req: HttpServletRequest
    ): ProblemDetail =
        problem(
            status = HttpStatus.METHOD_NOT_ALLOWED,
            instance = req.requestURI,
            detail = ex.message
        )

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun onUnsupportedMediaType(
        ex: HttpMediaTypeNotSupportedException,
        req: HttpServletRequest
    ): ProblemDetail =
        problem(
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            instance = req.requestURI,
            detail = ex.message
        )

    @ExceptionHandler(NoSuchElementException::class)
    fun onNotFound(
        ex: NoSuchElementException,
        req: HttpServletRequest
    ): ProblemDetail =
        problem(
            status = HttpStatus.NOT_FOUND,
            instance = req.requestURI,
            detail = ex.message ?: "Resource not found"
        )

    @ExceptionHandler(Exception::class)
    fun onUnexpected(
        ex: Exception,
        req: HttpServletRequest
    ): ProblemDetail =
        problem(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            instance = req.requestURI
        )


    private fun problem(
        status: HttpStatus,
        instance: String,
        title: String = status.reasonPhrase,
        detail: String? = null,
        type: URI = URI.create("about:blank"),
        errors: List<Violation>? = null
    ): ProblemDetail {
        val effectiveDetail = detail ?: title

        return ProblemDetail
            .forStatusAndDetail(status, effectiveDetail)
            .apply {
                this.title = title
                this.type = type
                this.instance = URI.create(instance)

                setProperty("timestamp", OffsetDateTime.now())
                setProperty("error", title)
                setProperty("message", effectiveDetail)
                setProperty("path", instance)

                if (!errors.isNullOrEmpty()) {
                    setProperty("errors", errors)
                }
            }
    }


}
