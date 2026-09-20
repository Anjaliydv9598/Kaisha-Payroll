package com.kaisha.payroll.salary.dto;

import com.kaisha.payroll.salary.entity.SalaryComponent.ComponentType;

import java.math.BigDecimal;

public class SalaryComponentDto {

    private Long id;

    private String componentName;

    private ComponentType componentType;

    private BigDecimal amount = BigDecimal.ZERO;

    private boolean systemDefined = false;

    // =====================================================
    // CONSTRUCTORS
    // =====================================================

    public SalaryComponentDto() {
    }

    public SalaryComponentDto(
            String componentName,
            BigDecimal amount,
            ComponentType componentType) {

        this.componentName = componentName;
        this.amount = amount;
        this.componentType = componentType;
    }

    public SalaryComponentDto(
            Long id,
            String componentName,
            BigDecimal amount,
            ComponentType componentType,
            boolean systemDefined) {

        this.id = id;
        this.componentName = componentName;
        this.amount = amount;
        this.componentType = componentType;
        this.systemDefined = systemDefined;
    }

    // =====================================================
    // ID
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // =====================================================
    // COMPONENT NAME
    // =====================================================

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    // =====================================================
    // COMPONENT TYPE
    // =====================================================

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    // =====================================================
    // AMOUNT
    // =====================================================

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // =====================================================
    // SYSTEM DEFINED
    // =====================================================

    public boolean isSystemDefined() {
        return systemDefined;
    }

    public void setSystemDefined(boolean systemDefined) {
        this.systemDefined = systemDefined;
    }
}