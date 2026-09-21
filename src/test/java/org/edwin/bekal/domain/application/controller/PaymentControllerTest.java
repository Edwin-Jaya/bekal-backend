package org.edwin.bekal.domain.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.edwin.bekal.domain.application.dto.LoanBalanceResponse;
import org.edwin.bekal.domain.application.dto.PaymentHistoryResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.entity.PaymentTransaction;
import org.edwin.bekal.domain.application.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PaymentServiceImpl paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(paymentController)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/v1/payments/repay - Success")
    void repayLoan_Success() throws Exception {
        UUID loanId = UUID.randomUUID();

        LoanApplication mockLoan = Mockito.mock(LoanApplication.class);
        given(mockLoan.getId()).willReturn(loanId);

        PaymentTransaction mockTransaction = Mockito.mock(PaymentTransaction.class);
        given(mockTransaction.getLoan()).willReturn(mockLoan);

        LoanBalanceResponse mockBalance = Mockito.mock(LoanBalanceResponse.class);

        given(paymentService.processRepayment(any())).willReturn(mockTransaction);
        given(paymentService.getLoanBalance(loanId)).willReturn(mockBalance);

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("loanId", loanId);
        requestPayload.put("amountPaid", BigDecimal.valueOf(500_000));
        requestPayload.put("paymentMethod", "BANK_TRANSFER");
        requestPayload.put("transactionReference", "TXN-REF-12345");

        mockMvc.perform(post("/api/v1/payments/repay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk());

        verify(paymentService).processRepayment(any());
        verify(paymentService).getLoanBalance(loanId);
    }

    @Test
    @DisplayName("GET /api/v1/payments/balance/{loanId} - Success")
    void getLoanBalance_Success() throws Exception {
        UUID loanId = UUID.randomUUID();
        LoanBalanceResponse mockBalance = Mockito.mock(LoanBalanceResponse.class);
        given(paymentService.getLoanBalance(loanId)).willReturn(mockBalance);

        mockMvc.perform(get("/api/v1/payments/balance/{loanId}", loanId))
                .andExpect(status().isOk());

        verify(paymentService).getLoanBalance(loanId);
    }

    @Test
    @DisplayName("GET /api/v1/payments/history/customer/{customerId} - Success")
    void getCustomerPaymentHistory_Success() throws Exception {
        UUID customerId = UUID.randomUUID();
        List<PaymentHistoryResponse> mockHistory = Collections.emptyList();
        given(paymentService.getPaymentHistoryByCustomer(customerId)).willReturn(mockHistory);

        mockMvc.perform(get("/api/v1/payments/history/customer/{customerId}", customerId))
                .andExpect(status().isOk());

        verify(paymentService).getPaymentHistoryByCustomer(customerId);
    }
}