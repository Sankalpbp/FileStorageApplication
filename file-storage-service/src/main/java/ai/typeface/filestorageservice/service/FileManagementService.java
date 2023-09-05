package ai.typeface.filestorageservice.service;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.dtos.FileMetadataPageResponse;
import com.google.cloud.storage.Blob;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileManagementService {

    public UUID upload (MultipartFile file );

    public Blob download ( UUID filename );

    public String delete ( UUID fileIdentifier );

    public FileMetadataDTO updateFileData ( MultipartFile file, UUID fileIdentifier );

    public FileMetadataDTO updateMetadata ( FileMetadataDTO metadata, UUID fileIdentifier );

    public FileMetadataPageResponse getAllFiles (int pageNumber, int pageSize, String sortBy, String sortDir );

    default HttpHeaders getHttpHeaders (String contentType, String filename ) {
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.setContentType(MediaType.parseMediaType(contentType));
        return headers;
    }
}
