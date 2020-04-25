# coding: utf-8

from __future__ import absolute_import

from flask import json
from six import BytesIO

from models.api_error import ApiError  # noqa: E501
from models.application import Application  # noqa: E501
from test import BaseTestCase


class TestAdminController(BaseTestCase):
    """AdminController integration test stubs"""

    def test_add_application(self):
        """Test case for add_application

        
        """
        body = Application()
        response = self.client.open(
            '/v1/admin/app',
            method='POST',
            data=json.dumps(body),
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_delete_app(self):
        """Test case for delete_app

        Delete application from database
        """
        response = self.client.open(
            '/v1/admin/app/{appId}'.format(appId=56),
            method='DELETE')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_get_app_by_id(self):
        """Test case for get_app_by_id

        Returns all application metadata
        """
        response = self.client.open(
            '/v1/admin/app/{appId}'.format(appId=56),
            method='GET')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_update_application(self):
        """Test case for update_application

        
        """
        response = self.client.open(
            '/v1/admin/app',
            method='PUT')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))


if __name__ == '__main__':
    import unittest
    unittest.main()
