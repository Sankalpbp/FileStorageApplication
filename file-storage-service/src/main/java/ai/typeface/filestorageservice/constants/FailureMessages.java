package ai.typeface.filestorageservice.constants;

public final class FailureMessages {

    private FailureMessages () {}

    public static final String ERROR_READING_CONTENT_TYPE = "An error occurred while reading the content type of the provided file to upload / update.";

    public static final String ERROR_READING_GCS_CONFIGURATION_FILE = "An error occurred while reading the Google Cloud Storage Configurations file. Please check if the file exists and is accessible. Error: ";

    public static final String ERROR_WRITING_TO_FILE = "An error occurred while writing data to the file. Error: ";

    public static final String NO_FILENAME_FOR_GIVEN_FILE = "No file found for provided filename: ";

    public static final String GCS_UPLOAD_FAILED = "File upload failed on Google Cloud Storage.";

    public static final String GCS_UPDATE_FAILED = "File update operation failed on Google Cloud Storage. An error occurred while saving the file";

    public static final String ERROR_READING_PROVIDED_FILE = "An error occurred while reading provided file. Error: ";

    public static final String GCS_FETCH_FAILED = "Fetch failed from Google Cloud Storage";

    public static final String GCS_DELETION_FAILED = "Deletion failed from Google Cloud Storage";

    public static final String METADATA_DELETION_FAILED = "File Delete operation failed!";

    public static final String ERROR_SAVING_METADATA = "An error occurred while saving the metadata to the database";

}
