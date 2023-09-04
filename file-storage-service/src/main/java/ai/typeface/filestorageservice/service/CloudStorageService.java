package ai.typeface.filestorageservice.service;

import com.google.cloud.storage.Blob;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudStorageService {

    public String uploadFile (MultipartFile file, String fileName, String contentType );

    public Blob downloadFile ( String filename );

    public String updateFile ( MultipartFile file, String filename, String contentType );

    public String deleteFile ( String filename );

    public List<String> getAllFiles ( );

}
