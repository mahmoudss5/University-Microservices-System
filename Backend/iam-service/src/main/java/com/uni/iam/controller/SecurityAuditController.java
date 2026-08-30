package com.uni.iam.controller;

import com.uni.iam.entity.SecurityAuditLog;
import com.uni.iam.service.interfaces.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security-audit-logs")
@RequiredArgsConstructor
public class SecurityAuditController {
    private final SecurityAuditService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<SecurityAuditLog> findAll(@RequestParam(required = false) String eventType, Pageable pageable) {
        return service.findAll(eventType, pageable);
    }
}
