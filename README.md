# Serverless Expense Tracking System

A cloud-native, serverless expense tracking application built with Spring Boot, AWS Lambda, and DynamoDB.

## Project Overview

This system provides a scalable, cost-efficient solution for tracking expenses with the following features:
- User authentication via AWS Cognito
- Real-time expense tracking and analytics
- Event-driven architecture using AWS Lambda and SQS
- RESTful API exposed through AWS API Gateway
- Infrastructure as Code using Terraform

## Project Structure

```
.
├── backend/                 # Spring Boot backend application
│   ├── src/                # Source code
│   ├── pom.xml            # Maven configuration
│   └── README.md          # Backend-specific documentation
├── infrastructure/         # Infrastructure as Code
│   ├── terraform/         # Terraform configurations
│   └── cdk/               # AWS CDK configurations (optional)
├── frontend/              # React frontend application (optional)
│   ├── src/
│   ├── package.json
│   └── README.md
└── docs/                  # Project documentation
    ├── architecture/      # Architecture diagrams and documentation
    └── api/              # API documentation
```

## Prerequisites

- Java 17 or later
- Maven 3.8+
- AWS CLI configured with appropriate credentials
- Terraform 1.0+
- Node.js 16+ (for frontend development)

## Getting Started

1. Clone the repository
2. Set up AWS credentials
3. Configure environment variables
4. Deploy infrastructure using Terraform
5. Build and deploy the backend
6. (Optional) Build and deploy the frontend

## Architecture

The system follows a serverless architecture with the following components:
- AWS Cognito for user authentication
- AWS Lambda for serverless functions
- DynamoDB for data storage
- SQS for asynchronous processing
- API Gateway for REST API exposure

## Development

### Backend Development
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Infrastructure Deployment
```bash
cd infrastructure/terraform
terraform init
terraform plan
terraform apply
```

### Frontend Development (Optional)
```bash
cd frontend
npm install
npm start
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.