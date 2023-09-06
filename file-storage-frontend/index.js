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

let currentPage = 0;

createList ( FIRST_PAGE, "5", "createdAt", "desc" );

async function deleteListenerAction ( event ) {
    const button = event.target;
    const buttonRow = button.parentElement;
    const uniqueIdentifier = buttonRow.uniqueIdentifier;
    const apiUrl = `http://localhost:8081/files/${uniqueIdentifier}`;

    try {
        const response = await fetch ( apiUrl, {
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

        while ( fileListContainer.firstChild ) {
            fileListContainer.removeChild ( fileListContainer.firstChild );
        }

        createList ( FIRST_PAGE, "5", "createdAt", "desc" );

    } catch ( error ) {
        console.error ( `Fetch error: ${error}` );
    }
}

async function downloadListenerAction ( event ) {
    const button = event.target;
    const buttonRow = button.parentElement;
    const filename = buttonRow.filename;
    const uniqueIdentifier = buttonRow.uniqueIdentifier;
    const apiUrl = `http://localhost:8081/files/${uniqueIdentifier}`;

    try {
        const response = await fetch ( apiUrl );

        if ( response.status !== 200 ) {
            alert ( 'Download Failed!' );
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

async function updateFileListenerAction ( event ) {
}

uploadButton.addEventListener ( 'click', async event => {

    const fileInput = document.querySelector ( '.fileInput' );

    if ( fileInput.files.length == 0 ) {
        errorMessage.textContent = 'Please select a file!';
        errorMessage.classList = 'text-danger';
        return;
    } else {
        errorMessage.textContent = '';
    }

    const formData = new FormData ();
    formData.append ( 'file', fileInput.files [ 0 ] );

    await uploadFile ( formData );
    while ( fileListContainer.firstChild ) {
        fileListContainer.removeChild ( fileListContainer.firstChild );
    }
    await createList ( FIRST_PAGE, "5", "createdAt", "desc" );
});

nextButton.addEventListener ( 'click', event => {
    while ( fileListContainer.firstChild ) {
        fileListContainer.removeChild ( fileListContainer.firstChild );
    }
    ++currentPage;
    createList ( currentPage, "5", "createdAt", "desc" );
});

previousButton.addEventListener ( 'click', event => {
    while ( fileListContainer.firstChild ) {
        fileListContainer.removeChild ( fileListContainer.firstChild );
    }
    --currentPage;
    createList ( currentPage, '5', 'createdAt', 'desc' );
});

async function uploadFile(formData) {

    const apiUrl = 'http://localhost:8081/files/upload';

    try {
        const response = await fetch( apiUrl, {
            method: 'POST',
            body: formData
        });

        const data = await response.json();
    
        if ( response.status !== 201 ) {
            errorMessage.textContent = `Upload failed!\n${data.message}`;
            errorMessage.style.color = 'text-danger';
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

function createFileListItem ( filename, uniqueIdentifier ) {
    const row = document.createElement ( 'div' );
    row.classList = 'row mt-3';
    row.uniqueIdentifier = uniqueIdentifier;
    row.filename = filename;

    const filenameDiv = document.createElement ( 'div' );
    filenameDiv.classList = 'col-4 m-1 mt-4';
    filenameDiv.textContent = filename;

    const downloadButton = document.createElement ( 'div' );
    downloadButton.classList = 'col-1 m-1 btn btn-outline-info download-button';
    downloadButton.textContent = 'Download';
    downloadButton.addEventListener ( 'click', downloadListenerAction );

    const updateFileButton = document.createElement ( 'div' );
    updateFileButton.classList = 'col-1 m-1 btn btn-outline-success update-file-button';
    updateFileButton.textContent = 'Update File';

    const updateFileMetadataButton = document.createElement ( 'div' );
    updateFileMetadataButton.classList = 'col-1 m-1 btn btn-outline-warning update-file-metadata-button';
    updateFileMetadataButton.textContent = 'Update File Metadata';

    const deleteButton = document.createElement ( 'div' );
    deleteButton.classList = 'col-1 m-1 btn btn-outline-danger delete-button';
    deleteButton.textContent = 'Delete';
    deleteButton.addEventListener ( 'click', deleteListenerAction );

    row.appendChild ( filenameDiv );
    row.appendChild ( downloadButton );
    row.appendChild ( updateFileButton );
    row.appendChild ( updateFileMetadataButton );
    row.appendChild ( deleteButton );

    return row;
}

async function createList ( pageNumber, pageSize, sortBy, sortDir ) {
    const apiUrl = `http://localhost:8081/files?pageNumber=${pageNumber}&pageSize=${pageSize}&sortBy=${sortBy}&sortDir=${sortDir}`;



    let apiResponse = null;
    try {
        const response = await fetch ( apiUrl );

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

    apiResponse.content.forEach ( file => {
        fileListContainer.appendChild ( createFileListItem ( file.filename, file.uniqueIdentifier ) );
        const updateBox = document.createElement ( 'div' );
        updateBox.classList = 'row mt-2 update-box';
        fileListContainer.appendChild ( updateBox );
    });

    setTimeout ( () => {
        errorMessage.textContent = '';
        successMessage.textContent = '';
        infoMessage.textContent = '';
        fileFetchFailedMessage.textContent = '';
    }, 2000 );
}
