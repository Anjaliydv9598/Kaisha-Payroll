package com.kaisha.payroll.payroll.entity;

import com.kaisha.payroll.payroll.request.PayrollRequestType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_download_requests")
public class PayrollDownloadRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    /*
     * Username/email of the STAFF/ADMIN who made request.
     */
    @Column(name = "requested_by", nullable = false, length = 255)
    private String requestedBy;

    @Column(name = "pay_period", nullable = false, length = 7)
    private String payPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 30)
    private PayrollRequestType scope;

    /*
     * For selected employees:
     *
     * E001,E002,E005
     */
    @Column(name = "employee_ids", columnDefinition = "TEXT")
    private String employeeIds;

    @Column(name = "from_record")
    private Integer fromRecord;

    @Column(name = "to_record")
    private Integer toRecord;

    /*
     * PDF / EXCEL / CSV
     */
    @Column(name = "format", nullable = false, length = 10)
    private String format;

    /*
     * PENDING / APPROVED / REJECTED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (status == null) {
            status = "PENDING";
        }
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(String payPeriod) {
        this.payPeriod = payPeriod;
    }

    public PayrollRequestType getScope() {
        return scope;
    }

    public void setScope(PayrollRequestType scope) {
        this.scope = scope;
    }

    public String getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(String employeeIds) {
        this.employeeIds = employeeIds;
    }

    public Integer getFromRecord() {
        return fromRecord;
    }

    public void setFromRecord(Integer fromRecord) {
        this.fromRecord = fromRecord;
    }

    public Integer getToRecord() {
        return toRecord;
    }

    public void setToRecord(Integer toRecord) {
        this.toRecord = toRecord;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
}