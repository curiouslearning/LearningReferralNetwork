import logging

import connexion

from common.config import Config
from firebase_admin import firestore
from models.api_error import ApiError
from models.application import Application
from models.referral_item import ReferralItem
from models.referral_result import ReferralResult
from models.app_info import AppInfo
from pydantic import BaseModel
from typing import Dict
from werkzeug.exceptions import BadRequest


# Basic validation is done by connexion based on the API definition
# TODO - may not need to use pydantic BaseModel and simple
#   dataclass is fine. Though we may want to do more involved validation
#   down the road
class ReferralRequestBody(BaseModel):
    package_name: str
    locale: str
    total_sessions: int = 0
    average_session_length: int = 0
    days_since_last_session: int = 0
    progress_by_skill: Dict[str, int] = {}
    max_results: int = Config.DEFAULT_RESULTS_LIMIT


def make_recommendation(body):  # noqa: E501
    """Invoke recommendation engine to get app referral

     # noqa: E501

    :param body: A JSON object containing application information
    :type body: dict | bytes

    :rtype: ReferralResult
    """
    if connexion.request.is_json:
        body = ReferralRequestBody.parse_obj(connexion.request.get_json())  # noqa: E501
    else:
        raise BadRequest('Invalid request body')

    db = firestore.client()

    # Holds the list of referral apps that will be returned in the
    # query response
    referrals = []
    try:
        apps_ref = db.collection(Config.APP_DB_COLLECTION)

        # TODO - Cloud Firestore provides limited support for logical OR queries.
        #   The in and array-contains-any operators support a logical OR of up to
        #   10 equality (==) or array-contains conditions on a single field. For
        #   other cases, create a separate query for each OR condition and merge
        #   the query results in your app.

        # TODO - Move this out to a separate recommendation engine package

        # For this demo, we are simply looking or other apps with the same locale.
        # Note that the storage of locale in the database may change
        db_query = apps_ref.where('locale', '==', body.locale)

        # Execute query
        apps = db_query.limit(body.max_results).stream()

        for app in apps:
            # Need to integrate Firestore client with Summary model
            # Keys used in Firestore should be the same as model here
            # Use protobuf
            app_doc = app.to_dict()
            # from_dict will ignore attributes that are not part of AppInfo
            # since AppInfo is a subset of the fields in Application
            app_info = AppInfo.from_dict(app_doc)

            # Add to the list of matching app
            referrals.append(ReferralItem(item=app_info, score=1))
    except Exception as e:
        logging.error('Failed to get document from firestore with error: ' + str(e))
        return ApiError(400, 'Failed to get fetch results')

    return ReferralResult(result=referrals)
