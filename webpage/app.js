const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');

const app = express();
const port = 3000;

app.use(express.static('public'));
app.use(express.json());

app.post('', (req, res) => {

    const paymentStatus = req.body.status; 

    // Serve different HTML pages based on payment status
    let htmlFile;
    if (paymentStatus === 'true' || paymentStatus == 'Y' || paymentStatus == 'success') {
        htmlFile = 'payment-success.html';
    } else {
        htmlFile = 'payment-failure.html';
    }

    // Log the received data
    console.log(req.body);

    // Send a response with the appropriate HTML file
    res.sendFile(path.join(__dirname, 'public', htmlFile));

});

app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
