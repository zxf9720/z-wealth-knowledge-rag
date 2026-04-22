package com.shawn.wealth.rag.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagLoadService {

    private final VectorStore vectorStore;

    public RagLoadService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public String loadSampleDocuments() {
        List<Document> documents = List.of(
                new Document(
                        "A savings account is used for storing money and earning interest.",
                        Map.of("topic", "banking", "type", "faq")
                ),
                new Document(
                        "A chequing account is used for daily transactions such as deposits, withdrawals, and bill payments.",
                        Map.of("topic", "banking", "type", "faq")
                ),
                new Document(
                        "A GIC is a guaranteed investment certificate with a fixed term and usually a fixed interest rate.",
                        Map.of("topic", "banking", "type", "faq")
                )
        );

        vectorStore.add(documents);
        return "Loaded " + documents.size() + " documents into Qdrant.";
    }

    public String loadFromFile() throws IOException {

        String fileName = "banking.txt";

        ClassPathResource resource = new ClassPathResource("docs/" + fileName);
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        int maxChunkSize = 200;
        int overlap = 40;

        List<String> chunks = splitIntoChunks(content, maxChunkSize, overlap);

        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            documents.add(new Document(
                    chunks.get(i),
                    Map.of(
                            "source", fileName,
                            "chunkIndex", i,
                            "category", "banking-faq"
                    )
            ));
        }

        vectorStore.add(documents);

        return "Loaded " + documents.size() + " clean chunks.";
    }

//    public String loadFromFile() throws IOException {
//
//        String fileName = "banking.txt";
//
//        ClassPathResource resource = new ClassPathResource("docs/" + fileName);
//        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
//
//        // 👉 chunk 参数（可以调）
//        int chunkSize = 100;     // 每块字符数
//        int overlap = 20;        // 重叠
//
//        List<Document> documents = new ArrayList<>();
//
//        int index = 0;
//        int chunkIndex = 0;
//
//        while (index < content.length()) {
//
//            int end = Math.min(index + chunkSize, content.length());
//            String chunk = content.substring(index, end).trim();
//
//            if (!chunk.isBlank()) {
//                documents.add(new Document(
//                        chunk,
//                        Map.of(
//                                "source", fileName,
//                                "chunkIndex", chunkIndex,
//                                "category", "banking-faq"
//                        )
//                ));
//                chunkIndex++;
//            }
//
//            index += (chunkSize - overlap);
//        }
//
//        vectorStore.add(documents);
//
//        return "Loaded " + documents.size() + " chunks.";
//    }

//    public String loadFromFile() throws IOException {
//        ClassPathResource resource = new ClassPathResource("docs/banking.txt");
//        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
//
//        String[] parts = content.split("\\n\\n");
//
//        List<Document> documents = Arrays.stream(parts)
//                .map(String::trim)
//                .filter(s -> !s.isBlank())
//                .map(text -> new Document(
//                        text,
//                        Map.of(
//                                "source", "banking.txt",
//                                "category", "banking-faq"
//                        )
//                ))
//                .toList();
//
//        vectorStore.add(documents);
//
//        return "Loaded " + documents.size() + " documents from file.";
//    }

//    public String loadFromFile() throws IOException {
//
//        // 读取文件
//        ClassPathResource resource = new ClassPathResource("docs/banking.txt");
//        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
//
//        // 简单按段落切分
//        String[] parts = content.split("\n\n");
//
//        List<Document> documents = List.of(parts)
//                .stream()
//                .map(text -> new Document(text))
//                .toList();
//
//        vectorStore.add(documents);
//
//        return "Loaded " + documents.size() + " documents from file.";
//    }


//    private List<String> splitIntoChunks(String content, int maxChunkSize, int overlap) {
//
//        List<String> chunks = new ArrayList<>();
//
//        // 1️⃣ 先按段落分
//        String[] paragraphs = content.split("\\n\\n");
//
//        for (String paragraph : paragraphs) {
//
//            paragraph = paragraph.trim();
//            if (paragraph.isBlank()) continue;
//
//            // 2️⃣ 如果段落够短，直接用
//            if (paragraph.length() <= maxChunkSize) {
//                chunks.add(paragraph);
//                continue;
//            }
//
//            // 3️⃣ 段落太长 → 再按句子切
//            String[] sentences = paragraph.split("(?<=[.!?])\\s+");
//
//            StringBuilder current = new StringBuilder();
//
//            for (String sentence : sentences) {
//
//                if (current.length() + sentence.length() > maxChunkSize) {
//                    chunks.add(current.toString().trim());
//
//                    // overlap（保留尾部）
//                    int start = Math.max(0, current.length() - overlap);
//                    current = new StringBuilder(current.substring(start));
//                }
//
//                current.append(sentence).append(" ");
//            }
//
//            if (!current.isEmpty()) {
//                chunks.add(current.toString().trim());
//            }
//        }
//
//        return chunks;
//    }

    public String loadAllFiles() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:docs/*.txt");

        List<Document> allDocuments = new ArrayList<>();
        int totalFiles = 0;

        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName == null) {
                continue;
            }

            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            int maxChunkSize = 200;
            int overlap = 40;

            List<String> chunks = splitIntoChunks(content, maxChunkSize, overlap);

            for (int i = 0; i < chunks.size(); i++) {
                allDocuments.add(new Document(
                        chunks.get(i),
                        Map.of(
                                "source", fileName,
                                "chunkIndex", i,
                                "category", getCategoryFromFileName(fileName)
                        )
                ));
            }

            totalFiles++;
        }

        vectorStore.add(allDocuments);

        return "Loaded " + allDocuments.size() + " chunks from " + totalFiles + " files.";
    }

//    private String getCategoryFromFileName(String fileName) {
//        int dotIndex = fileName.lastIndexOf('.');
//        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
//    }

//    private List<String> splitIntoChunks(String content, int maxChunkSize, int overlap) {
//        List<String> chunks = new ArrayList<>();
//
//        String[] paragraphs = content.split("\\n\\n");
//
//        for (String paragraph : paragraphs) {
//            paragraph = paragraph.trim();
//            if (paragraph.isBlank()) {
//                continue;
//            }
//
//            if (paragraph.length() <= maxChunkSize) {
//                chunks.add(paragraph);
//                continue;
//            }
//
//            String[] sentences = paragraph.split("(?<=[.!?])\\s+");
//            StringBuilder current = new StringBuilder();
//
//            for (String sentence : sentences) {
//                if (current.length() + sentence.length() > maxChunkSize) {
//                    if (!current.isEmpty()) {
//                        chunks.add(current.toString().trim());
//
//                        int start = Math.max(0, current.length() - overlap);
//                        current = new StringBuilder(current.substring(start).trim());
//                        if (!current.isEmpty()) {
//                            current.append(" ");
//                        }
//                    }
//                }
//
//                current.append(sentence).append(" ");
//            }
//
//            if (!current.isEmpty()) {
//                chunks.add(current.toString().trim());
//            }
//        }
//
//        return chunks;
//    }

    public String loadAllPdfFiles() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:docs/*.pdf");

        List<Document> allDocuments = new ArrayList<>();
        int totalFiles = 0;

        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName == null) {
                continue;
            }

            PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
            List<Document> pdfDocs = reader.get();

//            for (int i = 0; i < pdfDocs.size(); i++) {
//                Document doc = pdfDocs.get(i);
//
//                allDocuments.add(new Document(
//                        doc.getText(),
//                        Map.of(
//                                "source", fileName,
//                                "pageNumber", i + 1,
//                                "category", getCategoryFromFileName(fileName)
//                        )
//                ));
//            }

            for (int pageIndex = 0; pageIndex < pdfDocs.size(); pageIndex++) {

                Document doc = pdfDocs.get(pageIndex);

                String content = cleanText(doc.getText());

                List<String> chunks = splitIntoChunks(content, 200, 40);

                for (int i = 0; i < chunks.size(); i++) {
                    allDocuments.add(new Document(
                            chunks.get(i),
                            Map.of(
                                    "source", fileName,
                                    "pageNumber", pageIndex + 1,  // 👈 从1开始更自然
                                    "chunkIndex", i,
                                    "category", getCategoryFromFileName(fileName)
                            )
                    ));
                }
            }

            totalFiles++;
        }

        vectorStore.add(allDocuments);

        return "Loaded " + allDocuments.size() + " PDF page documents from " + totalFiles + " PDF files.";
    }

//    private String getCategoryFromFileName(String fileName) {
//        int dotIndex = fileName.lastIndexOf('.');
//        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
//    }

//    /**
//     * clean blank space
//     * @param text
//     * @return
//     */
//    private String cleanText(String text) {
//        return text.replaceAll("\\s+", " ").trim();
//    }

    public String loadAllSupportedFiles() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        List<Document> allDocuments = new ArrayList<>();

        int txtFileCount = loadTxtFiles(resolver, allDocuments);
        int pdfFileCount = loadPdfFiles(resolver, allDocuments);

        vectorStore.add(allDocuments);

        return "Loaded " + allDocuments.size() + " chunks from "
                + txtFileCount + " txt files and "
                + pdfFileCount + " pdf files.";
    }

    private int loadTxtFiles(PathMatchingResourcePatternResolver resolver, List<Document> allDocuments) throws IOException {
        Resource[] resources = resolver.getResources("classpath:docs/*.txt");
        int totalFiles = 0;

        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName == null) {
                continue;
            }

            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            content = cleanText(content);

            List<String> chunks = splitIntoChunks(content, 200, 40);

            for (int i = 0; i < chunks.size(); i++) {
                allDocuments.add(new Document(
                        chunks.get(i),
                        Map.of(
                                "source", fileName,
                                "fileType", "txt",
                                "chunkIndex", i,
                                "category", getCategoryFromFileName(fileName)
                        )
                ));
            }

            totalFiles++;
        }

        return totalFiles;
    }

    private int loadPdfFiles(PathMatchingResourcePatternResolver resolver, List<Document> allDocuments) throws IOException {
        Resource[] resources = resolver.getResources("classpath:docs/*.pdf");
        int totalFiles = 0;

        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName == null) {
                continue;
            }

            PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
            List<Document> pdfDocs = reader.get();

            for (int pageIndex = 0; pageIndex < pdfDocs.size(); pageIndex++) {
                Document pageDoc = pdfDocs.get(pageIndex);

                String content = cleanText(pageDoc.getText());
                List<String> chunks = splitIntoChunks(content, 200, 40);

                for (int i = 0; i < chunks.size(); i++) {
                    allDocuments.add(new Document(
                            chunks.get(i),
                            Map.of(
                                    "source", fileName,
                                    "fileType", "pdf",
                                    "pageNumber", pageIndex + 1,
                                    "chunkIndex", i,
                                    "category", getCategoryFromFileName(fileName)
                            )
                    ));
                }
            }

            totalFiles++;
        }

        return totalFiles;
    }

    private String getCategoryFromFileName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private String cleanText(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    private List<String> splitIntoChunks(String content, int maxChunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        String[] paragraphs = content.split("\\n\\n");

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isBlank()) {
                continue;
            }

            if (paragraph.length() <= maxChunkSize) {
                chunks.add(paragraph);
                continue;
            }

            String[] sentences = paragraph.split("(?<=[.!?])\\s+");
            StringBuilder current = new StringBuilder();

            for (String sentence : sentences) {
                if (current.length() + sentence.length() > maxChunkSize) {
                    if (!current.isEmpty()) {
                        chunks.add(current.toString().trim());

                        int start = Math.max(0, current.length() - overlap);
                        current = new StringBuilder(current.substring(start).trim());
                        if (!current.isEmpty()) {
                            current.append(" ");
                        }
                    }
                }

                current.append(sentence).append(" ");
            }

            if (!current.isEmpty()) {
                chunks.add(current.toString().trim());
            }
        }

        return chunks;
    }

}