package org.edwin.bekal.domain.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.service.LoanApplicationService;
import org.edwin.bekal.domain.application.service.LoanDisbursementService;
import org.edwin.bekal.domain.application.service.impl.LoanReviewDetailServiceImpl;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationControllerTest {

    // ------------------------------------------------------------------ //
    //  URL constants — single source of truth                             //
    // ------------------------------------------------------------------ //
    private static final String BASE_URL                 = "/api/v1/loan-applications";
    private static final String BY_CUSTOMER_URL          = BASE_URL + "/customer/{customerId}";
    private static final String PENDING_REVIEWS_URL      = BASE_URL + "/pending-reviews";
    private static final String PENDING_APPROVALS_URL    = BASE_URL + "/pending-approvals";
    private static final String PENDING_DISBURSEMENT_URL = BASE_URL + "/pending-disbursement";
    private static final String DETAIL_URL               = BASE_URL + "/{id}/detail";
    private static final String DETAIL_APPROVAL_URL      = BASE_URL + "/{id}/detail-approval";
    private static final String DETAIL_DISBURSEMENT_URL  = BASE_URL + "/{id}/detail-disbursement";

    // ------------------------------------------------------------------ //
    //  MockMvc + ObjectMapper                                             //
    // ------------------------------------------------------------------ //
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // ------------------------------------------------------------------ //
    //  Mocked services                                                    //
    // ------------------------------------------------------------------ //
    @Mock
    private LoanApplicationService loanApplicationService;

    @Mock
    private LoanReviewDetailServiceImpl loanReviewDetailService;

    @Mock
    private LoanDisbursementService loanDisbursementService;

    // ------------------------------------------------------------------ //
    //  Controller under test                                              //
    // ------------------------------------------------------------------ //
    @InjectMocks
    private LoanApplicationController loanApplicationController;

    // ------------------------------------------------------------------ //
    //  Shared fixtures                                                    //
    // ------------------------------------------------------------------ //
    private UUID testId;
    private UUID testCustomerId;
    private LoanApplicationResponse sampleResponse;
    private Page<LoanApplicationResponse> samplePage;
    private LoanReviewDetail sampleReviewDetail;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Build MockMvc manually — include MessageConverter with JavaTimeModule support
        mockMvc = MockMvcBuilders
                .standaloneSetup(loanApplicationController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testId         = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();

        sampleResponse = LoanApplicationResponse.builder()
                .id(testId)
                .applicationNumber("APP-0001")
                .amountRequested(BigDecimal.valueOf(5_000_000))
                .tenorMonths(12)
                .purpose("Modal Usaha")
                .interestRate(BigDecimal.valueOf(1.5))
                .monthlyInstallment(BigDecimal.valueOf(450_000))
                .totalRepayment(BigDecimal.valueOf(5_400_000))
                .status("PENDING_REVIEW")
                .submittedAt(Instant.now())
                .build();

        samplePage = new PageImpl<>(
                List.of(sampleResponse),
                PageRequest.of(0, 10),
                1L
        );

        sampleReviewDetail = LoanReviewDetail.builder()
                .loanApplicationResponse(sampleResponse)
                .build();
    }

    // ==================================================================== //
    //  GET /customer/{customerId}                                          //
    // ==================================================================== //

    @Test
    @DisplayName("GET /customer/{customerId} - returns 200 with page")
    void getLoanApplicationByCustomer_success() throws Exception {
        given(loanApplicationService.getLoanApplicationByCustomer(eq(testCustomerId), anyInt(), anyInt()))
                .willReturn(samplePage);

        mockMvc.perform(get(BY_CUSTOMER_URL, testCustomerId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].applicationNumber").value("APP-0001"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(loanApplicationService).getLoanApplicationByCustomer(testCustomerId, 0, 10);
    }

    @Test
    @DisplayName("GET /customer/{customerId} - uses default page=0 size=10 when params absent")
    void getLoanApplicationByCustomer_defaultParams() throws Exception {
        given(loanApplicationService.getLoanApplicationByCustomer(eq(testCustomerId), eq(0), eq(10)))
                .willReturn(samplePage);

        mockMvc.perform(get(BY_CUSTOMER_URL, testCustomerId))
                .andExpect(status().isOk());

        verify(loanApplicationService).getLoanApplicationByCustomer(testCustomerId, 0, 10);
    }

    @Test
    @DisplayName("GET /customer/{customerId} - returns empty page when no results")
    void getLoanApplicationByCustomer_emptyPage() throws Exception {
        // PERBAIKAN: Gunakan PageRequest.of(0, 10) sebagai pengganti unpaged()
        Page<LoanApplicationResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanApplicationService.getLoanApplicationByCustomer(eq(testCustomerId), anyInt(), anyInt()))
                .willReturn(emptyPage);

        mockMvc.perform(get(BY_CUSTOMER_URL, testCustomerId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ==================================================================== //
    //  POST /                                                              //
    // ==================================================================== //

    @Test
    @DisplayName("POST / - creates loan application and returns 200")
    void createLoanApplication_success() throws Exception {
        CreateLoanApplicationRequest request = new CreateLoanApplicationRequest();
        request.setAmountRequested(BigDecimal.valueOf(5_000_000));
        request.setTenorMonths(12);
        request.setPurpose("Modal Usaha");

        given(loanApplicationService.createLoanApplication(any(CreateLoanApplicationRequest.class)))
                .willReturn(sampleResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationNumber").value("APP-0001"))
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"));

        verify(loanApplicationService).createLoanApplication(any(CreateLoanApplicationRequest.class));
    }

    @Test
    @DisplayName("POST / - minimal body still returns 200")
    void createLoanApplication_minimalBody() throws Exception {
        CreateLoanApplicationRequest request = new CreateLoanApplicationRequest();
        request.setAmountRequested(BigDecimal.ONE);
        request.setTenorMonths(1);

        given(loanApplicationService.createLoanApplication(any()))
                .willReturn(sampleResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ==================================================================== //
    //  GET /                                                               //
    // ==================================================================== //

    @Test
    @DisplayName("GET / - returns 200 with paginated results")
    void getLoanApplication_success() throws Exception {
        given(loanApplicationService.getLoanApplication(anyInt(), anyInt()))
                .willReturn(samplePage);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testId.toString()));

        verify(loanApplicationService).getLoanApplication(0, 10);
    }

    @Test
    @DisplayName("GET / - uses default page=0 size=10 when params absent")
    void getLoanApplication_defaultParams() throws Exception {
        given(loanApplicationService.getLoanApplication(eq(0), eq(10)))
                .willReturn(samplePage);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());

        verify(loanApplicationService).getLoanApplication(0, 10);
    }

    @Test
    @DisplayName("GET / - respects custom page and size")
    void getLoanApplication_customPageSize() throws Exception {
        Page<LoanApplicationResponse> page2 =
                new PageImpl<>(List.of(sampleResponse), PageRequest.of(2, 5), 11L);

        given(loanApplicationService.getLoanApplication(eq(2), eq(5)))
                .willReturn(page2);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(5));
    }

    // ==================================================================== //
    //  GET /pending-reviews                                                 //
    // ==================================================================== //

    @Test
    @DisplayName("GET /pending-reviews - returns 200")
    void getPendingLoanApplication_success() throws Exception {
        given(loanApplicationService.getPendingLoanApplication(anyInt(), anyInt()))
                .willReturn(samplePage);

        mockMvc.perform(get(PENDING_REVIEWS_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING_REVIEW"));

        verify(loanApplicationService).getPendingLoanApplication(0, 10);
    }

    @Test
    @DisplayName("GET /pending-reviews - returns empty page when none pending")
    void getPendingLoanApplication_empty() throws Exception {
        // PERBAIKAN: Gunakan PageRequest.of(0, 10) sebagai pengganti unpaged()
        Page<LoanApplicationResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(loanApplicationService.getPendingLoanApplication(anyInt(), anyInt()))
                .willReturn(emptyPage);

        mockMvc.perform(get(PENDING_REVIEWS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // ==================================================================== //
    //  GET /pending-approvals                                              //
    // ==================================================================== //

    @Test
    @DisplayName("GET /pending-approvals - returns 200")
    void getPendingLoanApplicationApproval_success() throws Exception {
        given(loanApplicationService.getPendingLoanApplicationApproval(anyInt(), anyInt()))
                .willReturn(samplePage);

        mockMvc.perform(get(PENDING_APPROVALS_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(loanApplicationService).getPendingLoanApplicationApproval(0, 10);
    }

    @Test
    @DisplayName("GET /pending-approvals - uses default params")
    void getPendingLoanApplicationApproval_defaultParams() throws Exception {
        given(loanApplicationService.getPendingLoanApplicationApproval(eq(0), eq(10)))
                .willReturn(samplePage);

        mockMvc.perform(get(PENDING_APPROVALS_URL))
                .andExpect(status().isOk());
    }

    // ==================================================================== //
    //  GET /pending-disbursement                                           //
    // ==================================================================== //

    @Test
    @DisplayName("GET /pending-disbursement - returns 200")
    void getPendingLoanApplicationDisbursement_success() throws Exception {
        given(loanApplicationService.getPendingLoanApplicationDisbursement(anyInt(), anyInt()))
                .willReturn(samplePage);

        mockMvc.perform(get(PENDING_DISBURSEMENT_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(loanApplicationService).getPendingLoanApplicationDisbursement(0, 10);
    }

    @Test
    @DisplayName("GET /pending-disbursement - uses default params")
    void getPendingLoanApplicationDisbursement_defaultParams() throws Exception {
        given(loanApplicationService.getPendingLoanApplicationDisbursement(eq(0), eq(10)))
                .willReturn(samplePage);

        mockMvc.perform(get(PENDING_DISBURSEMENT_URL))
                .andExpect(status().isOk());
    }

    // ==================================================================== //
    //  GET /{id}/detail                                                    //
    // ==================================================================== //

    @Test
    @DisplayName("GET /{id}/detail - returns 200 with LoanReviewDetail")
    void getDetail_success() throws Exception {
        given(loanReviewDetailService.getDetail(eq(testId)))
                .willReturn(sampleReviewDetail);

        mockMvc.perform(get(DETAIL_URL, testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanApplicationResponse.applicationNumber")
                        .value("APP-0001"));

        verify(loanReviewDetailService).getDetail(testId);
        verifyNoInteractions(loanDisbursementService);
    }

    // ==================================================================== //
    //  GET /{id}/detail-approval                                           //
    // ==================================================================== //

    @Test
    @DisplayName("GET /{id}/detail-approval - returns 200, delegates to loanReviewDetailService")
    void getDetailApproval_success() throws Exception {
        given(loanReviewDetailService.getDetail(eq(testId)))
                .willReturn(sampleReviewDetail);

        mockMvc.perform(get(DETAIL_APPROVAL_URL, testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanApplicationResponse.applicationNumber")
                        .value("APP-0001"));

        verify(loanReviewDetailService).getDetail(testId);
        verifyNoInteractions(loanDisbursementService);
    }

    // ==================================================================== //
    //  GET /{id}/detail-disbursement                                       //
    // ==================================================================== //

    @Test
    @DisplayName("GET /{id}/detail-disbursement - returns 200, delegates to loanDisbursementService")
    void getDetailDisbursement_success() throws Exception {
        given(loanDisbursementService.getDetailDisbursement(eq(testId)))
                .willReturn(sampleReviewDetail);

        mockMvc.perform(get(DETAIL_DISBURSEMENT_URL, testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanApplicationResponse.applicationNumber")
                        .value("APP-0001"));

        verify(loanDisbursementService).getDetailDisbursement(testId);
        verifyNoInteractions(loanReviewDetailService);
    }
}