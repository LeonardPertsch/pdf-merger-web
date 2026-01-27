package com.pdfmerger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@SpringBootApplication
public class PdfMergerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfMergerApplication.class, args);
        System.out.println("\n==============================================");
        System.out.println("PDF Merger Web läuft auf: http://localhost:8080");
        System.out.println("==============================================\n");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
