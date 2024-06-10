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
    console.log('Reurn Params: ', returnParams);
    console.log('Payment Status: ', paymentStatus);

    let htmlFile;
    if (paymentStatus == 'true' || paymentStatus == 'Y' || paymentStatus == 'success') {
        htmlFile = 'payment-success.html';
    } else {
        htmlFile = 'payment-failure.html';
    }

    // Send a response with the appropriate HTML file
    res.sendFile(path.join(__dirname, 'public', htmlFile));

});

app.listen(port, () => {
    console.log(`Server running at ${externalHost}${contextPath}`);
});
