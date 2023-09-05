package ai.typeface.filestorageservice.dtos;

import lombok.Getter;

import java.util.List;

@Getter
public class FileMetadataPageResponse {

    private List<FileMetadataDTO> content;

    private int pageNumber;

    private int pageSize;

    private long totalElements;

    private long totalPages;

    private boolean last;

    private FileMetadataPageResponse () {

    }

    public static Builder builder () {
        return new Builder ();
    }

    public static class Builder {
        private List<FileMetadataDTO> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private long totalPages;
        private boolean last;

        private Builder () {

        }

        public Builder content ( List<FileMetadataDTO> content ) {
            this.content = content;
            return this;
        }

        public Builder pageNumber ( int pageNumber ) {
            this.pageNumber = pageNumber;
            return this;
        }

        public Builder pageSize ( int pageSize ) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder totalElements ( long totalElements ) {
            this.totalElements = totalElements;
            return this;
        }

        public Builder totalPages ( int totalPages ) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder last ( boolean last ) {
            this.last = last;
            return this;
        }

        public FileMetadataPageResponse build () {
            FileMetadataPageResponse pageResponse = new FileMetadataPageResponse();
            pageResponse.content = content;
            pageResponse.pageNumber = pageNumber;
            pageResponse.pageSize = pageSize;
            pageResponse.totalElements = totalElements;
            pageResponse.totalPages = totalPages;
            pageResponse.last = last;

            return pageResponse;
        }

    }
}
