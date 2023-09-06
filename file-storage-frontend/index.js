'use strict';

const uploadButton = document.querySelector ( '.upload' );
const errorMessage = document.querySelector ( '.error-message' );
const fileListContainer = document.querySelector ( '.file-list-container' );

createList ( );

uploadButton.addEventListener ( 'click', event => {
    const fileInput = document.querySelector ( '.fileInput' );

    if ( fileInput.files.length == 0 ) {
        errorMessage.textContent = 'Please select a file!';
        errorMessage.style.color = '#ff0000';
        return;
    } else {
        errorMessage.textContent = '';
    }

    const formData = new FormData ();
    formData.append ( 'file', fileInput.files [ 0 ] );

    uploadFile ( formData );
});


/*

const downloadLink = document.getElementById("downloadLink");

downloadLink.addEventListener("click", function (event) {
    event.preventDefault();

    const fileName = document.getElementById("metadataDownloadInput").value;
    if (!fileName) {
        alert("Please provide the file name");
        return;
    }

    downloadFile(fileName);
});

*/

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
    /*
    fetch("http://localhost:8081/files/upload", {
        method: "POST",
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Network response was not ok");
        }
        return response.json(); 
    })
    .then(data => {
        console.log("Response from server:", data);
    })
    .catch(error => {
        console.error("Error:", error);
    });
    */

    try {
        const response = await fetch('http://localhost:8081/files/upload', {
            method: 'POST',
            body: formData
        });
    
        if ( response.status !== 201 ) {
            throw new Error('Network response was not ok');
        }
    
        const data = await response.json();
        console.log('Response from server:', data);
    } catch (error) {
        console.error('Error:', error);
    }
}

function createFileListItem ( filename ) {
    const row = document.createElement ( 'div' );
    row.classList = 'row mt-2';

    const filenameDiv = document.createElement ( 'div' );
    filenameDiv.classList = 'col-5 m-1';
    filenameDiv.textContent = filename;

    const downloadButton = document.createElement ( 'div' );
    downloadButton.classList = 'col-1 m-1 btn btn-outline-info';
    downloadButton.textContent = 'Download';

    const updateButton = document.createElement ( 'div' );
    updateButton.classList = 'col-1 m-1 btn btn-outline-success';
    updateButton.textContent = 'Update';

    const deleteButton = document.createElement ( 'div' );
    deleteButton.classList = 'col-1 m-1 btn btn-outline-danger';
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
            throw new Error ( `HTTP error! Status: ${response.status}` );
        }

        apiResponse = await response.json ();
    } catch ( error ) {
        console.error ( `Fetch error: ${error}` );
    }

    apiResponse.content.forEach ( file => {
        fileListContainer.appendChild ( createFileListItem ( file.filename ) );
    });
}
