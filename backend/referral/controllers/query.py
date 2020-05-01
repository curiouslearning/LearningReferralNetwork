import logging

from firebase_admin import firestore
from models.api_error import ApiError
from models.application import Application
from models.matching_item import MatchingItem
from models.query_result import QueryResult
from models.summary import Summary

def execute_query(locale, limit=20, keywords=None):  # noqa: E501
    """Query the database

     # noqa: E501

    :param limit: Maximum number of items to return.
    :type limit: int (default = 20)

    :rtype: QueryResult
    """
    db = firestore.client()

    # List of search terms to pass into Firestore query
    query_terms = []
    if keywords:
        query_terms = keywords.split(',')

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
        query = apps_ref.where(u'skills', u'array_contains_any', query_terms)

        # Execute query
        apps = query.limit(limit).stream()

        for app in apps:
            print(u'{} => {}'.format(app.id, app.to_dict().get('packageName')))
            # Need to integrate Firestore client with Summary model
            # Keys used in Firestore should be the same as model here
            # Use protobuf
            app_info = app.to_dict()
            app_summary = Summary(id=app.id)
            for attr in ('title', 'platform_id', 'locale'):
                app_summary.__setattr__(attr, app_info[attr])
            matching_app = MatchingItem(item=app_summary, score=1.0)
            matching_items.append(matching_app)
    except Exception as e:
        logging.error('Failed to get document from firestore with error: ' + str(e))
        return ApiError(400, 'Failed to get fetch results')

    return QueryResult(result=matching_items)
