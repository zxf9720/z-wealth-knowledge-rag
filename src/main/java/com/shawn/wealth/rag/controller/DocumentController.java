package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.document.BootstrapResponse;
import com.shawn.wealth.rag.dto.document.DocumentResponse;
import com.shawn.wealth.rag.dto.document.IngestResponse;
import com.shawn.wealth.rag.dto.document.TextIngestRequest;
import com.shawn.wealth.rag.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for document ingestion and document management operations.
 *
 * This controller provides endpoints for uploading documents, ingesting raw text,
 * bootstrapping local documents, listing indexed documents, deleting documents,
 * and reindexing existing documents.
 */
@Tag(name = "Document APIs", description = "Endpoints for document ingestion and management")
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(
            summary = "Upload documents",
            description = "Upload one or more files and ingest their content into the vector database.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Documents uploaded and indexed successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = IngestResponse.class)),
                                    examples = @ExampleObject(
                                            name = "Upload Success",
                                            value = """
                                                    [
                                                      {
                                                        "documentId": "550e8400-e29b-41d4-a716-446655440000",
                                                        "status": "SUCCESS",
                                                        "chunks": 4
                                                      },
                                                      {
                                                        "documentId": "660e8400-e29b-41d4-a716-446655440000",
                                                        "status": "SUCCESS",
                                                        "chunks": 2
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<IngestResponse> upload(@RequestPart("files") List<MultipartFile> files) {
        return documentService.upload(files);
    }

    @Operation(
            summary = "Ingest raw text",
            description = "Ingest raw text content into the vector database.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Text ingested successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = IngestResponse.class),
                                    examples = @ExampleObject(
                                            name = "Text Ingest Success",
                                            value = """
                                                    {
                                                      "documentId": "550e8400-e29b-41d4-a716-446655440000",
                                                      "status": "SUCCESS",
                                                      "chunks": 2
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/text")
    public IngestResponse ingestText(@RequestBody TextIngestRequest request) {
        return documentService.ingestText(request);
    }

    @Operation(
            summary = "Bootstrap local documents",
            description = "Scan the local docs folder and ingest supported files into the vector database. If a file with the same name already exists, it will be replaced.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Bootstrap completed successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BootstrapResponse.class),
                                    examples = @ExampleObject(
                                            name = "Bootstrap Success",
                                            value = """
                                                    {
                                                      "indexed": 3,
                                                      "replaced": 1
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/bootstrap")
    public BootstrapResponse bootstrapDocuments() {
        return documentService.bootstrapDocuments();
    }

    @Operation(
            summary = "List documents",
            description = "Return all indexed documents stored in the document repository.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Document list returned successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = DocumentResponse.class)),
                                    examples = @ExampleObject(
                                            name = "Document List",
                                            value = """
                                                    [
                                                      {
                                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                                        "fileName": "tfsa.txt",
                                                        "contentType": "text/plain",
                                                        "status": "INDEXED",
                                                        "metadata": {
                                                          "category": "banking",
                                                          "source": "manual"
                                                        },
                                                        "createdAt": "2026-04-22T12:00:00"
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping
    public List<DocumentResponse> listDocuments() {
        return documentService.listDocuments();
    }

    @Operation(
            summary = "Delete document",
            description = "Delete a document record and all related vector chunks by document ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Document deleted successfully"
                    )
            }
    )
    @DeleteMapping("/{id}")
    public void deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
    }

    @Operation(
            summary = "Reindex document",
            description = "Rebuild vector chunks and embeddings for an existing document.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Document reindexed successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = IngestResponse.class),
                                    examples = @ExampleObject(
                                            name = "Reindex Success",
                                            value = """
                                                    {
                                                      "documentId": "550e8400-e29b-41d4-a716-446655440000",
                                                      "status": "REINDEXED",
                                                      "chunks": 4
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/{id}/reindex")
    public IngestResponse reindexDocument(@PathVariable String id) {
        return documentService.reindexDocument(id);
    }
}