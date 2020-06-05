"""
Script to deploy Swagger API spec to Cloud Endpoint
"""
import argparse
import os
import subprocess
from typing import List
from urllib.parse import urlparse

class GCloudCommand:
    def __init__(self, api: str, group:str, command:str, **kwargs):
        # Example: gcloud run services list --platform="managed"
        #   api = 'run'
        #   subcommands = ['services', 'list']
        #   platform="managed"
        self.base_cmd = ['gcloud', api, group, command]
        # Common arguments to gcloud
        # --quiet: remove stdout messages
        # --project: specify the GCP project ID for this command
        self.common_args = ['--quiet',
                            '--project=' + os.environ['PROJECT_ID']]
        # Command exit code
        self.exit_code_ok = 0

    def run(self, args_list: list) -> list:
        cmd = self.base_cmd.copy()
        cmd.extend(args_list)
        cmd.extend(self.common_args)
        process = subprocess.Popen(cmd, stdout=subprocess.PIPE, universal_newlines=True)
        return_values = []

        while True:
            # With --quiet, command return value are output on each line
            output = process.stdout.readline().strip()
            if output != '':
                return_values.append(output)

            return_code = process.poll()
            if return_code is not None:
                # Process has finished, read rest of the output
                if return_code != self.exit_code_ok:
                    message = process.stdout.read()
                    raise RuntimeError('gcloud command failed. Error: ' + message)
                return return_values

class GCloudCommandFactory:
    def __init__(self):
        self.builders = []

    def register_command(self, name, api, command, **kwargs):
        self.builders.append(GCloudCommand(api, command, **kwargs))

    def build(self, name):
        if name not in self.builders:
            raise ValueError('Builder for command %s not found' % name)
        return self.builders[name]

class CloudServiceClient:
    def __init__(self, api:str):
        # gcloud api name
        self.api = api
        # Common arguments used when calling SDK commands for this client
        self.common_args = []
        # Key for retriveing service name when calling "services list", this
        # is not the same for different APIs. For example, App uses 'id'
        # while Endpoints uses serviceName
        self.service_name_key = ''
        self.region = os.environ['REGION']

    def list_services(self) -> List[str]:
        cmd = GCloudCommand(self.api, 'services', 'list')
        args = ['--format=value(%s)' % self.service_name_key]
        args.extend(self.common_args)
        try:
            return cmd.run(args)
        except RuntimeError as e:
            print(str(e))
            return []


class CloudRunClient(CloudServiceClient):
    def __init__(self):
        super(CloudRunClient, self).__init__('run')
        self.common_args = ['--platform=managed']
        self.service_name_key = 'metadata.name'
        self.esp_image = 'gcr.io/endpoints-release/endpoints-runtime-serverless:1'

    def describe_service(self, name: str, attr_keys: list) -> dict:
        cmd = GCloudCommand(self.api, 'services', 'describe')
        args = [name]
        if attr_keys:
            args.append('--format=value(%s)' % ','.join(attr_keys))
        args.extend(self.common_args)
        try:
            output = cmd.run(args)
            attributes = output[0].split(' ')
            if len(attributes) != len(attr_keys):
                raise ValueError('Returned number of attributes does not match')
            ret = {}
            for i, attr in enumerate(attr_keys):
                ret[attr] = attributes[i]
            return ret
        except RuntimeError as e:
            print(str(e))
            return {}

    def deploy_esp_service(self, name: str, **kwargs):
        if name in self.list_services():
            cmd = GCloudCommand(self.api, 'services', 'update')
            # First arg after update is the service name
            args = [name]
        else:
            # gcloud run deploy does not work
            cmd = GCloudCommand(self.api, 'deploy', name)
            args = ['--region=%s' % self.region,
                    '--allow-unauthenticated',
                    '--image=%s' % self.esp_image]

        args.extend(self.common_args)
        # kwargs are additional '--' arguments to pass into the command
        for k, v in kwargs.items():
            args.append('--%s=%s' % (k, v))

        try:
            cmd.run(args)
        except RuntimeError as e:
            print(str(e))


class EndpointsClient(CloudServiceClient):
    def __init__(self):
        super(EndpointsClient, self).__init__('endpoints')
        self.service_name_key = 'serviceName'

    def deploy_service(self, config_file: str, **kwargs):
        cmd = GCloudCommand(self.api, 'services', 'deploy')

        # First arg after update is the service name
        args = [config_file]
        args.extend(self.common_args)

        # kwargs are additional '--' arguments to pass into the command
        for k, v in kwargs.items():
            args.append('--%s=%s' % (k, v))
        try:
            ret = cmd.run(args)
            print(ret)
        except RuntimeError as e:
            print(str(e))


class AppEngineClient(CloudServiceClient):
    def __init__(self):
        super(AppEngineClient, self).__init__('app')
        self.service_name_key = 'id'

    def deploy_service(self, config_file: str, **kwargs):
        cmd = GCloudCommand(self.api, 'deploy', config_file)
        args = self.common_args.copy()

        # kwargs are additional '--' arguments to pass into the command
        for k, v in kwargs.items():
            args.append('--%s=%s' % (k, v))
        try:
            ret = cmd.run(args)
            print(ret)
        except RuntimeError as e:
            print(str(e))



if __name__ == '__main__':
    # parser = argparse.ArgumentParser(
    #     description=__doc__,
    #     formatter_class=argparse.RawDescriptionHelpFormatter)
    # parser.add_argument('project', type=str, help='cloud project ID')
    # parser.add_argument('--endpoints', type=str, help='deploy cloud endpoints with the given spec file')
    # parser.add_argument('--update', action='store_true', help='update gcloud components')
    # parser.add_argument('--dry-run', action='store_true', help='Validate endpoint deployment only')
    # args, additionalArgs = parser.parse_known_args()

    cloud_run = CloudRunClient()
    endpoints = EndpointsClient()
    app_engine = AppEngineClient()

    # Name of the API service to deploy. Note that the name should match the folder name
    # where the api.yaml (for Endpoints) and app.service.yaml (for App Engine) can be found
    services = ['referral']

    get_gateway_name = lambda service: '%s-gateway' % service

    for service in services:
        config_path = os.path.join('./', service)
        gateway_name = get_gateway_name(service)

        # Create an ESP container for each service
        if gateway_name not in cloud_run.list_services():
            cloud_run.deploy_esp_service(gateway_name)

        # For each service, we need to deploy the Endpoints configuration, ESP container
        # on Cloud Run and the backend service on App Engine
        app_engine.deploy_service(os.path.join(config_path, 'app.service.yaml'))

        # Update the openAPI spec Host to the endpoint_service_name
        endpoints.deploy_service(os.path.join(config_path, 'api.yaml'))

        # Get ESP host name which will be used as Endpoint service name
        # service_attr = cloud_run.describe_service(gateway_name, ['status.address.url'])
        # url = service_attr['status.address.url']
        # endpoint_service_name = urlparse(url).hostname

        # HACK - gcloud run services describe cannot find server
        endpoint_service_name = service + '-gateway-hj2cd4bxba-de.a.run.app'

        # Enable Endpoint services
        try:
            GCloudCommand('services', 'enable', endpoint_service_name).run([])
        except RuntimeError as e:
            print('Failed to enable servive: ' + str(e))

        # Update ESP environment variable
        # The endpoint service name is the same as the ESP host because that is the entry point
        # for all API requests. Endpoints only support 1 openAPI spec
        update_args = {'set-env-vars': '^|^ENDPOINTS_SERVICE_NAME=%s|ESP_ARGS=--rollout_strategy=managed,--cors_preset=basic' % endpoint_service_name}
        cloud_run.deploy_esp_service(gateway_name, **update_args)



