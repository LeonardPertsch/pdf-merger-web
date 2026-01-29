package com.pdfmerger;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PdfMergerController {

    @PostMapping("/merge")
    public ResponseEntity<StreamingResponseBody> mergePdfs(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "filename", defaultValue = "merged_output.pdf") String filename) {

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().build();
        }

        // Validation: Nur PDFs erlauben
        for (MultipartFile file : files) {
            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            if (!original.endsWith(".pdf")) {
                return ResponseEntity.badRequest().build();
            }
        }

        // Temp files speichern (KRITISCH für Render - nicht im RAM halten!)
        final List<Path> tempFiles = new ArrayList<>();

        try {
            // Dateien sofort auf Disk schreiben
            for (MultipartFile file : files) {
                Path tmp = Files.createTempFile("pdf-", ".pdf");
                tempFiles.add(tmp);
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // Filename bereinigen
            if (!filename.toLowerCase().endsWith(".pdf")) {
                filename += ".pdf";
            }
            String finalFilename = filename;

            // WICHTIG: Streaming Response - kein großer Buffer im RAM!
            StreamingResponseBody stream = outputStream -> {
                try {
                    PDFMergerUtility pdfMerger = new PDFMergerUtility();

                    // Files hinzufügen
                    for (Path p : tempFiles) {
                        pdfMerger.addSource(p.toFile());
                    }

                    pdfMerger.setDestinationStream(outputStream);

                    // 🚀 KRITISCH für Render: Temp-file basiertes Caching
                    // PDFBox 3.x nutzt StreamCacheCreateFunction
                    // IOUtils.createTempFileOnlyStreamCache() = kein RAM, nur Disk!
                    pdfMerger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());

                    outputStream.flush();

                } finally {
                    // Cleanup NACH dem Merge
                    for (Path p : tempFiles) {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {}
                    }
                }
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFilename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(stream);

        } catch (IOException e) {
            // Cleanup bei Fehler
            for (Path p : tempFiles) {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {}
            }
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PDF Merger Service is running!");
    }

    // Bonus: Validate Endpoint für Debugging
    @PostMapping("/validate")
    public ResponseEntity<String> validatePdfs(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("No files uploaded");
        }

        StringBuilder info = new StringBuilder();
        info.append("Files: ").append(files.length).append("\n");

        long totalSize = 0;
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            totalSize += file.getSize();
            info.append(String.format("%d. %s - %.2f MB\n",
                    i + 1,
                    file.getOriginalFilename(),
                    file.getSize() / (1024.0 * 1024.0)));
        }

        info.append(String.format("\nTotal: %.2f MB\n", totalSize / (1024.0 * 1024.0)));

        // Render Free Limit: ~100 MB sicher
        if (totalSize > 100 * 1024 * 1024) {
            info.append("⚠️ WARNING: Total size exceeds recommended Render Free limits!\n");
            info.append("Consider:\n");
            info.append("- Upgrading to Render Starter ($7/mo)\n");
            info.append("- Or reducing file sizes\n");
        }

        return ResponseEntity.ok(info.toString());
    }
}