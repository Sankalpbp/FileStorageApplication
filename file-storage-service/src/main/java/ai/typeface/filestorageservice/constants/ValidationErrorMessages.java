package ai.typeface.filestorageservice.constants;

public final class ValidationErrorMessages {

    private ValidationErrorMessages () {}

    public static final String EMPTY_FILE_ERROR = "File provided in the request is empty";

    public static final String FILE_AND_METADATA_NOT_NULL_TOGETHER = "Both file and metadata cannot be null together";

    public static final String FILE_MUST_NOT_BE_EMPTY = "If file metadata provided in the request body is null, file must not be empty!";

    public static final String FILE = "File";

    public static final String UPDATE_ERROR_MESSAGE = "cannot be updated without updating the file data.";

    public static final String EXISTING = "Existing";

    public static final String NEW = "New";
    public static final String SIZE = "size";

    public static final String FILE_URL = "fileURL";

    public static final String FILE_TYPE = "fileType";


}
