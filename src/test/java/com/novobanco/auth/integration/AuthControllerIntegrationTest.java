package com.novobanco.auth.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("novobank_auth")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "dGVzdF9zZWNyZXRfbWluaW11bV8yNTZfYml0c19mb3JfdGVzdHNfb25seQ==");
        registry.add("jwt.expiration-ms", () -> "900000");
        registry.add("jwt.refresh-expiration-ms", () -> "604800000");
        registry.add("cors.allowed-origins", () -> "http://localhost:5173");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String REFRESH_URL = "/api/v1/auth/refresh";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";
    private static final String ME_URL = "/api/v1/auth/me";

    @Test
    void should_register_user_and_return_201() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("it201@novobanco.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.email").value("it201@novobanco.com"));
    }

    @Test
    void should_return_409_when_email_already_exists() throws Exception {
        String email = "dup409@novobanco.com";
        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email))).andExpect(status().isCreated());

        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void should_login_and_return_access_and_refresh_tokens() throws Exception {
        String email = "login200@novobanco.com";
        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email))).andExpect(status().isCreated());

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(email));
    }

    @Test
    void should_return_401_when_credentials_are_invalid() throws Exception {
        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@novobanco.com\",\"password\":\"Wrong123!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email o contraseña incorrectos"));
    }

    @Test
    void should_refresh_token_successfully() throws Exception {
        String email = "refresh200@novobanco.com";
        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email))).andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email)))
                .andExpect(status().isOk()).andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("refreshToken").asText();

        mockMvc.perform(post(REFRESH_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void should_return_401_when_refresh_token_is_revoked() throws Exception {
        String email = "revoked401@novobanco.com";
        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email))).andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email)))
                .andExpect(status().isOk()).andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.get("accessToken").asText();
        String refreshToken = loginJson.get("refreshToken").asText();

        // logout revokes tokens
        mockMvc.perform(post(LOGOUT_URL).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(REFRESH_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_logout_and_revoke_refresh_token() throws Exception {
        String email = "logout204@novobanco.com";
        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email))).andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email)))
                .andExpect(status().isOk()).andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(post(LOGOUT_URL).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_return_user_data_on_get_me_with_valid_token() throws Exception {
        String email = "me200@novobanco.com";
        mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email))).andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email)))
                .andExpect(status().isOk()).andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(get(ME_URL).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void should_return_401_on_get_me_without_token() throws Exception {
        mockMvc.perform(get(ME_URL))
                .andExpect(status().isUnauthorized());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String registerBody(String email) {
        return String.format("""
                {
                  "email": "%s",
                  "password": "Password123!",
                  "fullName": "Integration User",
                  "customerId": "%s"
                }""", email, UUID.randomUUID());
    }

    private String loginBody(String email) {
        return String.format("""
                {
                  "email": "%s",
                  "password": "Password123!"
                }""", email);
    }
}
