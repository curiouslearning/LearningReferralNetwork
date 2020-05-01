# coding: utf-8

from __future__ import absolute_import

from datetime import date, datetime  # noqa: F401

from typing import List, Dict  # noqa: F401
from models.summary import Summary
from models.base_model_ import Model
from common import util


class Application(Model):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    """

    def __init__(self, skills: Dict[str, object]=None, summary: Summary=None):  # noqa: E501
        """Application - a model defined in Swagger

        :param skills: The skills of this Application.  # noqa: E501
        :type skills: Dict[str, object]
        :param summary: The summary of this Application.  # noqa: E501
        :type summary: Summary
        """
        self.swagger_types = {
            'skills': Dict[str, object],
            'summary': Summary
        }

        self.attribute_map = {
            'skills': 'skills',
            'summary': 'summary'
        }

        self._skills = skills
        self._summary = summary

    @classmethod
    def from_dict(cls, dikt) -> 'Application':
        """Returns the dict as a model

        :param dikt: A dict.
        :type: dict
        :return: The Application of this Application.  # noqa: E501
        :rtype: Application
        """
        return util.deserialize_model(dikt, cls)

    @property
    def skills(self) -> Dict[str, object]:
        """Gets the skills of this Application.

        Skills are key-value pairs that describe the application content and intent. Certain skills can be applied to most applications and those are defined here. Such skills are expected to grow as we gain more understanding of the product, but existing definitions should not change for backward compatibility reasons. Applications are not required to have all the skills defined either. It is also possible for applications to provide additional keyword “tags” to help improve query relevance.  # noqa: E501

        :return: The skills of this Application.
        :rtype: Dict[str, object]
        """
        return self._skills

    @skills.setter
    def skills(self, skills: Dict[str, object]):
        """Sets the skills of this Application.

        skills are key-value pairs that describe the application content and intent. Certain skills can be applied to most applications and those are defined here. Such skills are expected to grow as we gain more understanding of the product, but existing definitions should not change for backward compatibility reasons. Applications are not required to have all the skills defined either. It is also possible for applications to provide additional keyword “tags” to help improve query relevance.  # noqa: E501

        :param skills: The skills of this Application.
        :type skills: Dict[str, object]
        """

        self._skills = skills

    @property
    def summary(self) -> Summary:
        """Gets the summary of this Application.


        :return: The summary of this Application.
        :rtype: Summary
        """
        return self._summary

    @summary.setter
    def summary(self, summary: Summary):
        """Sets the summary of this Application.


        :param summary: The summary of this Application.
        :type summary: Summary
        """

        self._summary = summary
