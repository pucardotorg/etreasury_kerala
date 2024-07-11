const serverUrl = process.env.SERVER_URL || "http://localhost:8090";

document.addEventListener("DOMContentLoaded", () => {
  if (window.receivedData) {
    const data = window.receivedData;

    fetch(`${serverUrl}/payment/v1/_decryptTreasuryResponse`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    })
      .then((response) => response.json())
      .then((data) => {
        console.log("Success:", data);
        // Call function to handle the response
        handleApiResponse(data);
      })
      .catch((error) => {
        console.error("Error:", error);
      });
  }
});

function handleApiResponse(responseData) {
  window.apiResponseData = responseData;

  const responseContainer = document.getElementById("response-container");
  if (responseContainer) {
    responseContainer.textContent = JSON.stringify(responseData, null, 2);
  }
}
