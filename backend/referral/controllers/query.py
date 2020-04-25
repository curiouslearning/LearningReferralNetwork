import connexion
import six

from models.api_error import ApiError  # noqa: E501
from models.query_result import QueryResult  # noqa: E501
from common import util


def execute_query(limit=None):  # noqa: E501
    """Query the database

     # noqa: E501

    :param limit: Maximum number of items to return.
    :type limit: int

    :rtype: QueryResult
    """
    return 'execute query!'
