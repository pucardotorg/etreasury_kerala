const express = require("express");
const path = require("path");
const axios = require("axios");
const qs = require('qs');

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

    const requestInfo = getRequestInfo();

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
      // Log the data to send
      console.log("Data to send:", JSON.stringify(dataToSend, null, 2));
      backendResponse = await axios.post(`${serverUrl}/etreasury/payment/v1/_decryptTreasuryResponse`, dataToSend, {
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Basic ZWdvdi11c2VyLWNsaWVudDo="
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

async function getRequestInfo() {
  const url = process.env.DRISTI_URL || "https://dristi-kerala-dev.pucar.org/user/oauth/token?_=1713357247536";
  const data = qs.stringify({
    username: process.env.USERNAME || "payment-collector",
    password: process.env.PASSWORD || "Dristi@123",
    tenantId: "kl",
    userType: "EMPLOYEE",
    scope: "read",
    grant_type: "password"
  });

  const headers = {
    'Cache-Control': 'no-cache',
    'Connection': 'keep-alive',
    'Content-Type': 'application/x-www-form-urlencoded',
    'Authorization': 'Basic ZWdvdi11c2VyLWNsaWVudDo='
  };

  try {
    const response = await axios.post(url, data, { headers });
    console.log("Response data:", response.data);

    const accessToken = response.data.access_token;
    const userInfo = response.data.UserRequest;

    const requestInfo = {
      apiId: "Rainmaker",
      authToken: accessToken,
      userInfo: userInfo
    };

    return requestInfo;
  } catch (error) {
    console.error('Error fetching Auth token:', error.response ? error.response.data : error.message);
    throw error;
  }
}

app.listen(port, () => {
  console.log(`Server running at ${externalHost}${contextPath}`);
});
