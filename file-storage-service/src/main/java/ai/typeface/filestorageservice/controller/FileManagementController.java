package ai.typeface.filestorageservice.controller;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.service.FileManagementService;
import com.google.cloud.storage.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping ( "/files" )
public class FileManagementController {

    /*
    TODO:
        1. Update all methods to return ResponseEntity<>
        2. Handle all the incoming arguments by the client ( validate the arguments )
    */

    private static final Logger LOGGER = LoggerFactory.getLogger ( FileManagementController.class );

    private final FileManagementService service;

    public FileManagementController ( FileManagementService service ) {
        this.service = service;
    }

    @PostMapping ( value = "/upload",
                   consumes = {
                        MediaType.MULTIPART_FORM_DATA_VALUE
                   } )
    public ResponseEntity<UUID> uploadFile ( @RequestParam ( "file" ) MultipartFile file ) {

        LOGGER.debug ( "Called files/upload API end point" );
        try {
            if (file.isEmpty()) {
                /* TODO: Throw a relevant exception */
                throw new RuntimeException ( "File provided is empty" );
            }
            return ResponseEntity.ok ( service.upload(file) );
        } catch ( Exception e ) {
            LOGGER.error ( e.getMessage () );
            return ResponseEntity.badRequest ().build ();
        }
    }

    @PutMapping ( "/{fileIdentifier}" )
    public ResponseEntity<FileMetadataDTO> updateFile ( @RequestParam ( value = "file", required = false ) MultipartFile file,
                                        @PathVariable ( "fileIdentifier" ) UUID fileIdentifier,
                                        @RequestBody ( required = false ) FileMetadataDTO metadata ) {

        LOGGER.debug ( "Called PUT files/{filename} API end point" );

        try {
            if ( file == null && metadata == null ) {
                throw new RuntimeException ( "Both file and metadata cannot be null together" );
            }
            if ( metadata == null && file.isEmpty () ) {
                /* TODO: Throw a relevant exception */
                throw new RuntimeException ( "No file found" );
            }

            if ( metadata != null ) {
                return ResponseEntity.ok ( service.updateMetadata ( metadata, fileIdentifier ) );
            }
            return ResponseEntity.ok ( service.updateFileData ( file, fileIdentifier ) );
        } catch ( Exception e ) {
            LOGGER.error ( e.getMessage () );
            return ResponseEntity.badRequest().build ();
        }
    }

    @DeleteMapping ( "/{fileIdentifier}" )
    public ResponseEntity<String> deleteFile ( @PathVariable ( "fileIdentifier" ) UUID fileIdentifier ) {
        LOGGER.debug ( "Called DELETE files/{filename} API end point" );

        try {
            return ResponseEntity.ok ( service.delete ( fileIdentifier ) );
        } catch ( Exception e ) {
            LOGGER.error ( "An error occurred while fetching the file corresponding to fileIdentifier: {}.", fileIdentifier );
            return ResponseEntity.badRequest ().build ();
        }
    }

    @GetMapping ( "/{fileIdentifier}" )
    public ResponseEntity<Resource> downloadFile( @PathVariable ( "fileIdentifier" ) UUID fileIdentifier ) {

        LOGGER.debug ( "Called GET files/{fileIdentifier} API end point" );

        Blob blob = service.download ( fileIdentifier );

        if (blob != null) {
            byte[] fileData = blob.getContent();
            ByteArrayResource resource = new ByteArrayResource(fileData);

            return ResponseEntity.ok()
                                 .headers( service.getHttpHeaders ( blob.getContentType (), blob.getName () ) )
                                 .contentType(MediaType.parseMediaType ( blob.getContentType() ) )
                                 .body(resource);
        }

        return ResponseEntity.badRequest().body ( new ByteArrayResource ( "File not found or download failed.".getBytes() ) );
    }

    @GetMapping
    public ResponseEntity<List<FileMetadataDTO>> getAllFiles ( ) {
        LOGGER.debug ( "Called GET /files API end point" );

        try {
            List<FileMetadataDTO> files = service.getAllFiles ();
            return ResponseEntity.ok ( files );
        } catch ( Exception e ) {
            LOGGER.error ( "An error occurred while fetching the files", e );
            return ResponseEntity.badRequest ().build ();
        }
    }

}
