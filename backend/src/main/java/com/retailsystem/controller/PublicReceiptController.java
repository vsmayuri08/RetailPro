package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.SaleDTO;
import com.retailsystem.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer-facing digital receipt. Token is an HMAC of the invoice number
 * (same secret as JWT) so invoice numbers cannot be enumerated without it.
 */
@RestController
@RequestMapping("/api/public/receipts")
public class PublicReceiptController {

    @Autowired
    private SaleService saleService;

    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<ApiResponse<SaleDTO>> getReceipt(@PathVariable String invoiceNumber,
                                                             @RequestParam("t") String token) {
        return ResponseEntity.ok(ApiResponse.success(saleService.publicReceipt(invoiceNumber, token)));
    }

    @GetMapping("/{invoiceNumber}/invoice.pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String invoiceNumber,
                                                @RequestParam("t") String token) {
        byte[] pdf = saleService.publicInvoicePdf(invoiceNumber, token);
        return pdfResponse(invoiceNumber, pdf);
    }

    static ResponseEntity<byte[]> pdfResponse(String invoiceNumber, byte[] pdf) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(invoiceNumber + ".pdf")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
