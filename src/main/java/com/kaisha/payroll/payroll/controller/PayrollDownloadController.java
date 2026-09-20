package com.kaisha.payroll.payroll.controller;

import com.kaisha.payroll.payroll.download.PayrollDownloadService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollDownloadController {

    private final PayrollDownloadService downloadService;

    public PayrollDownloadController(
            PayrollDownloadService downloadService) {

        this.downloadService = downloadService;
    }

    // =====================================================
    // DOWNLOAD PAYROLL / PAYSLIPS
    // =====================================================

    @PostMapping("/download")
    public ResponseEntity<byte[]> download(

            @RequestParam String payPeriod,

            @RequestParam String scope,

            @RequestParam(required = false)
            List<String> employeeIds,

            @RequestParam(required = false)
            Integer fromRecord,

            @RequestParam(required = false)
            Integer toRecord,

            @RequestParam String format) {

        byte[] data =
                downloadService.download(
                        payPeriod,
                        scope,
                        employeeIds,
                        fromRecord,
                        toRecord,
                        format
                );

        String normalizedFormat =
                format.trim().toUpperCase();

        MediaType mediaType;
        String extension;

        switch (normalizedFormat) {

            case "CSV":

                mediaType =
                        MediaType.parseMediaType(
                                "text/csv"
                        );

                extension = "csv";

                break;

            case "EXCEL":

                mediaType =
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        );

                extension = "xlsx";

                break;

            case "PDF":

                mediaType =
                        MediaType.APPLICATION_PDF;

                extension = "pdf";

                break;

            case "ZIP":

                mediaType =
                        MediaType.parseMediaType(
                                "application/zip"
                        );

                extension = "zip";

                break;

            default:

                throw new IllegalArgumentException(
                        "Unsupported download format: "
                                + format
                );
        }

        String filename =
                "Payroll_"
                        + payPeriod
                        + "."
                        + extension;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + filename
                                + "\""
                )
                .body(data);
    }
}