package com.bobbyprabowo.plugins

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*

fun Application.configureHttpClient() : HttpClient {
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    install(Sessions) {
        cookie<UserSession>("user_session")
    }
    val redirects = mutableMapOf<String, String>()
    install(Authentication) {
        oauth("auth-oauth-google") {
            // Configure oauth authentication
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GLINKING_WEB_GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("GLINKING_WEB_GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf(
                        "https://www.googleapis.com/auth/userinfo.profile",
                    ),
                    extraAuthParameters = listOf("access_type" to "offline"),
                    onStateCreated = { call, state ->
                        //saves new state with redirect url value
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    }
                )
            }
            client = httpClient
        }
    }

    return httpClient
}