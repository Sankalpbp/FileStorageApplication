package ai.typeface.filestorageservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileManagementService {

    public String upload ( MultipartFile file,
                           String metadata );
}
