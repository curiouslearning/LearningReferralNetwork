# Learning Referral Network Backend

## Deployment
[Setup Cloud Endpoints for App Engine Standard](https://cloud.google.com/endpoints/docs/openapi/get-started-app-engine-standard)

An ESP is deployed on Cloud Run as the API proxy for the backend services. It acts as a
gateway to provide authentication of client requests based on our API security requirements,
and route them to the proper backend services running on App Engine. The security definitions
are defined in our OpenAPI spec document and its managed by Cloud Endpoints. The following
steps are required for the complete deployment:
1. Initialize GCP
    ```bash
    gcloud auth login
    gcloud config set project learning-referral-network
    ```
2. Deploy the backend services to App Engine
    ```bash
   gcloud app deploy admin/app.service.yaml referral/app.service.yaml
    ```
3. If this is the first time deploying the ESP on Cloud Run, the region needs to be set.
    ```bash
    gcloud config set run/region europe-west1
    ```
   _europe-west1_ as the majority of the user would be in Africa and Western Asia.
   Next, deploy the ESP container as a new Cloud Run service. Here, **gateway** is chosen as the Cloud Run service
name and it will become part of the host URL path for all API requests. 
    ```bash
    gcloud run deploy referral-gateway \
        --image="gcr.io/endpoints-release/endpoints-runtime-serverless:1" \
        --allow-unauthenticated
    ```
   While ESPv2 is available it is only in beta and does NOT appear to support having API
backend running App Engine **Standard** environment. We have chosen to run our services on App
Engine Standard mainly for cost purposes, as it provides a free quota. The gateway host address
would be shown on the CLI after the operation completes, or can be found on google cloud console.
5. Create an OpenAPI specification to describe the API for each service. The host URL
should be the ESP service address (ie. referral-gateway-[HASH].a.run.app)
6. Deploy the API spec to Cloud Endpoints
    ```bash
    gcloud endpoints services deploy referral/api.yaml
    ```
7. If this is the first time deploying the API to Endpoints, enable the API services
    ```bash
    gcloud services enable servicemanagement.googleapis.com
    gcloud services enable servicecontrol.googleapis.com
    gcloud services enable endpoints.googleapis.com
    gcloud services enable ENDPOINTS_SERVICE_NAME
    ```
   Since all the API backend are running behind the ESP, the host URL for 
   ENDPOINTS_SERVICE_NAME is the **host** URL in the OpenAPI spec
8. Update ESP configuration to point to the 
    ```bash
    gcloud run services update referral-gateway \
       --set-env-vars ENDPOINTS_SERVICE_NAME=[ENDPOINTS_SERVICE_NAME] \
    ```
