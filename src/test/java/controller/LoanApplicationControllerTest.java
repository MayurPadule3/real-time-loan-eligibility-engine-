
package com.loanengine.application.controller;

import com.loanengine.application.dto.LoanApplicationResponse;
import com.loanengine.application.entity.LoanStatus;
import com.loanengine.application.service.LoanApplicationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationControllerTest {

    @Mock
    private LoanApplicationService loanApplicationService;

    @InjectMocks
    private LoanApplicationController loanApplicationController;

    private MockMvc mockMvc;

    private LoanApplicationResponse response;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(loanApplicationController)
                .build();

        response = new LoanApplicationResponse(
                1L,
                1L,
                new BigDecimal("500000.00"),
                new BigDecimal("10.50"),
                60,
                "Home Renovation",
                LoanStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void createLoanApplication_shouldReturn201() throws Exception {

        when(loanApplicationService.createLoanApplication(any()))
                .thenReturn(response);

        String requestJson = """
                {
                    "customerId": 1,
                    "loanAmount": 500000.00,
                    "interestRate": 10.50,
                    "tenureMonths": 60,
                    "loanPurpose": "Home Renovation"
                }
                """;

        mockMvc.perform(post("/api/v1/loan-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(loanApplicationService).createLoanApplication(any());
    }

    @Test
    void getAllLoanApplications_shouldReturn200() throws Exception {

        when(loanApplicationService.getAllLoanApplications())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/loan-applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(loanApplicationService).getAllLoanApplications();
    }

    @Test
    void getLoanApplicationById_shouldReturn200() throws Exception {

        when(loanApplicationService.getLoanApplicationById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/loan-applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.loanPurpose")
                        .value("Home Renovation"));

        verify(loanApplicationService).getLoanApplicationById(1L);
    }

    @Test
    void getLoanApplicationsByCustomerId_shouldReturn200()
            throws Exception {

        when(loanApplicationService.getLoanApplicationsByCustomerId(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/loan-applications/customer/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1));

        verify(loanApplicationService)
                .getLoanApplicationsByCustomerId(1L);
    }

    @Test
    void getLoanApplicationsByStatus_shouldReturn200()
            throws Exception {

        when(loanApplicationService.getLoanApplicationsByStatus(
                LoanStatus.PENDING
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/loan-applications/status/PENDING")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(loanApplicationService)
                .getLoanApplicationsByStatus(LoanStatus.PENDING);
    }
}
