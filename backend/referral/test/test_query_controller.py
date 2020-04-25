# coding: utf-8

from __future__ import absolute_import

from flask import json
from six import BytesIO

from models.api_error import ApiError  # noqa: E501
from models.query_result import QueryResult  # noqa: E501
from test import BaseTestCase


class TestQueryController(BaseTestCase):
    """QueryController integration test stubs"""

    def test_query_by_attributes(self):
        """Test case for query_by_attributes

        Query the database
        """
        query_string = [('limit', 100)]
        response = self.client.open(
            '/v1/query',
            method='GET',
            query_string=query_string)
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))


if __name__ == '__main__':
    import unittest
    unittest.main()
