import connexion

from common import encoder
import firebase_admin

app = connexion.App(__name__, specification_dir='./')
app.app.json_encoder = encoder.JSONEncoder
app.add_api('api.yaml', arguments={'title': 'LRN Endpoint on Cloud Run'})

# Initialize Firebase admin for Firestore access
firebase_admin.initialize_app()

if __name__ == '__main__':
    # This is used when running locally. Gunicorn is used to run the
    # application on Google App Engine. See entrypoint in app.service.yaml.
    app.run(host='127.0.0.1', port=8080, debug=True)