const functions = require('firebase-functions');
const port = 3000

var Express = require('express');
var App = Express();

App.listen(port, function() {
    console.log("Listening to port " + port);
});

App.get('/ping', (request, response) => {
 response.send("Hello from Firebase!");
});

exports.app = functions.https.onRequest(App);
