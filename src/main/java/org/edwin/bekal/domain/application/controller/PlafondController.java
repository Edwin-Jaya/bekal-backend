package org.edwin.bekal.domain.application.controller;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.PlafondResponse;
import org.edwin.bekal.domain.application.service.PlafondService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plafonds")
@RequiredArgsConstructor
public class PlafondController {

    private final PlafondService plafondService;

    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<PlafondResponse> getActivePlafond(@PathVariable UUID customerId) {
        return ResponseEntity.ok(plafondService.getActivePlafond(customerId));
    }
}