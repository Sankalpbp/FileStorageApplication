package ai.typeface.filestorageservice.constants;

public final class InfoMessages {

    private InfoMessages () {}

    public static final String FILE_MUST_NOT_BE_EMPTY = "File not found or download failed.";

    public static final String CONVERTING_MULTIPART_FILE = "Converting Multipart file : {}";

    public static final String GCS_FETCH_SUCCESS = "Fetch successful from Google Cloud Storage";

    public static final String GCS_UPLOAD_SUCCESS = "Successfully uploaded file to Google Cloud Storage";

    public static final String GCS_DELETION_SUCCESS = "Successfully deleted file from Google Cloud Storage";

    public static final String GCS_UPDATE_SUCCESS = "Successfully updated file to Google Cloud Storage";

    public static final String FILE_UPLOAD_STARTED = "Started file uploading process on Google Cloud Storage.";

    public static final String FILE_DOWNLOAD_STARTED = "Started file downloading process on Google Cloud Storage.";

    public static final String FILE_DELETION_STARTED = "Started file deletion process on Google Cloud Storage.";

    public static final String FILE_UPDATE_STARTED = "Started file update process on Google Cloud Storage.";

    public static final String METADATA_DELETION_SUCCESS = "Successfully deleted metadata from database";


}
