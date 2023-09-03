package ai.typeface.filestorageservice.controller;

import ai.typeface.filestorageservice.service.FileManagementService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

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
    public ResponseEntity<Resource> downloadFile (@RequestParam ( "filePath" ) String filePath ) {
        try {
            Resource resource = new UrlResource(Path.of(filePath).toUri());

            // Check if the file exists
            if (resource.exists() && resource.isReadable()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());
                headers.setContentType ( MediaType.APPLICATION_OCTET_STREAM );

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch ( IOException e ) {
            e.printStackTrace ();
            return ResponseEntity.badRequest ().build ();
        }
    }

}
