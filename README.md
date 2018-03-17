AWS Lambda Monitor (Kotlin)
=============
This AWS Lambda uses the Lambda and Cloudwatch APIs to monitor the invocation counts and success rates of AWS Lambdas. The JSON response is designed to be accessible via API Gateway.

Run Locally
------------
Right click run-local.kt and click `Run`. This executes a wrapper around the Lambda that prints what would be the body of the API Gateway response.
The default clients are used for AWS Lambda and Cloudwatch, which assumes you have appropriate credentials to make the client requests.

Configuration
--------------
For each Lambda function, an expectation should be set for how often we should expect invocations. We provide an expectation for each known Lambda, which is
a configurable time period (day, week etc.) and a lower bound on the expected number of invocations, so that if the actual number of invocations is less,
attention can be drawn to this fact.

The expectations are configured at the top of AwsLambdaMonitor.kt , and organised by category. Any Lambdas that are found that have not been categorised already
will be put into an 'Unexpected' category to prompt categorisation.

Deployment
-----------
Designed for deploying as an AWS Lambda, triggered by a GET request on Amazon API Gateway.