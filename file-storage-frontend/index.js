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

function downloadFile(fileName) {
    const anchor = document.createElement("a");
    anchor.href = `http://localhost:8081/files/download?filePath=${fileName}`;
    anchor.style.display = "none"; // Hide the anchor
    anchor.setAttribute("download", fileName);
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);
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

