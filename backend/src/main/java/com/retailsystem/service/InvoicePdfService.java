package com.retailsystem.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.retailsystem.entity.Sale;
import com.retailsystem.entity.SaleItem;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public byte[] generate(Sale sale) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A5, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(21, 128, 61));
        Font muted = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(90, 90, 90));
        Font body = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        Paragraph heading = new Paragraph(sale.getBranch().getBranchName(), title);
        heading.setAlignment(Element.ALIGN_CENTER);
        document.add(heading);
        Paragraph invoice = new Paragraph(sale.getInvoiceNumber(), bold);
        invoice.setAlignment(Element.ALIGN_CENTER);
        document.add(invoice);
        Paragraph when = new Paragraph(sale.getCreatedAt() != null ? WHEN.format(sale.getCreatedAt()) : "", muted);
        when.setAlignment(Element.ALIGN_CENTER);
        when.setSpacingAfter(10);
        document.add(when);

        document.add(new Paragraph("Cashier: " + sale.getProcessedBy().getFullName(), body));
        document.add(new Paragraph("Customer: " + (sale.getCustomer() != null ? sale.getCustomer().getFullName() : "Walk-in"), body));
        document.add(new Paragraph(" ", body));

        PdfPTable table = new PdfPTable(new float[] { 4f, 1f, 1.4f });
        table.setWidthPercentage(100);
        addHeader(table, "Item");
        addHeader(table, "Qty");
        addHeader(table, "Amount");
        for (SaleItem item : sale.getItems()) {
            addCell(table, item.getProductNameSnapshot(), body);
            addCell(table, String.valueOf(item.getQuantity()), body);
            addCell(table, money(item.getLineTotal()), body);
        }
        document.add(table);
        document.add(new Paragraph(" ", body));

        document.add(line("Subtotal", money(sale.getSubtotal()), body));
        if (sale.getDiscountAmount() != null && sale.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            document.add(line("Discount", "-" + money(sale.getDiscountAmount()), body));
        }
        document.add(line("Tax", money(sale.getTaxAmount()), body));
        document.add(line("Total", money(sale.getTotalAmount()), bold));
        if (sale.getPayment() != null) {
            document.add(line("Payment (" + sale.getPayment().getMethod().name() + ")",
                    money(sale.getPayment().getCashReceived()), body));
            document.add(line("Change", money(sale.getPayment().getChangeAmount()), body));
        }
        if (sale.getLoyaltyPointsEarned() > 0) {
            document.add(line("Loyalty points", "+" + sale.getLoyaltyPointsEarned(), body));
        }

        document.close();
        return out.toByteArray();
    }

    private static void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
        cell.setBackgroundColor(new Color(22, 163, 74));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private static Paragraph line(String label, String value, Font font) {
        return new Paragraph(label + ": " + value, font);
    }

    private static String money(BigDecimal amount) {
        if (amount == null) {
            return "Rs. 0.00";
        }
        return "Rs. " + amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
