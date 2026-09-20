package com.kaisha.payroll.payroll.download;

import com.kaisha.payroll.payroll.entity.Payroll;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PayrollExcelService {

    public byte[] generate(
            List<Payroll> payrolls) {

        try (
                Workbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet(
                            "Payroll"
                    );

            Row header =
                    sheet.createRow(0);

            header.createCell(0)
                    .setCellValue("Employee ID");

            header.createCell(1)
                    .setCellValue("Pay Period");

            header.createCell(2)
                    .setCellValue("Gross Salary");

            header.createCell(3)
                    .setCellValue(
                            "Total Deductions"
                    );

            header.createCell(4)
                    .setCellValue("Net Salary");

            header.createCell(5)
                    .setCellValue("Status");

            int rowNumber = 1;

            for (Payroll payroll : payrolls) {

                Row row =
                        sheet.createRow(
                                rowNumber++
                        );

                row.createCell(0)
                        .setCellValue(
                                payroll.getEmployeeId()
                        );

                row.createCell(1)
                        .setCellValue(
                                payroll.getPayPeriod()
                        );

                row.createCell(2)
                        .setCellValue(
                                payroll.getGrossSalary()
                                        .doubleValue()
                        );

                row.createCell(3)
                        .setCellValue(
                                payroll
                                        .getTotalDeductions()
                                        .doubleValue()
                        );

                row.createCell(4)
                        .setCellValue(
                                payroll.getNetSalary()
                                        .doubleValue()
                        );

                row.createCell(5)
                        .setCellValue(
                                payroll.getStatus()
                                        .name()
                        );
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to generate Excel",
                    e
            );
        }
    }
}