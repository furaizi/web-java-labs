package org.example.lab1_1.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

class ApiKeyFilter(
    private val validApiKey: String,
    private val headerName: String = "X-API-KEY",
    private val protectedPattern: String = "/api/**"
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        // Only enforce API key on protected API routes
        return !pathMatcher.match(protectedPattern, request.servletPath)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader(headerName)?.takeIf { it.isNotBlank() }
        if (apiKey == null || apiKey != validApiKey) {
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.contentType = "application/json"
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, """ApiKey realm="cosmo-cats"""")
            response.writer.write("""{"error":"invalid_api_key","message":"Missing or invalid API Key"}""")
            return
        }

        filterChain.doFilter(request, response)
    }
}
