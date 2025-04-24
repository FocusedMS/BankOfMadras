package com.bankofmadras.controller;

import com.bankofmadras.annotation.RateLimit;
import com.bankofmadras.dto.PDFGenerationDTO;
import com.bankofmadras.model.Account;
import com.bankofmadras.service.PDFGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class PDFController {
    private final PDFGenerationService pdfGenerationService;

    @RateLimit
    @PostMapping("/generate-pdf")
    public ResponseEntity<byte[]> generateTransactionHistoryPDF(
            @AuthenticationPrincipal Account account,
            @Valid @RequestBody PDFGenerationDTO request) {
        
        byte[] pdfContent = pdfGenerationService.generateTransactionHistoryPDF(account, request);
        
        String filename = String.format("transaction-history-%s-%s-to-%s.pdf",
            account.getAccountNumber(),
            request.getStartDate().format(DateTimeFormatter.ISO_DATE),
            request.getEndDate().format(DateTimeFormatter.ISO_DATE));

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfContent);
    }
} 