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

    const requestInfo = {
      apiId: "string",
      ver: "string",
      ts: 0,
      action: "string",
      did: "string",
      key: "string",
      msgId: "string",
      requesterId: "string",
      authToken: "06aabaa6-3431-41eb-a97c-ab8265289d91",
      userInfo: {
        uuid: "435f92c-e22a-4a2e-8a5e-3f03baa46cb1"
      }
    };    

    const treasuryParams = {
      status: paymentStatus,
      rek: returnParams.rek,
      data: returnParams.data,
      hmac: returnParams.hmac,
      authToken: returnHeader.AuthToken
    };

    const dataToSend = {
      RequestInfo: requestInfo, 
      TreasuryParams: treasuryParams
    }
    // Send data to the backend service
    let backendResponse;
    try {
      backendResponse = await axios.post(`${serverUrl}/etreasury/payment/v1/_decryptTreasuryResponse`, dataToSend, {
        headers: {
          "Content-Type": "application/json"
        }
      });
      console.log("Backend response:", backendResponse.data);
    } catch (backendError) {
      console.error("Backend request error:", backendError);
      backendResponse = null;
    }

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
