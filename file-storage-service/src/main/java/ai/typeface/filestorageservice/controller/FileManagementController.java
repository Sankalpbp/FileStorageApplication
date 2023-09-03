package ai.typeface.filestorageservice.controller;

import ai.typeface.filestorageservice.service.FileManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


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
    public String uploadFile ( @RequestParam ( "file" ) MultipartFile file ) {

        LOGGER.debug ( "Called /upload API end point" );

        if ( file.isEmpty () ) {
            /* TODO: Throw a relevant exception */
            return "No File found";
        }
        return service.upload ( file );
    }

}
