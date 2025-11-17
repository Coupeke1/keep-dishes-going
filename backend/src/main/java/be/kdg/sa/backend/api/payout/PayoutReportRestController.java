package be.kdg.sa.backend.api.payout;

import be.kdg.sa.backend.application.services.payment.PayoutReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
@Slf4j
public class PayoutReportRestController {

    private final PayoutReportService payoutReportService;

    @GetMapping("/report")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<ByteArrayResource> getPayoutReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        log.info("Generating payout report for {} - {}", startDate, endDate);

        byte[] pdf = payoutReportService.generatePayoutReport(startDate, endDate);

        ByteArrayResource resource = new ByteArrayResource(pdf);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=payout-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(resource);
    }
}
