package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.document.KnowledgeDocument;
import com.shawn.wealth.rag.document.dto.BootstrapResponse;
import com.shawn.wealth.rag.document.dto.DocumentResponse;
import com.shawn.wealth.rag.document.dto.IngestResponse;
import com.shawn.wealth.rag.document.dto.TextIngestRequest;
import com.shawn.wealth.rag.repository.InMemoryDocumentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final String STATUS_INDEXED = "INDEXED";
    private static final String STATUS_REINDEXED = "REINDEXED";
    private static final String DEFAULT_TEXT_FILE_NAME = "raw-text";
    private static final String TEXT_CONTENT_TYPE = "text/plain";
    private static final String LOCAL_DOCS_PATH = "src/main/resources/docs";

    private final VectorStore vectorStore;
    private final InMemoryDocumentRepository documentRepository;

    public DocumentServiceImpl(VectorStore vectorStore,
                               InMemoryDocumentRepository documentRepository) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
    }

    @Override
    public List<IngestResponse> upload(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files were provided");
        }

        return files.stream()
                .map(this::uploadSingleFile)
                .collect(Collectors.toList());
    }

    private IngestResponse uploadSingleFile(MultipartFile file) {
        validateFile(file);

        try {
            String rawText = extractText(file);
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();

            replaceExistingDocumentByFileName(fileName);

            String documentId = UUID.randomUUID().toString();
            Map<String, Object> metadata = buildFileMetadata(documentId, file);

            return indexDocument(fileName, contentType, rawText, metadata);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and index document: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public IngestResponse ingestText(TextIngestRequest request) {
        String logicalFileName = request.fileName() == null ? DEFAULT_TEXT_FILE_NAME : request.fileName();

        replaceExistingDocumentByFileName(logicalFileName);

        String documentId = UUID.randomUUID().toString();
        Map<String, Object> metadata = buildTextMetadata(documentId, request);

        return indexDocument(
                logicalFileName,
                TEXT_CONTENT_TYPE,
                request.text(),
                metadata
        );
    }

    @Override
    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAll().stream()
                .map(document -> new DocumentResponse(
                        document.getId(),
                        document.getFileName(),
                        document.getContentType(),
                        document.getStatus(),
                        document.getMetadata(),
                        document.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDocument(String id) {
        KnowledgeDocument document = documentRepository.findById(id);
        if (document == null) {
            throw new IllegalArgumentException("Document not found: " + id);
        }

        try {
            vectorStore.delete("documentId == '" + id + "'");
            documentRepository.delete(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete document and its vectors: " + id, e);
        }
    }

    @Override
    public IngestResponse reindexDocument(String id) {
        KnowledgeDocument document = documentRepository.findById(id);
        if (document == null) {
            throw new IllegalArgumentException("Document not found: " + id);
        }

        try {
            vectorStore.delete("documentId == '" + id + "'");

            List<Document> chunks = toChunks(document.getRawText(), document.getMetadata());
            vectorStore.add(chunks);

            document.setUpdatedAt(LocalDateTime.now());
            document.setStatus(STATUS_REINDEXED);
            documentRepository.save(document);

            return new IngestResponse(id, STATUS_REINDEXED, chunks.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to reindex document: " + id, e);
        }
    }

    @Override
    public BootstrapResponse bootstrapDocuments() {
        File docsFolder = new File(LOCAL_DOCS_PATH);

        if (!docsFolder.exists() || !docsFolder.isDirectory()) {
            throw new IllegalArgumentException("Docs folder not found: " + docsFolder.getAbsolutePath());
        }

        File[] files = docsFolder.listFiles(this::isSupportedFile);
        if (files == null || files.length == 0) {
            return new BootstrapResponse(0, 0);
        }

        int indexed = 0;
        int replaced = 0;

        for (File file : files) {
            String fileName = file.getName();

            try {
                boolean existed = replaceExistingDocumentByFileName(fileName);

                String rawText = extractText(file);
                String contentType = resolveContentType(fileName);
                String documentId = UUID.randomUUID().toString();
                Map<String, Object> metadata = buildFileMetadata(documentId, fileName, contentType);

                indexDocument(fileName, contentType, rawText, metadata);
                indexed++;

                if (existed) {
                    replaced++;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to bootstrap file: " + fileName, e);
            }
        }

        return new BootstrapResponse(indexed, replaced);
    }

    private IngestResponse indexDocument(
            String fileName,
            String contentType,
            String rawText,
            Map<String, Object> metadata
    ) {
        String documentId = (String) metadata.get("documentId");

        KnowledgeDocument knowledgeDocument = new KnowledgeDocument(
                documentId,
                fileName,
                contentType,
                rawText,
                metadata,
                STATUS_INDEXED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        documentRepository.save(knowledgeDocument);

        List<Document> chunks = toChunks(rawText, metadata);
        vectorStore.add(chunks);

        return new IngestResponse(documentId, "SUCCESS", chunks.size());
    }

    private boolean replaceExistingDocumentByFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }

        KnowledgeDocument existing = documentRepository.findByFileName(fileName);
        if (existing == null) {
            return false;
        }

        deleteDocument(existing.getId());
        return true;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is missing");
        }

        String lowerName = fileName.toLowerCase();
        if (!(lowerName.endsWith(".txt")
                || lowerName.endsWith(".md")
                || lowerName.endsWith(".pdf")
                || lowerName.endsWith(".docx"))) {
            throw new IllegalArgumentException("Unsupported file type: " + fileName);
        }
    }

    private String extractText(MultipartFile file) throws Exception {
        String fileName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();

        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        if (fileName.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        }

        if (fileName.endsWith(".docx")) {
            return extractTextFromDocx(file);
        }

        throw new IllegalArgumentException("Unsupported file type: " + fileName);
    }

    private String extractText(File file) throws Exception {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        }

        if (fileName.endsWith(".pdf")) {
            try (PDDocument pdf = Loader.loadPDF(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(pdf);
            }
        }

        if (fileName.endsWith(".docx")) {
            try (InputStream inputStream = new FileInputStream(file);
                 XWPFDocument docx = new XWPFDocument(inputStream);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
                return extractor.getText();
            }
        }

        throw new IllegalArgumentException("Unsupported file type: " + file.getName());
    }

    private String extractTextFromPdf(MultipartFile file) throws Exception {
        try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
    }

    private String extractTextFromDocx(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument docx = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
            return extractor.getText();
        }
    }

    private Map<String, Object> buildFileMetadata(String documentId, MultipartFile file) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", documentId);
        metadata.put("fileName", file.getOriginalFilename());
        metadata.put("contentType", file.getContentType());
        return metadata;
    }

    private Map<String, Object> buildFileMetadata(String documentId, String fileName, String contentType) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", documentId);
        metadata.put("fileName", fileName);
        metadata.put("contentType", contentType);
        return metadata;
    }

    private Map<String, Object> buildTextMetadata(String documentId, TextIngestRequest request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", documentId);
        metadata.put("fileName", request.fileName() == null ? DEFAULT_TEXT_FILE_NAME : request.fileName());

        if (request.metadata() != null) {
            metadata.putAll(request.metadata());
        }

        return metadata;
    }

    private List<Document> toChunks(String text, Map<String, Object> metadata) {
        List<Document> chunks = new ArrayList<>();

        for (int start = 0; start < text.length(); start += DEFAULT_CHUNK_SIZE) {
            int end = Math.min(start + DEFAULT_CHUNK_SIZE, text.length());
            String chunkText = text.substring(start, end);

            Map<String, Object> chunkMetadata = new HashMap<>(metadata);
            chunkMetadata.put("chunkStart", start);
            chunkMetadata.put("chunkEnd", end);

            chunks.add(new Document(chunkText, chunkMetadata));
        }

        return chunks;
    }

    private boolean isSupportedFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }

        String lowerName = file.getName().toLowerCase();
        return lowerName.endsWith(".txt")
                || lowerName.endsWith(".md")
                || lowerName.endsWith(".pdf")
                || lowerName.endsWith(".docx");
    }

    private String resolveContentType(String fileName) {
        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".txt")) {
            return "text/plain";
        }
        if (lowerName.endsWith(".md")) {
            return "text/markdown";
        }
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lowerName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        return "application/octet-stream";
    }
}