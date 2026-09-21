package org.edwin.bekal.domain.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.edwin.bekal.domain.application.dto.RoleMenuAccessResponse;
import org.edwin.bekal.domain.application.service.RoleMenuAccessService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoleMenuAccessControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RoleMenuAccessService roleMenuAccessService;

    @InjectMocks
    private RoleMenuAccessController roleMenuAccessController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(roleMenuAccessController)
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
    @DisplayName("GET /api/v1/role-menu-access - Success")
    void getAccess_Success() throws Exception {
        Page<RoleMenuAccessResponse> mockPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);
        given(roleMenuAccessService.getAccess(anyInt(), anyInt())).willReturn(mockPage);

        mockMvc.perform(get("/api/v1/role-menu-access")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(roleMenuAccessService).getAccess(0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/role-menu-access/role/{roleId} - Success")
    void getMatrixByRole_Success() throws Exception {
        UUID roleId = UUID.randomUUID();
        Page<RoleMenuAccessResponse> mockPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0L);

        given(roleMenuAccessService.getMatrixByRoleId(eq(roleId), anyInt(), anyInt())).willReturn(mockPage);

        mockMvc.perform(get("/api/v1/role-menu-access/role/{roleId}", roleId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(roleMenuAccessService).getMatrixByRoleId(roleId, 0, 10);
    }
}