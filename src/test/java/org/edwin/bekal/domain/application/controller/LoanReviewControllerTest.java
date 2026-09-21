package org.edwin.bekal.domain.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.service.LoanReviewDetailService;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test for {@link LoanReviewController}.
 *
 * Note: LoanReviewController injects LoanReviewDetailService twice
 * (loanReviewService + loanReviewDetailService) — both fields point to the
 * same interface, so a single @Mock covers both via @InjectMocks.
 */
@ExtendWith(MockitoExtension.class)
class LoanReviewControllerTest {

    private static final String BASE_URL    = "/api/v1/loan-reviews";
    private static final String SUBMIT_URL  = BASE_URL + "/submit";
    private static final String HISTORY_URL = BASE_URL + "/history";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // Single mock covers both loanReviewService and loanReviewDetailService
    // because they share the same type — @InjectMocks will inject this mock
    // into both fields.
    @Mock private LoanReviewDetailService loanReviewDetailService;
    @Mock private InternalUserRepository internalUserRepository;

    @InjectMocks
    private LoanReviewController loanReviewController;

    // ------------------------------------------------------------------ //
    //  Shared fixtures                                                     //
    // ------------------------------------------------------------------ //
    private UUID reviewerUserId;
    private String reviewerEmail;
    private UsernamePasswordAuthenticationToken principal;
    private InternalUser mockInternalUser;
    private SubmitReviewRequest submitRequest;
    private LoanReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(loanReviewController)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        reviewerUserId = UUID.randomUUID();
        reviewerEmail  = "marketing@bekal.com";

        UserDetails mockUserDetails = User.withUsername(reviewerEmail)
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_MARKETING"))
                .build();

        principal = new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, mockUserDetails.getAuthorities());

        // Set otentikasi ke SecurityContextHolder agar dibaca oleh AuthenticationPrincipalArgumentResolver
        SecurityContextHolder.getContext().setAuthentication(principal);

        mockInternalUser = mock(InternalUser.class);

        submitRequest = SubmitReviewRequest.builder()
                .loanApplicationId(UUID.randomUUID())
                .internalUserId(reviewerUserId)
                .verifiedIncome(BigDecimal.valueOf(8_000_000))
                .result("APPROVED")
                .notes("Income verified")
                .build();

        reviewResponse = LoanReviewResponse.builder()
                .id(UUID.randomUUID())
                .result("APPROVED")
                .notes("Income verified")
                .reviewedAt(Instant.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================================================================== //
    //  POST /submit                                                         //
    // ==================================================================== //

    @Test
    @DisplayName("POST /submit - returns 200 with review response")
    void submitReview_success() throws Exception {
        given(mockInternalUser.getId()).willReturn(reviewerUserId);
        given(internalUserRepository.findByInternalUserEmail(reviewerEmail))
                .willReturn(Optional.of(mockInternalUser));
        given(loanReviewDetailService.submitReview(any(SubmitReviewRequest.class), eq(reviewerUserId)))
                .willReturn(reviewResponse);

        mockMvc.perform(post(SUBMIT_URL)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("APPROVED"))
                .andExpect(jsonPath("$.notes").value("Income verified"));

        verify(internalUserRepository).findByInternalUserEmail(reviewerEmail);
        verify(loanReviewDetailService).submitReview(any(SubmitReviewRequest.class), eq(reviewerUserId));
    }

    @Test
    @DisplayName("POST /submit - returns 200 with REJECTED result")
    void submitReview_rejected() throws Exception {
        SubmitReviewRequest rejectRequest = SubmitReviewRequest.builder()
                .loanApplicationId(UUID.randomUUID())
                .internalUserId(reviewerUserId)
                .result("REJECTED")
                .notes("Docs incomplete")
                .build();

        LoanReviewResponse rejectedResponse = LoanReviewResponse.builder()
                .id(UUID.randomUUID())
                .result("REJECTED")
                .notes("Docs incomplete")
                .reviewedAt(Instant.now())
                .build();

        given(mockInternalUser.getId()).willReturn(reviewerUserId);
        given(internalUserRepository.findByInternalUserEmail(reviewerEmail))
                .willReturn(Optional.of(mockInternalUser));
        given(loanReviewDetailService.submitReview(any(), eq(reviewerUserId)))
                .willReturn(rejectedResponse);

        mockMvc.perform(post(SUBMIT_URL)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("REJECTED"));
    }

    @Test
    @DisplayName("POST /submit - throws EntityNotFoundException when user not found")
    void submitReview_userNotFound_throwsException() {
        given(internalUserRepository.findByInternalUserEmail(reviewerEmail))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                mockMvc.perform(post(SUBMIT_URL)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
        ).hasCauseInstanceOf(jakarta.persistence.EntityNotFoundException.class);

        verify(internalUserRepository).findByInternalUserEmail(reviewerEmail);
        verifyNoInteractions(loanReviewDetailService);
    }

    // ==================================================================== //
    //  GET /history                                                         //
    // ==================================================================== //

    @Test
    @DisplayName("GET /history - returns 200 with page of review history")
    void getReviewHistory_success() throws Exception {
        LoanHistoryResponse historyItem = new LoanHistoryResponse(
                UUID.randomUUID(), "APP-0005", "Budi Santoso",
                BigDecimal.valueOf(3_000_000), "REVIEW_APPROVED",
                "APPROVED", "Income verified", Instant.now()
        );

        Page<LoanHistoryResponse> historyPage =
                new PageImpl<>(List.of(historyItem), PageRequest.of(0, 10), 1L);

        given(loanReviewDetailService.getApplicationReviewHistory(eq(0), eq(10)))
                .willReturn(historyPage);

        mockMvc.perform(get(HISTORY_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].applicationNumber").value("APP-0005"))
                .andExpect(jsonPath("$.content[0].reviewResult").value("APPROVED"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(loanReviewDetailService).getApplicationReviewHistory(0, 10);
    }

    @Test
    @DisplayName("GET /history - uses default page=0 size=10 when params absent")
    void getReviewHistory_defaultParams() throws Exception {
        Page<LoanHistoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanReviewDetailService.getApplicationReviewHistory(eq(0), eq(10)))
                .willReturn(emptyPage);

        mockMvc.perform(get(HISTORY_URL))
                .andExpect(status().isOk());

        verify(loanReviewDetailService).getApplicationReviewHistory(0, 10);
    }

    @Test
    @DisplayName("GET /history - returns empty page when no history")
    void getReviewHistory_empty() throws Exception {
        Page<LoanHistoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanReviewDetailService.getApplicationReviewHistory(anyInt(), anyInt()))
                .willReturn(emptyPage);

        mockMvc.perform(get(HISTORY_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("GET /history - respects custom page and size")
    void getReviewHistory_customPageSize() throws Exception {
        Page<LoanHistoryResponse> page2 =
                new PageImpl<>(Collections.emptyList(), PageRequest.of(2, 5), 0L);

        given(loanReviewDetailService.getApplicationReviewHistory(eq(2), eq(5)))
                .willReturn(page2);

        mockMvc.perform(get(HISTORY_URL)
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(5));
    }
}