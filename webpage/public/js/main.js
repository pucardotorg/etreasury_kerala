const serverUrl = process.env.SERVER_URL || 'http://localhost:8090';

document.addEventListener('DOMContentLoaded', () => {
    if (window.receivedData) {

        const data = window.receivedData;

      /**  fetch(`${serverUrl}/v1/payment/challan_data`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => response.json())
        .then(data => {
            console.log('Success:', data);
        })
        .catch((error) => {
            console.error('Error:', error);
        }); */
    }
});
