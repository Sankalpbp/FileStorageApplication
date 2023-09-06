'use strict';

const uploadButton = document.querySelector ( '.upload' );
const errorMessage = document.querySelector ( '.error-message' );
const successMessage = document.querySelector ( '.success-message' );
const fileListContainer = document.querySelector ( '.file-list-container' );
const fileFetchFailedMessage = document.querySelector ( '.file-fetch-failed' );

createList ( );



uploadButton.addEventListener ( 'click', event => {

    const fileInput = document.querySelector ( '.fileInput' );

    if ( fileInput.files.length == 0 ) {
        errorMessage.textContent = 'Please select a file!';
        errorMessage.style.color = 'text-danger';
        return;
    } else {
        errorMessage.textContent = '';
    }

    const formData = new FormData ();
    formData.append ( 'file', fileInput.files [ 0 ] );

    uploadFile ( formData );
});

function downloadFile(fileName) {
    const anchor = document.createElement("a");
    anchor.href = `http://localhost:8081/files/download?filePath=${fileName}`;
    anchor.style.display = 'none'; 
    anchor.setAttribute("download", fileName);
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);
}

async function uploadFile(formData) {

    try {
        const response = await fetch('http://localhost:8081/files/upload', {
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
    row.classList = 'row mt-2';
    row.uniqueIdentifier = uniqueIdentifier;

    const filenameDiv = document.createElement ( 'div' );
    filenameDiv.classList = 'col-5 m-1';
    filenameDiv.textContent = filename;

    const downloadButton = document.createElement ( 'div' );
    downloadButton.classList = 'col-1 m-1 btn btn-outline-info download-button';
    downloadButton.textContent = 'Download';

    const updateButton = document.createElement ( 'div' );
    updateButton.classList = 'col-1 m-1 btn btn-outline-success update-button';
    updateButton.textContent = 'Update';

    const deleteButton = document.createElement ( 'div' );
    deleteButton.classList = 'col-1 m-1 btn btn-outline-danger delete-button';
    deleteButton.textContent = 'Delete';

    row.appendChild ( filenameDiv );
    row.appendChild ( downloadButton );
    row.appendChild ( updateButton );
    row.appendChild ( deleteButton );

    return row;
}

async function createList ( ) {
    const apiUrl = 'http://localhost:8081/files';

    let apiResponse = null;
    try {
        const response = await fetch ( apiUrl );

        if ( response.status !== 200 ) {
            fileFetchFailedMessage.textContent = 'Fetching files from server failed!';
            throw new Error ( `HTTP error! Status: ${response.status}` );
        } else {
            fileFetchFailedMessage.textContent = '';
        }

        apiResponse = await response.json ();
    } catch ( error ) {
        console.error ( `Fetch error: ${error}` );
    }

    apiResponse.content.forEach ( file => {
        fileListContainer.appendChild ( createFileListItem ( file.filename, file.uniqueIdentifier ) );
    });
}
