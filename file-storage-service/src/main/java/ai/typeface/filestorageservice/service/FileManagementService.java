package ai.typeface.filestorageservice.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

public interface FileManagementService {

    public String upload ( MultipartFile file );
}
