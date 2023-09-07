'use strict';

const uploadButton = document.querySelector ( '.upload' );
const errorMessage = document.querySelector ( '.error-message' );
const successMessage = document.querySelector ( '.success-message' );
const infoMessage = document.querySelector ( '.info-message' );
const fileListContainer = document.querySelector ( '.file-list-container' );
const fileFetchFailedMessage = document.querySelector ( '.file-fetch-failed' );
const previousButton = document.querySelector ( '.previous' );
const nextButton = document.querySelector ( '.next' );
const pageNumberContainer = document.querySelector ( '.page-number' );

const FIRST_PAGE = 0;
const PAGE_SIZE = 5;
const SORT_BY_FIELD = 'createdAt';
const SORT_BY_DIRECTION = 'desc';

const HOSTNAME = 'localhost';
const HTTP = 'http'
const PORT = '8081';
const FILES = 'files';
const METADATA = 'metadata';
const FILE = 'file';

const URL_PREFIX = `${HTTP}://${HOSTNAME}:${PORT}/${FILES}`;

let currentPage = 0;

createList ( FIRST_PAGE, PAGE_SIZE, SORT_BY_FIELD, SORT_BY_DIRECTION );

function url ( uniqueIdentifier, isMetadataBeingUpdated ) {
    let apiUrl = URL_PREFIX;
    if ( !uniqueIdentifier ) {
        return apiUrl;
    }
    if ( isMetadataBeingUpdated !== undefined ) {
        apiUrl += '/';
        apiUrl += isMetadataBeingUpdated ? METADATA
                                         : FILE;
    }
    apiUrl += '/' + uniqueIdentifier;
    return apiUrl;
}

async function deleteListenerAction ( event ) {
    const button = event.target;
    const buttonRow = button.parentElement;
    const uniqueIdentifier = buttonRow.uniqueIdentifier;
    const DELETE_URL = url ( uniqueIdentifier );

    try {
        const response = await fetch ( DELETE_URL, {
            method: 'DELETE'
        });

        if ( response.status != 200 ) {
            infoMessage.textContent =  'File delete operation failed!';
            infoMessage.classList = 'text-danger';
            return;
        } else {
            infoMessage.textContent = 'File successfully deleted ';
            infoMessage.classList = 'text-success';
        }

        createList ( FIRST_PAGE, PAGE_SIZE, SORT_BY_FIELD, SORT_BY_DIRECTION );

    } catch ( error ) {
        console.error ( `Fetch error: ${error}` );
    }
}

async function downloadListenerAction ( event ) {
    const button = event.target;
    const buttonRow = button.parentElement;
    const filename = buttonRow.filename;
    const uniqueIdentifier = buttonRow.uniqueIdentifier;
    const GET_URL = url ( uniqueIdentifier );

    try {
        const response = await fetch ( GET_URL );

        if ( response.status !== 200 ) {
            errorMessage.textContent = 'Download Failed!';
            return;
        }

        const blobData = await response.blob();

        const anchor = document.createElement('a');
        anchor.href = URL.createObjectURL(blobData);

        anchor.download = filename;
        anchor.click();
        URL.revokeObjectURL(anchor.href);

    } catch ( error ) {
        console.error ( `Fetch error: ${error}` );
    }
}

async function updateFileData ( uniqueIdentifier ) {
    const fileInput = document.querySelector ( `.fileDataUpdateInput-${uniqueIdentifier}` );
    const updateBox = document.querySelector ( `.update-box-${uniqueIdentifier}` );

    const errorDiv = document.createElement ( 'div' );
    errorDiv.classList = `text-danger error-${uniqueIdentifier}`;
    updateBox.appendChild ( errorDiv );

    if ( fileInput.files.length == 0 ) {
        errorDiv.textContent = 'Please select a file!';
        removeMessage ( errorDiv );
        return;
    } else {
        errorDiv.textContent = '';
    }

    if ( ( fileInput.files [ 0 ].size / ( 1024 * 1024 ) ) > 50 ) {
        errorDiv.textContent = 'Please upload a file with size less than 50MB';
        removeMessage ( errorMessage );
        return;
    }

    const formData = new FormData ();
    formData.append ( 'file', fileInput.files [ 0 ] );

    const successful = await updateFile ( uniqueIdentifier, formData );
    if ( successful ) {
        createList ( FIRST_PAGE, PAGE_SIZE, SORT_BY_FIELD, SORT_BY_DIRECTION );
    }
}

async function updateFileMetadata ( uniqueIdentifier, metadata ) {
    const filename = metadata.filename;
    const updateBox = document.querySelector ( `.update-box-${uniqueIdentifier}` );

    const errorDiv = document.createElement ( 'div' );
    errorDiv.classList = `text-danger error-${uniqueIdentifier}`;
    updateBox.appendChild ( errorDiv );

    if ( !filename ) {
        errorDiv.textContent = 'Please provide an updated filename!';
        removeMessage ( errorDiv );
        return;
    } else {
        errorDiv.textContent = '';
    }

    const successful = await updateMetadata ( uniqueIdentifier, metadata );
    if ( successful ) {
        createList ( currentPage, PAGE_SIZE, SORT_BY_FIELD, SORT_BY_DIRECTION );
    }
}

function updateFileMetadataListenerAction ( event ) {
    const button = event.target;
    const buttonRow = button.parentElement;
    const uniqueIdentifier = buttonRow.uniqueIdentifier;
    const updateBox = document.querySelector ( `.update-box-${uniqueIdentifier}` );
    updateBox.classList.add ( `file-metadata-update-${uniqueIdentifier}` );
    
    button.disabled = true;

    const form = document.createElement ( 'div' );
    form.classList = `form col-8 update-metadata-form file-metadata-update-${uniqueIdentifier}`;

    const label = document.createElement ( 'label' );
    label.classList = 'form-label';
    label.textContent = 'Enter updated file name: ';

    form.appendChild ( label );

    const input = document.createElement ( 'input' );
    input.classList = `form-control fileMetadataUpdateInput-${uniqueIdentifier}`;
    input.type = 'text';

    form.appendChild ( input );

    const updateButton = document.createElement ( 'button' );
    updateButton.classList = `btn btn-warning col-1 m-3 mb-0 file-metadata-update-${uniqueIdentifier}`;
    updateButton.textContent = 'Update';
    updateButton.addEventListener ( 'click', async event => {
        await updateFileMetadata ( uniqueIdentifier, { filename: input.value } );
    } );

    const closeButton = document.createElement ( 'button' );
    closeButton.classList = `btn btn-secondary col-1 m-3 mb-0 close-${uniqueIdentifier} file-metadata-update-${uniqueIdentifier}`;
    closeButton.textContent = 'Close';
    closeButton.addEventListener ( 'click', event => {
        const childrenToRemove = updateBox.querySelectorAll ( `.file-metadata-update-${uniqueIdentifier}` );
        Array.from ( childrenToRemove ).forEach ( child => {
            updateBox.removeChild ( child );
        });
        button.disabled = false;
    });
    
    updateBox.appendChild ( form );
    updateBox.appendChild ( updateButton );
    updateBox.appendChild ( closeButton );
}

function updateFileDataListenerAction ( event ) {
    const button = event.target;
    const buttonRow = button.parentElement;
    const uniqueIdentifier = buttonRow.uniqueIdentifier;
    const updateBox = document.querySelector ( `.update-box-${uniqueIdentifier}` );

    button.disabled = true;

    const form = document.createElement ( 'div' );
    form.classList = `form col-8 update-file-form file-data-update-${uniqueIdentifier}`;

    const label = document.createElement ( 'label' );
    label.classList = 'form-label';
    label.textContent = 'Click on Choose File button to browse files from the file system';

    form.appendChild ( label );

    const input = document.createElement ( 'input' );
    input.classList = `form-control fileDataUpdateInput-${uniqueIdentifier}`;
    input.type = 'file';

    form.appendChild ( input );

    const updateButton = document.createElement ( 'button' );
    updateButton.classList = `btn btn-warning col-1 m-3 mb-0 file-data-update-${uniqueIdentifier}`;
    updateButton.textContent = 'Update';
    updateButton.addEventListener ( 'click', async event => {
        await updateFileData ( uniqueIdentifier );
    } );

    const closeButton = document.createElement ( 'button' );
    closeButton.classList = `btn btn-secondary col-1 m-3 mb-0 close-${uniqueIdentifier} file-data-update-${uniqueIdentifier}`;
    closeButton.textContent = 'Close';
    closeButton.addEventListener ( 'click', event => {
        const childrenToRemove = updateBox.querySelectorAll ( `.file-data-update-${uniqueIdentifier}` );
        Array.from ( childrenToRemove ).forEach ( child => {
            updateBox.removeChild ( child );
        });
        button.disabled = false;
    });
    
    updateBox.appendChild ( form );
    updateBox.appendChild ( updateButton );
    updateBox.appendChild ( closeButton );
}

uploadButton.addEventListener ( 'click', async event => {

    const fileInput = document.querySelector ( '.fileInput' );

    if ( fileInput.files.length == 0 ) {
        errorMessage.textContent = 'Please select a file!';
        errorMessage.classList = 'text-danger';
        removeMessage ( errorMessage );
        return;
    } else {
        errorMessage.textContent = '';
    }

    if ( ( fileInput.files [ 0 ].size / ( 1024 * 1024 ) ) > 50 ) {
        errorMessage.textContent = 'Please upload a file with size less than 50MB';
        errorMessage.classList = 'text-danger';
        removeMessage ( errorMessage );
        return;
    }

    const formData = new FormData ();
    formData.append ( 'file', fileInput.files [ 0 ] );

    await uploadFile ( formData );
    createList ( FIRST_PAGE, PAGE_SIZE, SORT_BY_FIELD, SORT_BY_DIRECTION );
});

nextButton.addEventListener ( 'click', event => {
    ++currentPage;
    createList ( currentPage, PAGE_SIZE, SORT_BY_FIELD, SORT_BY_DIRECTION );
});

previousButton.addEventListener ( 'click', event => {
    --currentPage;
    createList ( currentPage, PAGE_SIZE, SORT_BY_FIELD, SORT_BY_DIRECTION );
});

async function updateMetadata ( uniqueIdentifier, metadata ) {
    const PUT_URL = url ( uniqueIdentifier, true );

    try {
        const response = await fetch ( PUT_URL, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify ( metadata )
        });

        const data = await response.json ();

        if ( response.status != 200 ) {
            const errorDiv = document.querySelector ( `.error-${uniqueIdentifier}` );
            errorDiv.textContent = data.message;
            removeMessage ( errorDiv );
            return false;
        }

        successMessage.textContent = 'File metadata has been saved successfully!';
        successMessage.classList = 'text-success';
        console.log('Response from server:', data);
        return true;
    } catch ( error ) {
        console.error ( 'Error: ', error );
    }
}

async function updateFile ( uniqueIdentifier, formData ) {
    const PUT_URL = url ( uniqueIdentifier, false );

    try {
        const response = await fetch ( PUT_URL, {
            method: 'PUT',
            body: formData,
        });

        const data = await response.json ();

        if ( response.status !== 200 ) {
            const errorDiv = document.querySelector ( `.error-${uniqueIdentifier}` );
            errorDiv.textContent = data.message;
            removeMessage ( errorDiv );
            return false;
        }
        successMessage.textContent = `Updated file has been saved successfully!`;
        successMessage.classList = 'text-success';
        console.log('Response from server:', data);
        return true;
    } catch ( error ) {
        console.error ( 'Error: ', error );
    }
}

async function uploadFile(formData) {
    const POST_URL = `${url ()}/upload`;

    try {
        const response = await fetch( POST_URL, {
            method: 'POST',
            body: formData
        });

        const data = await response.json();
    
        if ( response.status !== 201 ) {
            errorMessage.textContent = `Upload failed!\n${data.message}`;
            errorMessage.classList = 'text-danger';
            return;
        } else {
            errorMessage.textContent = '';
        }
    
        successMessage.textContent = `Uploaded file has been saved successfully!`;
        successMessage.classList = 'text-success';
        console.log('Response from server:', data);
    } catch (error) {
        console.error('Error:', error);
    }
}

function createFileListItem ( file, uniqueIdentifier ) {
    const row = document.createElement ( 'div' );
    row.classList = 'row mt-3';
    row.uniqueIdentifier = uniqueIdentifier;
    row.filename = file.filename;

    const filenameDiv = document.createElement ( 'div' );
    filenameDiv.classList = 'col-4 m-1 mt-4';
    filenameDiv.textContent = file.filename;

    const sizeDiv = document.createElement ( 'div' );
    sizeDiv.classList = 'col-1 m-1 mt-4';
    sizeDiv.textContent = Math.round ( file.size / 1000 ) + ' KB';

    const downloadButton = document.createElement ( 'button' );
    downloadButton.classList = 'col-1 m-1 btn btn-outline-info download-button';
    downloadButton.textContent = 'Download';
    downloadButton.addEventListener ( 'click', downloadListenerAction );

    const updateFileDataButton = document.createElement ( 'button' );
    updateFileDataButton.classList = 'col-1 m-1 btn btn-outline-success update-file-button';
    updateFileDataButton.textContent = 'Update File';
    updateFileDataButton.addEventListener ( 'click', updateFileDataListenerAction );

    const updateFileMetadataButton = document.createElement ( 'button' );
    updateFileMetadataButton.classList = 'col-1 m-1 btn btn-outline-warning update-file-metadata-button';
    updateFileMetadataButton.textContent = 'Update File Metadata';
    updateFileMetadataButton.addEventListener ( 'click', updateFileMetadataListenerAction );

    const deleteButton = document.createElement ( 'button' );
    deleteButton.classList = 'col-1 m-1 btn btn-outline-danger delete-button';
    deleteButton.textContent = 'Delete';
    deleteButton.addEventListener ( 'click', deleteListenerAction );

    row.appendChild ( filenameDiv );
    row.appendChild ( sizeDiv );
    row.appendChild ( downloadButton );
    row.appendChild ( updateFileDataButton );
    row.appendChild ( updateFileMetadataButton );
    row.appendChild ( deleteButton );

    return row;
}

async function createList ( pageNumber, pageSize, sortBy, sortDir ) {

    while ( fileListContainer.firstChild ) {
        fileListContainer.removeChild ( fileListContainer.firstChild );
    }

    const GET_URL = `${url ( )}?pageNumber=${pageNumber}&pageSize=${pageSize}&sortBy=${sortBy}&sortDir=${sortDir}`;

    let apiResponse = null;
    try {
        const response = await fetch ( GET_URL );

        if ( response.status === 200 ) {
            apiResponse = await response.json ();
        } else if ( response.status === 204 ) {
            fileFetchFailedMessage.textContent = 'No files found';
            return;
        } else {
            fileFetchFailedMessage.textContent = 'Fetching files from server failed!';
            throw new Error ( `HTTP error! Status: ${response.status}` );
        }
        fileFetchFailedMessage.textContent = '';

    } catch ( error ) {
        console.error ( `Fetch error: ${error}` );
    }

    pageNumberContainer.textContent = apiResponse.pageNumber + 1;

    previousButton.disabled = ( apiResponse.pageNumber === 0 );
    nextButton.disabled = ( apiResponse.last );

    const legend = document.createElement ( 'div' );
    legend.classList = 'row mt-3';

    const filenameDiv = document.createElement ( 'div' );
    filenameDiv.classList = 'col-4 m-1 mt-4 h5';
    filenameDiv.textContent = 'File Name';

    const sizeDiv = document.createElement ( 'div' );
    sizeDiv.classList = 'col-1 m-1 mt-4 h5';
    sizeDiv.textContent = 'Size';

    legend.appendChild ( filenameDiv );
    legend.appendChild ( sizeDiv );

    fileListContainer.appendChild ( legend );

    apiResponse.content.forEach ( file => {
        fileListContainer.appendChild ( createFileListItem ( file, file.uniqueIdentifier ) );
        const updateBox = document.createElement ( 'div' );
        updateBox.classList = `row mt-2 mb-5 update-box-${file.uniqueIdentifier}`;
        fileListContainer.appendChild ( updateBox );
    });

    removeMessages ();
}

function removeMessages ( ) {
    removeMessage ( errorMessage );
    removeMessage ( successMessage );
    removeMessage ( infoMessage );
    removeMessage ( fileFetchFailedMessage );
}

function removeMessage ( element ) {
    if ( !element ) {
        return;
    }
    setTimeout ( () => {
        element.textContent = '';
    }, 2000 );
}