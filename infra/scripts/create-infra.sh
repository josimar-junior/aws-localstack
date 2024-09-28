#!/bin/bash

echo "INIT HERE!!!"

# Wait for LocalStack to be ready
echo "Waiting for LocalStack to start..."
until $(curl --output /dev/null --silent --head --fail http://localstack:4566); do
    printf '.'
    sleep 5
done

# Create SQS queue
echo "Creating SQS queue..."
awslocal sqs create-queue --queue-name person-queue
echo "SQS queue created."

echo "Creating DynamoDB table"
awslocal dynamodb create-table \
    --table-name Person \
    --attribute-definitions \
        AttributeName=cpf,AttributeType=S \
    --key-schema \
        AttributeName=cpf,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:4566 \
    --region us-east-1
echo "DynamoDB table created"