package ai.typeface.filestorageservice.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

public interface CloudStorageService {

    public String uploadFile (MultipartFile file, String fileName, String contentType );

}
