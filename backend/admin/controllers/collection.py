import connexion
import six

from models.api_error import ApiError  # noqa: E501
from models.application import Application  # noqa: E501
from common import util


def add_app(body):  # noqa: E501
    """add_application

    Add application to the database # noqa: E501

    :param body: A JSON object containing application information
    :type body: dict | bytes

    :rtype: None
    """
    if connexion.request.is_json:
        body = Application.from_dict(connexion.request.get_json())  # noqa: E501
    return 'add app called!'


def delete_app(appId):  # noqa: E501
    """Delete application from database

     # noqa: E501

    :param appId: Unique app ID
    :type appId: int

    :rtype: None
    """
    return 'delete app called!'


def get_app_by_id(appId):  # noqa: E501
    """Returns all application metadata

     # noqa: E501

    :param appId: Unique app ID
    :type appId: int

    :rtype: Application
    """
    return 'get app by id called!'


def update_app():  # noqa: E501
    """update_application

    Update an app to the database # noqa: E501


    :rtype: None
    """
    return 'update app called!'
