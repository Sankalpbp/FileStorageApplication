package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.dtos.FileMetadataPageResponse;
import ai.typeface.filestorageservice.entity.FileMetadata;
import ai.typeface.filestorageservice.exception.GoogleCloudStorageException;
import ai.typeface.filestorageservice.service.CloudStorageService;
import ai.typeface.filestorageservice.service.FileManagementService;
import ai.typeface.filestorageservice.service.FileMetadataService;
import ai.typeface.filestorageservice.util.CloudStorageUtil;
import ai.typeface.filestorageservice.util.FileMetadataUtil;
import com.google.cloud.storage.Blob;
import io.micrometer.common.util.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class FileManagementServiceImpl implements FileManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger ( FileManagementServiceImpl.class );

    private final CloudStorageService cloudStorageService;

    private final FileMetadataService fileMetadataService;

    public FileManagementServiceImpl ( CloudStorageService cloudStorageService,
                                       FileMetadataService fileMetadataService ) {
        this.cloudStorageService = cloudStorageService;
        this.fileMetadataService = fileMetadataService;
    }

    @Override
    @Transactional
    public FileMetadataDTO updateMetadata ( FileMetadataDTO metadataDTO, UUID fileIdentifier ) {

        FileMetadataDTO existingMetadata = fileMetadataService.findByUniqueIdentifier ( fileIdentifier );
        String contentType = CloudStorageUtil.getContentType ( existingMetadata.getFilename () );

        FileMetadataUtil.validateUpdateMetadataWithExistingMetadata ( metadataDTO, existingMetadata );

        String existingFilename = existingMetadata.getFilename();
        String newFilename = metadataDTO.getFilename();
        String fileURL = existingMetadata.getFileURL();

        if ( !existingFilename.equals ( newFilename ) ) {
            fileURL = cloudStorageService.updateFilename ( existingFilename, newFilename, contentType );
        }

        if ( fileURL == null || fileURL.isBlank () ) {
            LOGGER.error ( "An error occurred while saving the file in Google Cloud Storage." );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while saving the file in Google Cloud Storage." );
        }

        metadataDTO.setFileURL ( fileURL );

        return fileMetadataService.updateFileMetadata ( metadataDTO, fileIdentifier );
    }

    @Override
    public FileMetadataDTO updateFileData ( MultipartFile file, UUID fileIdentifier ) {
        String originalFileName = CloudStorageUtil.checkAndReturnOriginalFileName ( file );
        FileMetadataDTO metadata = fileMetadataService.findByUniqueIdentifier ( fileIdentifier );

        String contentType = CloudStorageUtil.getContentType ( originalFileName );
        String fileURL = cloudStorageService.updateFile ( file, metadata.getFilename (), contentType );

        if ( fileURL == null || fileURL.isBlank () ) {
            LOGGER.error ( "An error occurred while saving the file in Google Cloud Storage." );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while saving the file in Google Cloud Storage." );
        }

        metadata.setFileURL ( fileURL );

        try {
            return fileMetadataService.updateFileData ( FileMetadataUtil.createUpdatedFileMetadataDTO ( metadata.getFilename (),
                                                                                                        fileIdentifier,
                                                                                                        file.getBytes ().length,
                                                                                                        fileURL,
                                                                                                        metadata.getCreatedAt() ) );
        } catch ( IOException e ) {
            LOGGER.error ( "An error occurred while saving the metadata to the database" + e.getMessage () );
            throw new RuntimeException ( "An error occurred while saving the metadata to the database" + e.getMessage () );
        }
    }

    @Override
    @Transactional
    public String delete ( UUID fileIdentifier ) {
        String filename = getFilenameFromMetadata ( fileIdentifier );
        LOGGER.info ( fileMetadataService.deleteByFilename ( filename ) );
        return cloudStorageService.deleteFile ( filename );
    }

    @Override
    public FileMetadataPageResponse getAllFiles (int pageNumber,
                                                 int pageSize,
                                                 String sortBy,
                                                 String sortDir ) {
        return fileMetadataService.getAll ( pageNumber, pageSize, sortBy, sortDir );
    }

    @Override
    public Blob download ( UUID fileIdentifier ) {
        return cloudStorageService.downloadFile ( getFilenameFromMetadata ( fileIdentifier ) );
    }

    @Override
    @Transactional
    public UUID upload ( MultipartFile file ) {

        LOGGER.debug ( "Started file upload operation." );

        // Storing file in Google Cloud Storage
        String originalFileName = CloudStorageUtil.checkAndReturnOriginalFileName ( file );

        final UUID uniqueIdentifier = UUID.nameUUIDFromBytes ( originalFileName.getBytes () );
        String fileURL;

        fileURL = cloudStorageService.uploadFile( file, originalFileName, CloudStorageUtil.getContentType ( originalFileName ) );

        if ( fileURL == null || fileURL.isBlank () ) {
            LOGGER.error ( "An error occurred while saving the file in Google Cloud Storage." );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while saving the file in Google Cloud Storage." );
        }

        LOGGER.info ( "File uploaded successfully, file name: {} and url: {}", originalFileName, fileURL );

        // Storing metadata in MySQL Database

        try {
            FileMetadataDTO savedMetadata = fileMetadataService.save (
                                                    FileMetadataUtil.createFileMetadataDTO(originalFileName,
                                                                                           uniqueIdentifier,
                                                                                           file.getBytes().length,
                                                                                           fileURL));
            return savedMetadata.getUniqueIdentifier ();
        } catch ( IOException e) {
            LOGGER.error ( "An error occurred while saving the metadata to the database" + e.getMessage () );
            throw new RuntimeException ( "An error occurred while saving the metadata to the database" + e.getMessage () );
        }

    }

    private String getFilenameFromMetadata ( UUID fileIdentifier ) {
        FileMetadataDTO metadata = fileMetadataService.findByUniqueIdentifier ( fileIdentifier );
        return metadata.getFilename();
    }

}
