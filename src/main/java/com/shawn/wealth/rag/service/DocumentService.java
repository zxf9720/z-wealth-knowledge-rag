package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.document.dto.BootstrapResponse;
import com.shawn.wealth.rag.document.dto.DocumentResponse;
import com.shawn.wealth.rag.document.dto.IngestResponse;
import com.shawn.wealth.rag.document.dto.TextIngestRequest;
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