package org.edwin.bekal.domain.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.service.LoanApprovalService;
import org.edwin.bekal.domain.application.service.LoanDisbursementService;
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
class LoanDisbursementControllerTest {

    private static final String BASE_URL    = "/api/v1/loan-disbursement";
    private static final String SUBMIT_URL  = BASE_URL + "/submit";
    private static final String HISTORY_URL = BASE_URL + "/history";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private LoanApprovalService loanApprovalService;
    @Mock private LoanDisbursementService loanDisbursementService;
    @Mock private InternalUserRepository internalUserRepository;

    @InjectMocks
    private LoanDisbursementController loanDisbursementController;

    private UUID disburserId;
    private String disburserEmail;
    private UsernamePasswordAuthenticationToken principal;
    private InternalUser mockInternalUser;
    private SubmitDisbursementRequest submitRequest;
    private LoanDisbursementResponse disbursementResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(loanDisbursementController)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        disburserId    = UUID.randomUUID();
        disburserEmail = "disburser@bekal.com";

        UserDetails mockUserDetails = User.withUsername(disburserEmail)
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_BACK_OFFICE"))
                .build();

        principal = new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, mockUserDetails.getAuthorities());

        // Set otentikasi ke SecurityContextHolder agar dibaca oleh AuthenticationPrincipalArgumentResolver
        SecurityContextHolder.getContext().setAuthentication(principal);

        mockInternalUser = mock(InternalUser.class);

        submitRequest = SubmitDisbursementRequest.builder()
                .loanApplicationId(UUID.randomUUID())
                .customerBankId(UUID.randomUUID())
                .disbursedBy(disburserId)
                .disbursementAmount(BigDecimal.valueOf(5_000_000))
                .status("DISBURSED")
                .referenceNumber("REF-001")
                .build();

        disbursementResponse = LoanDisbursementResponse.builder()
                .id(UUID.randomUUID())
                .disbursementAmount(BigDecimal.valueOf(5_000_000))
                .referenceNumber("REF-001")
                .status("DISBURSED")
                .disbursedAt(Instant.now())
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
    @DisplayName("POST /submit - returns 200 with disbursement response")
    void submitDisbursement_success() throws Exception {
        given(mockInternalUser.getId()).willReturn(disburserId);
        given(internalUserRepository.findByInternalUserEmail(disburserEmail))
                .willReturn(Optional.of(mockInternalUser));
        given(loanDisbursementService.submitDisbursement(any(SubmitDisbursementRequest.class), eq(disburserId)))
                .willReturn(disbursementResponse);

        mockMvc.perform(post(SUBMIT_URL)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceNumber").value("REF-001"))
                .andExpect(jsonPath("$.status").value("DISBURSED"));

        verify(internalUserRepository).findByInternalUserEmail(disburserEmail);
        verify(loanDisbursementService).submitDisbursement(any(), eq(disburserId));
    }

    @Test
    @DisplayName("POST /submit - throws EntityNotFoundException when user not found")
    void submitDisbursement_userNotFound_throwsException() {
        given(internalUserRepository.findByInternalUserEmail(disburserEmail))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                mockMvc.perform(post(SUBMIT_URL)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
        ).hasCauseInstanceOf(jakarta.persistence.EntityNotFoundException.class);

        verify(internalUserRepository).findByInternalUserEmail(disburserEmail);
        verifyNoInteractions(loanDisbursementService);
    }

    // ==================================================================== //
    //  GET /history — branch coverage for page/size guard clauses          //
    // ==================================================================== //

    @Test
    @DisplayName("GET /history - valid page and size returns 200")
    void getHistory_validPageSize() throws Exception {
        Page<LoanDisbursementHistoryResponse> page = new PageImpl<>(
                List.of(new LoanDisbursementHistoryResponse(
                        UUID.randomUUID(), "APP-0010",
                        BigDecimal.valueOf(5_000_000), "Jane Doe",
                        "DISBURSED", Instant.now()
                )), PageRequest.of(0, 10), 1L);

        given(loanDisbursementService.getApplicationDisbursementHistory(eq(0), eq(10)))
                .willReturn(page);

        mockMvc.perform(get(HISTORY_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].applicationNumber").value("APP-0010"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(loanDisbursementService).getApplicationDisbursementHistory(0, 10);
    }

    @Test
    @DisplayName("GET /history - page < 0 is clamped to 0")
    void getHistory_negativePage_clampedToZero() throws Exception {
        Page<LoanDisbursementHistoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanDisbursementService.getApplicationDisbursementHistory(eq(0), eq(10)))
                .willReturn(emptyPage);

        mockMvc.perform(get(HISTORY_URL)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(loanDisbursementService).getApplicationDisbursementHistory(0, 10);
    }

    @Test
    @DisplayName("GET /history - size < 1 is clamped to 10")
    void getHistory_zeroSize_clampedToTen() throws Exception {
        Page<LoanDisbursementHistoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanDisbursementService.getApplicationDisbursementHistory(eq(0), eq(10)))
                .willReturn(emptyPage);

        mockMvc.perform(get(HISTORY_URL)
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isOk());

        verify(loanDisbursementService).getApplicationDisbursementHistory(0, 10);
    }

    @Test
    @DisplayName("GET /history - size > 100 is clamped to 10")
    void getHistory_oversizedPage_clampedToTen() throws Exception {
        Page<LoanDisbursementHistoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanDisbursementService.getApplicationDisbursementHistory(eq(0), eq(10)))
                .willReturn(emptyPage);

        mockMvc.perform(get(HISTORY_URL)
                        .param("page", "0")
                        .param("size", "200"))
                .andExpect(status().isOk());

        verify(loanDisbursementService).getApplicationDisbursementHistory(0, 10);
    }

    @Test
    @DisplayName("GET /history - uses default page=0 size=10 when params absent")
    void getHistory_defaultParams() throws Exception {
        Page<LoanDisbursementHistoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanDisbursementService.getApplicationDisbursementHistory(eq(0), eq(10)))
                .willReturn(emptyPage);

        mockMvc.perform(get(HISTORY_URL))
                .andExpect(status().isOk());

        verify(loanDisbursementService).getApplicationDisbursementHistory(0, 10);
    }

    @Test
    @DisplayName("GET /history - returns empty page when no history")
    void getHistory_empty() throws Exception {
        Page<LoanDisbursementHistoryResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanDisbursementService.getApplicationDisbursementHistory(anyInt(), anyInt()))
                .willReturn(emptyPage);

        mockMvc.perform(get(HISTORY_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}