document.getElementById("uploadForm").addEventListener("submit", function (event) {
    event.preventDefault();

    const fileInput = document.getElementById("fileInput");
    const metadataInput = document.getElementById("metadataInput");

    if (fileInput.files.length === 0) {
        alert("Please select a file.");
        return;
    }

    const formData = new FormData();

    formData.append("file", fileInput.files[0]);
    formData.append("metadata", metadataInput.value);

    uploadFile(formData);
});

document.getElementById("downloadForm").addEventListener("submit", function (event) {
    event.preventDefault();

    const fileName = document.getElementById("metadataDownloadInput").value;
    if ( !fileName ) {
        alert ( "please provide the filename" );
        return;
    }

    downloadFile ( fileName );
});

function downloadFile ( fileName ) {
    fetch (`http://localhost:8081/files/download?filePath=${fileName}`, {
        method: "GET"
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
}


function uploadFile(formData) {
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
}

