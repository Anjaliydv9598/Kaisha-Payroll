package com.kaisha.payroll.payroll.download;

import com.kaisha.payroll.payroll.entity.Payroll;
import com.kaisha.payroll.payroll.repository.PayrollRepository;
import com.kaisha.payroll.salary.entity.Salary;
import com.kaisha.payroll.salary.repository.SalaryRepository;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PayrollDownloadService {

    private final PayrollRepository payrollRepository;

    private final SalaryRepository salaryRepository;

    private final PayrollPdfService pdfService;

    private final PayrollExcelService excelService;

    private final PayrollCsvService csvService;

    public PayrollDownloadService(
            PayrollRepository payrollRepository,
            SalaryRepository salaryRepository,
            PayrollPdfService pdfService,
            PayrollExcelService excelService,
            PayrollCsvService csvService) {

        this.payrollRepository =
                payrollRepository;

        this.salaryRepository =
                salaryRepository;

        this.pdfService =
                pdfService;

        this.excelService =
                excelService;

        this.csvService =
                csvService;
    }

    // =====================================================
    // MAIN DOWNLOAD METHOD
    // =====================================================

    public byte[] download(
            String payPeriod,
            String scope,
            List<String> employeeIds,
            Integer fromRecord,
            Integer toRecord,
            String format) {

        if (payPeriod == null ||
                payPeriod.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Pay period is required"
            );
        }

        if (format == null ||
                format.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Download format is required"
            );
        }

        List<Payroll> records =
                payrollRepository
                        .findByPayPeriodOrderByEmployeeIdAsc(
                                payPeriod
                        );

        records =
                filterRecords(
                        records,
                        scope,
                        employeeIds,
                        fromRecord,
                        toRecord
                );

        if (records.isEmpty()) {

            throw new RuntimeException(
                    "No payroll records found for "
                            + payPeriod
            );
        }

        String normalizedFormat =
                format.trim().toUpperCase();

        return switch (normalizedFormat) {

            case "CSV" ->
                    csvService.generate(records);

            case "EXCEL" ->
                    excelService.generate(records);

            case "PDF" ->
                    generateCombinedPdf(records);

            case "ZIP" ->
                    generateZip(records);

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported download format: "
                                    + format
                    );
        };
    }

    // =====================================================
    // FILTER RECORDS
    // =====================================================

    private List<Payroll> filterRecords(
            List<Payroll> records,
            String scope,
            List<String> employeeIds,
            Integer fromRecord,
            Integer toRecord) {

        if (records == null ||
                records.isEmpty()) {

            return Collections.emptyList();
        }

        if (scope == null ||
                scope.trim().isEmpty()) {

            return records;
        }

        // =================================================
        // SELECTED EMPLOYEES
        // =================================================

        if ("SELECTED".equalsIgnoreCase(scope)) {

            Set<String> selected =
                    employeeIds == null
                            ? Set.of()
                            : employeeIds
                              .stream()
                              .filter(
                                      Objects::nonNull
                              )
                              .map(
                                      String::trim
                              )
                              .filter(
                                      id ->
                                      !id.isEmpty()
                              )
                              .collect(
                                      Collectors.toSet()
                              );

            if (selected.isEmpty()) {

                throw new IllegalArgumentException(
                        "Please select at least one employee"
                );
            }

            return records.stream()
                    .filter(
                            payroll ->
                                    selected.contains(
                                            payroll.getEmployeeId()
                                    )
                    )
                    .toList();
        }

        // =================================================
        // CUSTOM RECORD RANGE
        // =================================================

        if ("RANGE".equalsIgnoreCase(scope)) {

            int from =
                    fromRecord == null
                            ? 1
                            : fromRecord;

            int to =
                    toRecord == null
                            ? records.size()
                            : toRecord;

            if (from < 1) {
                throw new IllegalArgumentException(
                        "From record must be greater than zero"
                );
            }

            if (to < 1) {
                throw new IllegalArgumentException(
                        "To record must be greater than zero"
                );
            }

            if (from > to) {

                throw new IllegalArgumentException(
                        "From record cannot be greater than To record"
                );
            }

            if (from > records.size()) {

                throw new IllegalArgumentException(
                        "From record is outside the available records"
                );
            }

            if (to > records.size()) {
                to = records.size();
            }

            return records.subList(
                    from - 1,
                    to
            );
        }

        // =================================================
        // ALL EMPLOYEES
        // =================================================

        return records;
    }

    // =====================================================
    // GET SALARY FOR PAYROLL
    // =====================================================

    private Salary getSalaryForPayroll(
            Payroll payroll) {

        return salaryRepository
                .findByEmployeeIdAndPayPeriod(
                        payroll.getEmployeeId(),
                        payroll.getPayPeriod()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Salary not found for employee "
                                        + payroll.getEmployeeId()
                                        + " for pay period "
                                        + payroll.getPayPeriod()
                        )
                );
    }

    // =====================================================
    // COMBINED PDF
    // =====================================================

    private byte[] generateCombinedPdf(
            List<Payroll> records) {

        Document combinedDocument = null;

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        try {

            combinedDocument =
                    new Document();

            PdfCopy pdfCopy =
                    new PdfCopy(
                            combinedDocument,
                            output
                    );

            combinedDocument.open();

            for (Payroll payroll : records) {

                Salary salary =
                        getSalaryForPayroll(payroll);

                byte[] pdf =
                        pdfService.generatePayslip(
                                payroll,
                                salary
                        );

                PdfReader reader =
                        new PdfReader(pdf);

                int numberOfPages =
                        reader.getNumberOfPages();

                for (int page = 1;
                     page <= numberOfPages;
                     page++) {

                    pdfCopy.addPage(
                            pdfCopy.getImportedPage(
                                    reader,
                                    page
                            )
                    );
                }

                reader.close();
            }

            combinedDocument.close();

            return output.toByteArray();

        } catch (Exception e) {

            if (combinedDocument != null) {

                try {

                    if (combinedDocument.isOpen()) {
                        combinedDocument.close();
                    }

                } catch (Exception ignored) {
                    // Ignore secondary close exception
                }
            }

            throw new RuntimeException(
                    "Unable to generate combined PDF",
                    e
            );
        }
    }

    // =====================================================
    // ZIP - INDIVIDUAL PAYSLIPS
    // =====================================================

    private byte[] generateZip(
            List<Payroll> records) {

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            try (
                    ZipOutputStream zip =
                            new ZipOutputStream(output)
            ) {

                for (Payroll payroll : records) {

                    Salary salary =
                            getSalaryForPayroll(
                                    payroll
                            );

                    byte[] pdf =
                            pdfService.generatePayslip(
                                    payroll,
                                    salary
                            );

                    String fileName =
                            payroll.getEmployeeId()
                                    + "_"
                                    + payroll.getPayPeriod()
                                    + "_Payslip.pdf";

                    zip.putNextEntry(
                            new ZipEntry(fileName)
                    );

                    zip.write(pdf);

                    zip.closeEntry();
                }

                zip.finish();
            }

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to generate ZIP",
                    e
            );
        }
    }
}