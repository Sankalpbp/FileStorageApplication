package ai.typeface.filestorageservice.controller;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.entity.FileMetadata;
import ai.typeface.filestorageservice.service.FileManagementService;
import com.google.cloud.storage.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping ( "/files" )
public class FileManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger ( FileManagementController.class );

    private final FileManagementService service;

    public FileManagementController ( FileManagementService service ) {
        this.service = service;
    }

    @PostMapping ( value = "/upload",
                   consumes = {
                        MediaType.MULTIPART_FORM_DATA_VALUE
                   } )
    public UUID uploadFile ( @RequestParam ( "file" ) MultipartFile file ) {

        LOGGER.debug ( "Called files/upload API end point" );
        if ( file.isEmpty () ) {
            /* TODO: Throw a relevant exception */
            LOGGER.error ( "File provided is empty." );
            return null;
        }

        return service.upload ( file );
    }

    @PutMapping ( "/{filename}" )
    public String updateFile ( @RequestParam ( "file" ) MultipartFile file, @PathVariable ( "filename" ) String filename ) {

        LOGGER.debug ( "Called PUT files/{filename} API end point" );
        if ( file.isEmpty () ) {
            /* TODO: Throw a relevant exception */
            return "No file found";
        }
        return service.update ( file, filename );
    }

    @DeleteMapping ( "/{filename}" )
    public String deleteFile ( @PathVariable ( "filename" ) String filename ) {
        LOGGER.debug ( "Called DELETE files/{filename} API end point" );
        return service.delete ( filename );
    }

    @GetMapping ( "/{filename}" )
    public ResponseEntity<Resource> downloadFile( @PathVariable ( "filename" ) String filename) {

        LOGGER.debug ( "Called GET files/{filename} API end point" );

        Blob blob = service.download ( filename );

        if (blob != null) {
            byte[] fileData = blob.getContent();
            ByteArrayResource resource = new ByteArrayResource(fileData);

            return ResponseEntity.ok()
                    .headers( service.getHttpHeaders ( blob.getContentType (), filename ) )
                    .contentType(MediaType.parseMediaType ( blob.getContentType() ) )
                    .body(resource);
        }

        // Return ResponseEntity with an error message
        return ResponseEntity.badRequest().body(new ByteArrayResource("File not found or download failed.".getBytes()));
    }

    @GetMapping
    public List<String> getAllFiles ( ) {
        LOGGER.debug ( "Called GET /files API end point" );

        return service.getAllFiles ( );
    }


}
