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

    const dataToSend = {
        status: paymentStatus,
        rek: returnParams.rek,
        data: returnParams.data,
        hmac: returnParams.hmac,
        authToken: returnParams.AuthToken
    };

    let htmlFile;
    if (paymentStatus == true || paymentStatus == 'true' || paymentStatus == 'Y' || paymentStatus == 'success') {
        htmlFile = 'payment-success.html';
    } else {
        htmlFile = 'payment-failure.html';
    }

    paymentCompletionEvent(dataToSend);

    // Send a response with the appropriate HTML file
    const htmlFilePath = path.join(__dirname, 'public', htmlFile);
    res.sendFile(htmlFilePath, (err) => {
        if (err) {
            res.status(500).send(err);
        } else {
            // Inject the data into the client-side script
            res.write(`
                <script>
                    document.addEventListener('DOMContentLoaded', (event) => {
                        const dataToSend = ${JSON.stringify(dataToSend)};

                        if (dataToSend) {
                            // Create and dispatch a custom event with the payment data
                            const completionEvent = new CustomEvent('payment-process-completed', { detail: dataToSend });
                            window.opener.dispatchEvent(completionEvent);

                            console.log('Dispatched payment-process-completed event with data:', dataToSend);

                            // Optionally close the popup window
                            setTimeout(() => {
                                window.close();
                            }, 15000);
                        }
                    });
                </script>
            `);
        }
    });
});  

app.listen(port, () => {
    console.log(`Server running at ${externalHost}${contextPath}`);
});
