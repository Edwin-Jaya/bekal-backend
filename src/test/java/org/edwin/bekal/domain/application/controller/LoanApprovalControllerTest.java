package org.edwin.bekal.domain.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.service.LoanApprovalService;
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

@ExtendWith(MockitoExtension.class)
class LoanApprovalControllerTest {

    private static final String BASE_URL    = "/api/v1/loan-approval";
    private static final String SUBMIT_URL  = BASE_URL + "/submit";
    private static final String HISTORY_URL = BASE_URL + "/history";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private LoanApprovalService loanApprovalService;
    @Mock private InternalUserRepository internalUserRepository;

    @InjectMocks
    private LoanApprovalController loanApprovalController;

    private UUID reviewerUserId;
    private String reviewerEmail;
    private UsernamePasswordAuthenticationToken principal;
    private InternalUser mockInternalUser;
    private SubmitReviewRequest submitRequest;
    private LoanApprovalResponse approvalResponse;

    @BeforeEach
    void setUp() {
        reviewerUserId = UUID.randomUUID();
        reviewerEmail  = "approver@bekal.com";

        UserDetails mockUserDetails = User.withUsername(reviewerEmail)
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_BRANCH_MANAGER"))
                .build();

        principal = new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, mockUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(principal);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(loanApprovalController)
                // Jika memiliki GlobalExceptionHandler, hilangkan komentar di bawah:
                // .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        mockInternalUser = mock(InternalUser.class);

        submitRequest = SubmitReviewRequest.builder()
                .loanApplicationId(UUID.randomUUID())
                .internalUserId(reviewerUserId)
                .result("APPROVED")
                .notes("Looks good")
                .build();

        approvalResponse = LoanApprovalResponse.builder()
                .id(UUID.randomUUID())
                .result("APPROVED")
                .notes("Looks good")
                .approvedAt(Instant.now())
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
    @DisplayName("POST /submit - returns 200 with approval response")
    void submitApproval_success() throws Exception {
        given(mockInternalUser.getId()).willReturn(reviewerUserId);
        given(internalUserRepository.findByInternalUserEmail(reviewerEmail))
                .willReturn(Optional.of(mockInternalUser));
        given(loanApprovalService.submitApproval(any(SubmitReviewRequest.class), eq(reviewerUserId)))
                .willReturn(approvalResponse);

        mockMvc.perform(post(SUBMIT_URL)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("APPROVED"))
                .andExpect(jsonPath("$.notes").value("Looks good"));

        verify(internalUserRepository).findByInternalUserEmail(reviewerEmail);
        verify(loanApprovalService).submitApproval(any(SubmitReviewRequest.class), eq(reviewerUserId));
    }

    @Test
    @DisplayName("POST /submit - returns 200 with REJECTED result")
    void submitApproval_rejected() throws Exception {
        SubmitReviewRequest rejectRequest = SubmitReviewRequest.builder()
                .loanApplicationId(UUID.randomUUID())
                .internalUserId(reviewerUserId)
                .result("REJECTED")
                .notes("Income insufficient")
                .build();

        LoanApprovalResponse rejectedResponse = LoanApprovalResponse.builder()
                .id(UUID.randomUUID())
                .result("REJECTED")
                .notes("Income insufficient")
                .approvedAt(Instant.now())
                .build();

        given(mockInternalUser.getId()).willReturn(reviewerUserId);
        given(internalUserRepository.findByInternalUserEmail(reviewerEmail))
                .willReturn(Optional.of(mockInternalUser));
        given(loanApprovalService.submitApproval(any(), eq(reviewerUserId)))
                .willReturn(rejectedResponse);

        mockMvc.perform(post(SUBMIT_URL)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("REJECTED"));
    }

    @Test
    @DisplayName("POST /submit - throws exception when user email not found")
    void submitApproval_userNotFound_throwsEntityNotFoundException() {
        given(internalUserRepository.findByInternalUserEmail(reviewerEmail))
                .willReturn(Optional.empty());

        // Menggunakan assertThatThrownBy karena tanpa GlobalExceptionHandler,
        // Spring MVC akan melempar Exception langsung keluar dari MockMvc
        assertThatThrownBy(() ->
                mockMvc.perform(post(SUBMIT_URL)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
        ).hasCauseInstanceOf(jakarta.persistence.EntityNotFoundException.class);

        verify(internalUserRepository).findByInternalUserEmail(reviewerEmail);
        verifyNoInteractions(loanApprovalService);
    }

    // ==================================================================== //
    //  GET /history                                                         //
    // ==================================================================== //

    @Test
    @DisplayName("GET /history - returns 200 with page of approval history")
    void getApprovalHistory_success() throws Exception {
        LoanHistoryResponse historyItem = new LoanHistoryResponse(
                UUID.randomUUID(), "APP-0001", "John Doe",
                BigDecimal.valueOf(5_000_000), "APPROVED",
                "APPROVED", "All good", Instant.now()
        );

        Page<LoanHistoryResponse> historyPage =
                new PageImpl<>(List.of(historyItem), PageRequest.of(0, 10), 1L);

        given(loanApprovalService.getApplicationApprovalHistory(eq(0), eq(10)))
                .willReturn(historyPage);

        mockMvc.perform(get(HISTORY_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].applicationNumber").value("APP-0001"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(loanApprovalService).getApplicationApprovalHistory(0, 10);
    }

    @Test
    @DisplayName("GET /history - uses default page=0 size=10 when params absent")
    void getApprovalHistory_defaultParams() throws Exception {
        Page<LoanHistoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanApprovalService.getApplicationApprovalHistory(eq(0), eq(10)))
                .willReturn(emptyPage);

        mockMvc.perform(get(HISTORY_URL))
                .andExpect(status().isOk());

        verify(loanApprovalService).getApplicationApprovalHistory(0, 10);
    }

    @Test
    @DisplayName("GET /history - returns empty page when no history")
    void getApprovalHistory_empty() throws Exception {
        Page<LoanHistoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanApprovalService.getApplicationApprovalHistory(anyInt(), anyInt()))
                .willReturn(emptyPage);

        mockMvc.perform(get(HISTORY_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("GET /history - respects custom page and size")
    void getApprovalHistory_customPageSize() throws Exception {
        Page<LoanHistoryResponse> page2 =
                new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 5), 0L);

        given(loanApprovalService.getApplicationApprovalHistory(eq(1), eq(5)))
                .willReturn(page2);

        mockMvc.perform(get(HISTORY_URL)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));
    }
}