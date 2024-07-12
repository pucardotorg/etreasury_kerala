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
        apiId: "Rainmaker",
        authToken: "d57ab03c-a3e3-4c2f-8de2-93c13dd2c00f",
        userInfo: {
            id: 378,
            uuid: "85dd084c-1f75-4513-87f3-765cef1b2462",
            userName: "9016190161",
            name: "SYSTEM USER",
            mobileNumber: "9016190161",
            type: "CITIZEN",
            roles: [
                {
                    name: "CASE_VIEWER",
                    code: "CASE_VIEWER",
                    tenantId: "kl"
                },
                {
                    name: "DEPOSITION_EDITOR",
                    code: "DEPOSITION_EDITOR",
                    tenantId: "kl"
                },
                {
                    name: "DEPOSITION_VIEWER",
                    code: "DEPOSITION_VIEWER",
                    tenantId: "kl"
                },
                {
                    name: "Citizen",
                    code: "CITIZEN",
                    tenantId: "kl"
                },
                {
                    name: "DEPOSITION_CREATOR",
                    code: "DEPOSITION_CREATOR",
                    tenantId: "kl"
                },
                {
                    name: "CASE_EDITOR",
                    code: "CASE_EDITOR",
                    tenantId: "kl"
                },
                {
                    name: "CASE_CREATOR",
                    code: "CASE_CREATOR",
                    tenantId: "kl"
                }
            ],
            active: true,
            tenantId: "kl"
        },
        msgId: "1720795009723|en_IN"
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
