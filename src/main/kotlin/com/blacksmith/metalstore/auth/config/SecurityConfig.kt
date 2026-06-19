package com.blacksmith.metalstore.auth.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSKeySelector
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import java.net.URL

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    @Profile("dev", "test")
    fun devSecurityFilterChain(
        http: HttpSecurity,
        jwtDecoder: JwtDecoder,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { it.decoder(jwtDecoder) }
            }
        return http.build()
    }

    @Bean
    @Profile("prod")
    fun prodSecurityFilterChain(
        http: HttpSecurity,
        jwtDecoder: JwtDecoder,
        cookieResolver: CookieBearerTokenResolver,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth                    .requestMatchers("/api/health", "/api/auth/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.bearerTokenResolver(cookieResolver)
                oauth2.jwt { it.decoder(jwtDecoder) }
            }
        return http.build()
    }

    @Bean
    fun jwtDecoder(props: SupabaseProperties): JwtDecoder {
        val jwkSetUrl = URL("${props.url}/auth/v1/.well-known/jwks.json")
        val jwkSet = RemoteJWKSet<SecurityContext>(jwkSetUrl)
        val keySelector: JWSKeySelector<SecurityContext> =
            JWSVerificationKeySelector(JWSAlgorithm.Family.EC, jwkSet)
        val processor = DefaultJWTProcessor<SecurityContext>()
        processor.jwsKeySelector = keySelector
        return NimbusJwtDecoder(processor)
    }
}
