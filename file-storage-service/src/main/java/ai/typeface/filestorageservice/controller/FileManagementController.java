package ai.typeface.filestorageservice.controller;

import ai.typeface.filestorageservice.constants.ApiCalledMessages;
import ai.typeface.filestorageservice.constants.ApiConstants;
import ai.typeface.filestorageservice.constants.InfoMessages;
import ai.typeface.filestorageservice.constants.ValidationErrorMessages;
import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.dtos.FileMetadataPageResponse;
import ai.typeface.filestorageservice.service.FileManagementService;
import com.google.cloud.storage.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping ( "/files" )
public class FileManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger ( FileManagementController.class );

    @Value ( "${spring.servlet.multipart.max-request-size}" )
    private String maxAllowedFileSize;

    private final FileManagementService service;

    public FileManagementController ( FileManagementService service ) {
        this.service = service;
    }

    @PostMapping ( value = "/upload",
                   consumes = {
                        MediaType.MULTIPART_FORM_DATA_VALUE
                   } )
    public ResponseEntity<UUID> uploadFile ( @RequestParam ( "file" ) MultipartFile file ) {

        LOGGER.debug (ApiCalledMessages.UPLOAD_API_CALLED);
        if (file.isEmpty()) {
            LOGGER.error (ValidationErrorMessages.EMPTY_FILE_ERROR);
            throw new RuntimeException ( ValidationErrorMessages.EMPTY_FILE_ERROR );
        }
        if ( !isFileSizeWithinRange( file.getSize ()) ) {
            LOGGER.error ( ValidationErrorMessages.MAX_ALLOWED_FILE_SIZE_EXCEEDED );
            throw new MaxUploadSizeExceededException ( file.getSize () );
        }
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( service.upload ( file ) );
    }

    @PutMapping ( "/file/{fileIdentifier}" )
    public ResponseEntity<FileMetadataDTO> updateFile ( @RequestParam ( "file" ) MultipartFile file,
                                                        @PathVariable ( "fileIdentifier" ) UUID fileIdentifier ) {

        LOGGER.debug ( ApiCalledMessages.UPDATE_API_CALLED );

        if ( file.isEmpty () ) {
            LOGGER.error ( ValidationErrorMessages.FILE_MUST_NOT_BE_EMPTY );
            throw new RuntimeException ( ValidationErrorMessages.EMPTY_FILE_ERROR );
        }

        if ( !isFileSizeWithinRange( file.getSize ()) ) {
            LOGGER.error ( ValidationErrorMessages.MAX_ALLOWED_FILE_SIZE_EXCEEDED );
            throw new MaxUploadSizeExceededException ( file.getSize () );
        }

        return ResponseEntity.ok ( service.updateFileData ( file, fileIdentifier ) );
    }

    @PutMapping ( "/metadata/{fileIdentifier}" )
    public ResponseEntity<FileMetadataDTO> updateFile ( @PathVariable ( "fileIdentifier" ) UUID fileIdentifier,
                                                        @RequestBody FileMetadataDTO metadata ) {

        LOGGER.debug ( ApiCalledMessages.UPDATE_API_CALLED );

        if ( metadata == null ) {
            LOGGER.error ( ValidationErrorMessages.FILE_MUST_NOT_BE_EMPTY );
            throw new RuntimeException ( ValidationErrorMessages.EMPTY_FILE_ERROR );
        }

        return ResponseEntity.ok ( service.updateMetadata ( metadata, fileIdentifier ) );
    }

    @DeleteMapping ( "/{fileIdentifier}" )
    public ResponseEntity<String> deleteFile ( @PathVariable ( "fileIdentifier" ) UUID fileIdentifier ) {
        LOGGER.debug ( ApiCalledMessages.DELETE_API_CALLED );
        return ResponseEntity.ok ( service.delete ( fileIdentifier ) );
    }

    @GetMapping ( "/{fileIdentifier}" )
    public ResponseEntity<Resource> downloadFile( @PathVariable ( "fileIdentifier" ) UUID fileIdentifier ) {

        LOGGER.debug ( ApiCalledMessages.GET_API_FOR_SINGLE_FILE_CALLED );

        Blob blob = service.download ( fileIdentifier );

        if ( blob == null ) {
            LOGGER.error (InfoMessages.FILE_MUST_NOT_BE_EMPTY);
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
            @RequestParam ( value = "pageNumber", defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER, required = false ) int pageNumber,
            @RequestParam ( value = "pageSize", defaultValue = ApiConstants.DEFAULT_PAGE_SIZE, required = false ) int pageSize,
            @RequestParam ( value = "sortBy", defaultValue = ApiConstants.DEFAULT_SORT_BY_FIELD, required = false ) String sortBy,
            @RequestParam ( value = "sortDir", defaultValue = ApiConstants.DEFAULT_SORT_DIRECTION, required = false ) String sortDir
    ) {
        LOGGER.debug ( "Called GET /files API end point" );

        FileMetadataPageResponse files = service.getAllFiles ( pageNumber, pageSize, sortBy, sortDir );
        if ( files == null || files.getTotalElements() == 0 ) {
            return ResponseEntity.status ( HttpStatus.NO_CONTENT ).build();
        }
        return ResponseEntity.ok ( files );
    }

    private boolean isFileSizeWithinRange ( long bytes ) {
        long allowedFileSize = Long.parseLong ( maxAllowedFileSize.substring ( 0, maxAllowedFileSize.length () - 2 ) );
        long uploadedFileSize = Math.round ( (float) bytes / ( 1024 * 1024 ) );
        return allowedFileSize > uploadedFileSize;
    }
}
