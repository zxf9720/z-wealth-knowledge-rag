package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.document.BootstrapResponse;
import com.shawn.wealth.rag.dto.document.DocumentResponse;
import com.shawn.wealth.rag.dto.document.IngestResponse;
import com.shawn.wealth.rag.dto.document.TextIngestRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    List<IngestResponse> upload(List<MultipartFile> files);

    IngestResponse ingestText(TextIngestRequest request);

    List<DocumentResponse> listDocuments();

    void deleteDocument(String id);

    IngestResponse reindexDocument(String id);

    BootstrapResponse bootstrapDocuments();
}