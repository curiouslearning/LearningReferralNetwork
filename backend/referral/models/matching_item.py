# coding: utf-8

from __future__ import absolute_import, annotations
from datetime import date, datetime  # noqa: F401

from typing import List, Dict  # noqa: F401

from models import *
from models.app_info import AppInfo
from models.base_model_ import Model
from common import util


class MatchingItem(Model):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    """

    def __init__(self, item: AppInfo=None, score: float=None):  # noqa: E501
        """MatchingItem - a model defined in Swagger

        :param item: The item of this MatchingItem.  # noqa: E501
        :type item: AppInfo
        :param score: The score of this MatchingItem.  # noqa: E501
        :type score: float
        """
        self.swagger_types = {
            'item': AppInfo,
            'score': float
        }

        self.attribute_map = {
            'item': 'item',
            'score': 'score'
        }

        self._item = item
        self._score = score

    @classmethod
    def from_dict(cls, dikt) -> 'MatchingItem':
        """Returns the dict as a model

        :param dikt: A dict.
        :type: dict
        :return: The MatchingItem of this MatchingItem.  # noqa: E501
        :rtype: MatchingItem
        """
        return util.deserialize_model(dikt, cls)

    @property
    def item(self) -> AppInfo:
        """Gets the item of this MatchingItem.


        :return: The item of this MatchingItem.
        :rtype: AppInfo
        """
        return self._item

    @item.setter
    def item(self, item: AppInfo):
        """Sets the item of this MatchingItem.


        :param item: The item of this MatchingItem.
        :type item: AppInfo
        """

        self._item = item

    @property
    def score(self) -> float:
        """Gets the score of this MatchingItem.


        :return: The score of this MatchingItem.
        :rtype: float
        """
        return self._score

    @score.setter
    def score(self, score: float):
        """Sets the score of this MatchingItem.


        :param score: The score of this MatchingItem.
        :type score: float
        """

        self._score = score
