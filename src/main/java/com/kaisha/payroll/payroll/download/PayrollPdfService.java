package com.kaisha.payroll.payroll.download;

import com.kaisha.payroll.payroll.entity.Payroll;
import com.kaisha.payroll.salary.entity.Salary;
import com.kaisha.payroll.salary.entity.SalaryComponent;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PayrollPdfService {

    // =====================================================
    // GENERATE INDIVIDUAL PAYSLIP
    // =====================================================

    public byte[] generatePayslip(
            Payroll payroll,
            Salary salary) {

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            Document document =
                    new Document();

            PdfWriter.getInstance(
                    document,
                    output
            );

            document.open();

            // =================================================
            // HEADER
            // =================================================

            document.add(
                    new Paragraph(
                            "KAISHA PAYROLL"
                    )
            );

            document.add(
                    new Paragraph(
                            "PAYSLIP"
                    )
            );

            document.add(
                    new Paragraph(
                            " "
                    )
            );

            // =================================================
            // EMPLOYEE INFORMATION
            // =================================================

            document.add(
                    new Paragraph(
                            "Employee ID: "
                                    + payroll.getEmployeeId()
                    )
            );

            document.add(
                    new Paragraph(
                            "Pay Period: "
                                    + payroll.getPayPeriod()
                    )
            );

            document.add(
                    new Paragraph(
                            " "
                    )
            );

            // =================================================
            // EARNINGS
            // =================================================

            document.add(
                    new Paragraph(
                            "EARNINGS"
                    )
            );

            for (SalaryComponent component :
                    salary.getComponents()) {

                if (
                        component.getComponentType()
                                == SalaryComponent.ComponentType.EARNING
                ) {

                    document.add(
                            new Paragraph(
                                    component
                                            .getComponentName()
                                            + " : ₹"
                                            + component
                                            .getAmount()
                            )
                    );
                }
            }

            document.add(
                    new Paragraph(
                            "Gross Salary : ₹"
                                    + payroll.getGrossSalary()
                    )
            );

            document.add(
                    new Paragraph(
                            " "
                    )
            );

            // =================================================
            // DEDUCTIONS
            // =================================================

            document.add(
                    new Paragraph(
                            "DEDUCTIONS & LIABILITIES"
                    )
            );

            for (SalaryComponent component :
                    salary.getComponents()) {

                if (
                        component.getComponentType()
                                == SalaryComponent.ComponentType.DEDUCTION
                ) {

                    document.add(
                            new Paragraph(
                                    component
                                            .getComponentName()
                                            + " : ₹"
                                            + component
                                            .getAmount()
                            )
                    );
                }
            }

            document.add(
                    new Paragraph(
                            "Total Deductions : ₹"
                                    + payroll.getTotalDeductions()
                    )
            );

            document.add(
                    new Paragraph(
                            " "
                    )
            );

            // =================================================
            // NET SALARY
            // =================================================

            document.add(
                    new Paragraph(
                            "NET SALARY : ₹"
                                    + payroll.getNetSalary()
                    )
            );

            // =================================================
            // CLOSE
            // =================================================

            document.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to generate payslip PDF",
                    e
            );
        }
    }
}