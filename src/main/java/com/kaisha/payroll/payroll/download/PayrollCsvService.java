package com.kaisha.payroll.payroll.download;

import com.kaisha.payroll.payroll.entity.Payroll;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class PayrollCsvService {

    public byte[] generate(
            List<Payroll> payrolls) {

        StringBuilder csv =
                new StringBuilder();

        csv.append(
                "Employee ID,"
                        + "Pay Period,"
                        + "Gross Salary,"
                        + "Total Deductions,"
                        + "Net Salary,"
                        + "Status\n"
        );

        for (Payroll payroll : payrolls) {

            csv.append(
                    escape(
                            payroll.getEmployeeId()
                    )
            );

            csv.append(",");

            csv.append(
                    escape(
                            payroll.getPayPeriod()
                    )
            );

            csv.append(",");

            csv.append(
                    payroll.getGrossSalary()
            );

            csv.append(",");

            csv.append(
                    payroll.getTotalDeductions()
            );

            csv.append(",");

            csv.append(
                    payroll.getNetSalary()
            );

            csv.append(",");

            csv.append(
                    payroll.getStatus()
                            .name()
            );

            csv.append("\n");
        }

        return csv.toString()
                .getBytes(
                        StandardCharsets.UTF_8
                );
    }

    private String escape(
            String value) {

        if (value == null) {
            return "";
        }

        if (value.contains(",") ||
                value.contains("\"")) {

            return "\""
                    + value.replace(
                    "\"",
                    "\"\""
            )
                    + "\"";
        }

        return value;
    }
}