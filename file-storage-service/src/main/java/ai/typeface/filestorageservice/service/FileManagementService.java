package ai.typeface.filestorageservice.service;

import com.google.cloud.storage.Blob;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileManagementService {

    public String upload ( MultipartFile file );

    public Blob download ( String filename );

    public String delete ( String filename );

    public String update ( MultipartFile file, String filename );

    public List<String> getAllFiles ( );

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
