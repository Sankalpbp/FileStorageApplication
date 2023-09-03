package ai.typeface.filestorageservice.controller;

import ai.typeface.filestorageservice.service.FileManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping ( "/files" )
public class FileManagementController {

    private final FileManagementService service;

    public FileManagementController ( FileManagementService service ) {
        this.service = service;
    }

    @PostMapping ( "/upload" )
    public String uploadFile ( @RequestParam ( "file" ) MultipartFile file,
                               @RequestParam ( "metadata" ) String metadata ) {
        if ( file.isEmpty () ) {
            return "No File found";
        }
        return service.upload ( file, metadata );
    }

    @GetMapping ( "/download" )
    public ResponseEntity<Resource> downloadFile (@RequestParam ( "filePath" ) String filePath, HttpServletRequest request ) {
        try {
            Resource resource = new UrlResource(Path.of(filePath).toUri());

            String contentType = request.getServletContext ().getMimeType ( resource.getFile().getAbsolutePath () );
            if ( contentType == null ) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType ( MediaType.parseMediaType (contentType ) )
                    .header ( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body ( resource );
        } catch ( IOException e ) {
            e.printStackTrace ();
            return ResponseEntity.badRequest ().build ();
        }
    }

}
