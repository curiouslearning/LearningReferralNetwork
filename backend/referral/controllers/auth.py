def verify_apikey(token, required_scopes):
    # A Extensible Service Proxy is deployed on Cloud Run that checks
    # for valid API key before passing the request to this service. So
    # We can skip authentication here for now
    return {
        'sub': 0
    }
