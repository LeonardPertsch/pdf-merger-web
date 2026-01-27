package com.pdfmerger;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PdfMergerController {

    @PostMapping("/merge")
    public ResponseEntity<Resource> mergePdfs(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "filename", defaultValue = "merged_output.pdf") String filename) {

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().build();
        }

        List<PDDocument> documents = new ArrayList<>();

        try {
            // Validiere alle Dateien
            for (MultipartFile file : files) {
                if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                    return ResponseEntity.badRequest().build();
                }
            }

            // Erstelle PDFMergerUtility
            PDFMergerUtility pdfMerger = new PDFMergerUtility();

            // Fuege alle PDFs hinzu
            for (MultipartFile file : files) {
                try {
                    byte[] fileBytes = file.getBytes();
                    RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(fileBytes);
                    pdfMerger.addSource(buffer);

                    // Laden fuer spaeteres Cleanup
                    PDDocument doc = Loader.loadPDF(fileBytes);
                    documents.add(doc);
                } catch (IOException e) {
                    // Cleanup bei Fehler
                    closeAllDocuments(documents);
                    throw e;
                }
            }

            // Merge zu ByteArray
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            pdfMerger.setDestinationStream(outputStream);
            pdfMerger.mergeDocuments(null);

            // Cleanup
            closeAllDocuments(documents);

            byte[] mergedPdfBytes = outputStream.toByteArray();
            outputStream.close();

            // Stelle sicher, dass Filename .pdf hat
            if (!filename.endsWith(".pdf")) {
                filename += ".pdf";
            }

            // Erstelle Response
            ByteArrayResource resource = new ByteArrayResource(mergedPdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(mergedPdfBytes.length)
                    .body(resource);

        } catch (IOException e) {
            e.printStackTrace();
            closeAllDocuments(documents);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void closeAllDocuments(List<PDDocument> documents) {
        for (PDDocument doc : documents) {
            try {
                if (doc != null) {
                    doc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PDF Merger Service is running!");
    }
}