const port = 3000

// Firebase-admin is used to access Firebase
const functions = require('firebase-functions');
const admin = require('firebase-admin');
process.env.GCLOUD_PROJECT = 'learning-referral-network';
admin.initializeApp({
  credential: admin.credential.applicationDefault()
});
const db = admin.firestore();

var Express = require('express');
var App = Express();

App.listen(port, function () {
  console.log("Listening to port " + port);
});

App.get('/app/:id', (req, res) => {
  var appsRef = db.collection('apps');

  appsRef.where('appId', '==', parseInt(req.params.id)).get()
      .then(snapshot => {
        if (snapshot.empty) {
          res.status(200).send({});
          return null;
        }
        return snapshot.forEach(doc => {
          console.log(doc.id, '=>', doc.data().appId);
          res.status(200).send(doc.data());
        });
      })
      .catch(err => {
        console.log('error getting app', err);
        res.status(500).send("System error, please try again.");
      })
});

exports.admin = functions.https.onRequest(App);
