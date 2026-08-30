package com.uni.iam.service.interfaces;

import com.uni.iam.dto.request.SecurityAuditEventDto;
import com.uni.iam.entity.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SecurityAuditService {
    void record(SecurityAuditEventDto event);
    Page<SecurityAuditLog> findAll(String eventType, Pageable pageable);
}
