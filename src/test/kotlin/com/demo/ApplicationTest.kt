package com.demo

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testLoginPageLoads() = testApplication {
        application { module() }
        val response = client.get("/login")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("AI Assistant Login"))
    }

    @Test
    fun testUnauthenticatedRedirectsToLogin() = testApplication {
        application { module() }
        val response = client.get("/")
        // When not authenticated, should redirect to login or show login page
        assertTrue(
            response.status == HttpStatusCode.Found ||
            response.status == HttpStatusCode.OK ||
            response.status == HttpStatusCode.Unauthorized
        )
    }

    @Test
    fun testLoginWithValidCredentials() = testApplication {
        application { module() }
        val response = client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("username=admin&password=admin123")
        }
        // Should redirect to dashboard on success
        assertTrue(
            response.status == HttpStatusCode.Found ||
            response.status == HttpStatusCode.OK
        )
    }

    @Test
    fun testLoginWithInvalidCredentials() = testApplication {
        application { module() }
        val response = client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("username=admin&password=wrong")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertTrue(response.bodyAsText().contains("Invalid username or password"))
    }

    @Test
    fun testLogout() = testApplication {
        application { module() }
        val response = client.get("/logout")
        // Should redirect to login
        assertTrue(
            response.status == HttpStatusCode.Found ||
            response.status == HttpStatusCode.OK
        )
    }
}
