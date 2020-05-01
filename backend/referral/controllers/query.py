import logging

from common.config import Config
from firebase_admin import firestore
from models.api_error import ApiError
from models.application import Application
from models.matching_item import MatchingItem
from models.query_result import QueryResult
from models.app_info import AppInfo

def execute_query(locale, limit=Config.DEFAULT_QUERY_LIMIT, skills=None):  # noqa: E501
    """Query the database

     # noqa: E501

    :param limit: Maximum number of items to return.
    :type limit: int (default = 20)

    :rtype: QueryResult
    """
    db = firestore.client()

    # Holds the list of matching items that will be returned in the
    # query response
    matching_items = []
    try:
        apps_ref = db.collection(u'apps')

        # TODO - Ideally we would like to filter out the calling app from the
        #   list of matching results, so to avoid potentially suppressing other
        #   apps from making to the list because of limit param or lower score
        #   We may need to add another query param for the caller ID?

        # TODO - Cloud Firestore provides limited support for logical OR queries.
        #   The in and array-contains-any operators support a logical OR of up to
        #   10 equality (==) or array-contains conditions on a single field. For
        #   other cases, create a separate query for each OR condition and merge
        #   the query results in your app.
        db_query = apps_ref.where('locale', '==', locale)

        # List of search terms to pass into Firestore query
        query_terms = []
        if skills:
            query_terms = skills.split(',')
            db_query = db_query.where(u'skills', u'array_contains_any', query_terms)

        # Execute query
        apps = db_query.limit(limit).stream()

        for app in apps:
            # Need to integrate Firestore client with Summary model
            # Keys used in Firestore should be the same as model here
            # Use protobuf
            app_doc = app.to_dict()
            # from_dict will ignore attributes that are not part of AppInfo
            # since AppInfo is a subset of the fields in Application
            app_info = AppInfo.from_dict(app_doc)

            # Crude way of calculating score based on the number of matches
            if len(query_terms) > 0:
                app_skills = set(app_doc['skills'])
                matching_skills = app_skills.intersection(set(query_terms))
                matching_score = len(matching_skills)/len(query_terms)
            else:
                matching_score = 1

            # Add to the list of matching app
            matching_items.append(MatchingItem(item=app_info, score=matching_score))
    except Exception as e:
        logging.error('Failed to get document from firestore with error: ' + str(e))
        return ApiError(400, 'Failed to get fetch results')

    return QueryResult(result=matching_items)
