package ai.typeface.filestorageservice.constants;

public final class ApiArgumentsAndMappings {

    private ApiArgumentsAndMappings () {}

    public static final String UPLOAD_PATH = Symbols.SLASH + "upload";

    public static final String CONTROLLER_PATH = Symbols.SLASH + "files";

    public static final String FILE = "file";

    public static final String IDENTIFIER = "Identifier";

    public static final String FILE_IDENTIFIER_VARIABLE_PATH = Symbols.SLASH + Symbols.OPENING_CURLY_BRACE + FILE + IDENTIFIER + Symbols.CLOSING_CURLY_BRACE;
}
