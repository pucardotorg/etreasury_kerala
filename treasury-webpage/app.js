const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');

const app = express();
const port = 8080;
const externalHost = process.env.EXTERNAL_HOST || 'http://localhost:8088';
const contextPath = '/epayments';  

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

//log incoming requests
app.use((req, res, next) => {
    console.log(`Incoming ${req.method} request to ${req.url}`);
    if (req.method === 'POST' || req.method === 'PUT' || req.method === 'PATCH') {
        console.log('Request body:', req.body);
    }
    next();
});

app.use(express.static('public'));

app.post(`${contextPath}`, (req, res) => {

    returnParams = JSON.parse(req.body.RETURN_PARAMS);
    returnHeader = JSON.parse(req.body.RETURN_HEADER);

    const paymentStatus = returnParams.status;

    // Serve different HTML pages based on payment status
    console.log('Return Params: ', returnParams);
    console.log('Payment Status: ', paymentStatus);

    storeInLocalStorage("status", paymentStatus);
    storeInLocalStorage("rek", returnParams.rek);
    storeInLocalStorage("data", returnParams.data);
    storeInLocalStorage("hmac", returnParams.hmac);
    storeInLocalStorage("authToken", returnParams.AuthToken);

    let htmlFile;
    if (paymentStatus == true || paymentStatus == 'true' || paymentStatus == 'Y' || paymentStatus == 'success') {
        htmlFile = 'payment-success.html';
    } else {
        htmlFile = 'payment-failure.html';
    }

    paymentCompletionEvent();

    // Send a response with the appropriate HTML file
    res.sendFile(path.join(__dirname, 'public', htmlFile));

});

function storeInLocalStorage(key, data) {
    // Convert data to a string if it's not already
    if (typeof data !== 'string') {
      data = JSON.stringify(data);
    }
    
    localStorage.setItem(key, data);
    console.log(`Stored data with key "${key}" in local storage.`);
}

function paymentCompletionEvent() {
    // Create a custom event object with payment status
    const completionEvent = new CustomEvent('payment-process-completed');

    // Dispatch the event to the parent window
    window.opener.dispatchEvent(completionEvent);
    
    // Optionally close the popup window
    setTimeout(() => {
        window.close();
      }, 15000); 
}  
  

app.listen(port, () => {
    console.log(`Server running at ${externalHost}${contextPath}`);
});
