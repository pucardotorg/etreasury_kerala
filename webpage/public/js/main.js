document.addEventListener('DOMContentLoaded', () => {
    if (window.receivedData) {

        const data = window.receivedData;
        
        const authToken = window.receivedHeaders.authToken;

        // Function to extract headers from the query parameters
        function getHeadersFromQuery() {
            const urlParams = new URLSearchParams(window.location.search);
            let headers = {};
            for (const [key, value] of urlParams.entries()) {
                headers[key] = value;
            }
            return headers;
        }

        // Extract headers
        const receivedHeaders = getHeadersFromQuery();

        fetch('/api/data', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'authToken': '${authToken}'
            },
            body: JSON.stringify(data)
        })
        .then(response => response.json())
        .then(data => {
            console.log('Success:', data);
        })
        .catch((error) => {
            console.error('Error:', error);
        });
    }
});
