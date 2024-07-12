const express = require("express");
const path = require("path");
const axios = require("axios");

const app = express();
const port = 8080;
const externalHost = process.env.EXTERNAL_HOST || "http://localhost:8088";
const contextPath = "/epayments";
const serverUrl = process.env.SERVER_URL || "http://egov-etreasury:8080";

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Log incoming requests
app.use((req, res, next) => {
  console.log(`Incoming ${req.method} request to ${req.url}`);
  if (req.method === "POST" || req.method === "PUT" || req.method === "PATCH") {
    console.log("Request body:", req.body);
  }
  next();
});

app.use(express.static("public"));

app.post(`${contextPath}`, async (req, res) => {
  try {
    const returnParams = JSON.parse(req.body.RETURN_PARAMS);
    const returnHeader = JSON.parse(req.body.RETURN_HEADER);
    const paymentStatus = returnParams.status;

    const dataToSend = {
      status: paymentStatus,
      rek: returnParams.rek,
      data: returnParams.data,
      hmac: returnParams.hmac,
      authToken: returnHeader.AuthToken
    };

    // Send data to the backend service
    const backendResponse = await axios.post(`${serverUrl}/payment/v1/_decryptTreasuryResponse`, dataToSend, {
      headers: {
        "Content-Type": "application/json"
      }
    });

    // Log the backend response
    console.log("Backend response:", backendResponse.data);

    let htmlFile;
    if (
      paymentStatus === true ||
      paymentStatus === "true" ||
      paymentStatus === "Y" ||
      paymentStatus === "success"
    ) {
      htmlFile = "payment-success.html";
    } else {
      htmlFile = "payment-failure.html";
    }

    const htmlFilePath = path.join(__dirname, "public", htmlFile);
    res.sendFile(htmlFilePath);
  } catch (error) {
    console.error("Error:", error);
    res.status(500).send("Internal Server Error");
  }
});

app.listen(port, () => {
  console.log(`Server running at ${externalHost}${contextPath}`);
});