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

        // Erst in /tmp spulen (wenig Heap auf Render)
        final List<Path> tempFiles = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
                if (!original.endsWith(".pdf")) {
                    return ResponseEntity.badRequest().build();
                }
            }

            for (MultipartFile file : files) {
                Path tmp = Files.createTempFile("pdf-merge-", ".pdf");
                tempFiles.add(tmp);
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            if (!filename.toLowerCase().endsWith(".pdf")) {
                filename += ".pdf";
            }
            String finalFilename = filename;

            StreamingResponseBody stream = outputStream -> {
                try {
                    PDFMergerUtility pdfMerger = new PDFMergerUtility();
                    for (Path p : tempFiles) {
                        pdfMerger.addSource(p.toFile());
                    }
                    pdfMerger.setDestinationStream(outputStream);

                    // PDFBox 3.x: StreamCacheCreateFunction statt MemoryUsageSetting
                    // Tempfile-only Cache reduziert Heap-Spikes
                    pdfMerger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());
                    outputStream.flush();
                } finally {
                    // Tempfiles erst NACH dem Merge löschen
                    for (Path p : tempFiles) {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    }
                }
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFilename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(stream);

        } catch (IOException e) {
            // Falls schon vor dem Streaming etwas schiefgeht, Tempfiles hier aufräumen
            for (Path p : tempFiles) {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            }
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PDF Merger Service is running!");
    }
}
