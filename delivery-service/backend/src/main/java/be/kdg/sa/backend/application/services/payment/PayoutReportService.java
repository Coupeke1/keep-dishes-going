package be.kdg.sa.backend.application.services.payment;

import be.kdg.sa.backend.api.delivery.dto.CompletedDeliveriesResponse;
import be.kdg.sa.backend.application.services.driver.DriverService;
import be.kdg.sa.backend.infrastructure.db.repositories.driverRepository.DriverRepository;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Slf4j
@Transactional
public class PayoutReportService {
    private final DriverRepository driverRepository;
    private final DriverService driverService;

    public PayoutReportService(DriverRepository driverRepository,
                               DriverService driverService) {
        this.driverRepository = driverRepository;
        this.driverService = driverService;
    }

    public byte[] generatePayoutReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating payout report for {} - {}", startDate, endDate);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.addTitle("Uitbetalingsrapport");
            document.add(new Paragraph("Uitbetalingsrapport (PDF)"));
            document.add(new Paragraph(String.format("Periode: %s t/m %s", startDate, endDate)));
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Koerier");
            table.addCell("Aantal leveringen");
            table.addCell("Totaal uitbetaald (€)");
            driverRepository.findAll().forEach(driver -> {
                CompletedDeliveriesResponse response = driverService.getCompletedDeliveriesForDriver(driver.getId());
                table.addCell(driver.getName());
                table.addCell(String.valueOf(response.getDeliveries().size()));
                table.addCell(response.getTotal().setScale(2, RoundingMode.HALF_UP).toPlainString());
            });
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Kon PDF niet genereren", e);
        }
    }
}
