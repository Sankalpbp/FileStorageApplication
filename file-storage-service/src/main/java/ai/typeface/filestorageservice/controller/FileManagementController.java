package ai.typeface.filestorageservice.controller;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.dtos.FileMetadataPageResponse;
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
        TODO: Handle all the incoming arguments by the client ( validate the arguments )
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
        if (file.isEmpty()) {
            LOGGER.error ( "File provided is empty" );
            throw new RuntimeException ( "File provided is empty" );
        }
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( service.upload ( file ) );
    }

    @PutMapping ( "/{fileIdentifier}" )
    public ResponseEntity<FileMetadataDTO> updateFile ( @RequestParam ( value = "file", required = false ) MultipartFile file,
                                        @PathVariable ( "fileIdentifier" ) UUID fileIdentifier,
                                        @RequestBody ( required = false ) FileMetadataDTO metadata ) {

        LOGGER.debug ( "Called PUT files/{filename} API end point" );

        if ( file == null && metadata == null ) {
            throw new RuntimeException ( "Both file and metadata cannot be null together" );
        }
        if ( metadata == null && file.isEmpty () ) {
            LOGGER.error ( "If file metadata provided in the request body is null, file must not be empty!" );
            throw new RuntimeException ( "No file found" );
        }

        if ( metadata != null ) {
            return ResponseEntity.ok ( service.updateMetadata ( metadata, fileIdentifier ) );
        }
        return ResponseEntity.ok ( service.updateFileData ( file, fileIdentifier ) );
    }

    @DeleteMapping ( "/{fileIdentifier}" )
    public ResponseEntity<String> deleteFile ( @PathVariable ( "fileIdentifier" ) UUID fileIdentifier ) {
        LOGGER.debug ( "Called DELETE files/{filename} API end point" );
        return ResponseEntity.ok ( service.delete ( fileIdentifier ) );
    }

    @GetMapping ( "/{fileIdentifier}" )
    public ResponseEntity<Resource> downloadFile( @PathVariable ( "fileIdentifier" ) UUID fileIdentifier ) {

        LOGGER.debug ( "Called GET files/{fileIdentifier} API end point" );

        Blob blob = service.download ( fileIdentifier );

        if ( blob == null ) {
            LOGGER.info ( "File not found or download failed." );
            return ResponseEntity.status ( HttpStatus.NO_CONTENT ).build ();
        }

        byte[] fileData = blob.getContent();
        ByteArrayResource resource = new ByteArrayResource(fileData);

        return ResponseEntity.ok()
                             .headers( service.getHttpHeaders ( blob.getContentType (), blob.getName () ) )
                             .contentType(MediaType.parseMediaType ( blob.getContentType() ) )
                             .body(resource);
    }

    @GetMapping
    public ResponseEntity<FileMetadataPageResponse> getAllFiles (
            @RequestParam ( value = "pageNumber", defaultValue = "0", required = false ) int pageNumber,
            @RequestParam ( value = "pageSize", defaultValue = "5", required = false ) int pageSize,
            @RequestParam ( value = "sortBy", defaultValue = "uniqueIdentifier", required = false ) String sortBy,
            @RequestParam ( value = "sortDir", defaultValue = "asc", required = false ) String sortDir
    ) {
        LOGGER.debug ( "Called GET /files API end point" );

        FileMetadataPageResponse files = service.getAllFiles ( pageNumber, pageSize, sortBy, sortDir );
        if ( files == null || files.getTotalElements() == 0 ) {
            return ResponseEntity.status ( HttpStatus.NO_CONTENT ).build();
        }
        return ResponseEntity.ok ( files );
    }

}
