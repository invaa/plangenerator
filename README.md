# Repayment plan generator for loans

Backend micro-service is configured for Docker containerisation and can be scaled with container management system.
Results are cached for better performance.
Code is automatically validated against common style rules using checkstyles.
In order to support encrypted properties the service is integrated with jasypt library (for details see Jasypt section below).

Out of scope:
- Authentication

Tech stack: Java 11, spring-boot, Docker

Limitations:
- interest can't be greater than annuity (the borrower would never be able to pay the loan) and therefore one would get an exception
- min loan amount > 0, max load amount <= 1 mln
- startDate should be provided, there is no validation if it's in the past or future
- nominalRate is between 0 and 100
- duration is between 1 month and 100 years

## How to run the backend

### 1. With Docker (Recommended)
Prerequisites: installed docker, internet

```
docker run -p 8080:8080 -t invaa/plangenerator:latest
```

### 2. Maven + Spring-Boot
Prerequisites: installed jdk 1.11 (code is compatible with jdk 1.8), git, internet

```
cd <your project dir> 
git clone https://github.com/invaa/plangenerator.git
cd plangenerator
./mvnw --settings=settings.xml clean install
./mvnw --settings=settings.xml spring-boot:run -Djasypt.encryptor.password=password
```

## Jasypt library
Default password is password and for usage please read guide on http://www.jasypt.org/

## Service health info
The api is integrated with Spring Actuator.
You can check if service instance is running by invoking: 
```
http://localhost:8080/actuator/health
```

## Documentation
Swagger UI page:
```
http://localhost:8080/swagger-ui.html
```

Swagger json file:
```
http://localhost:8080/v2/api-docs
```

## Examples

You can find the example calls in the collection below.
Install postman an import collection from project path
```
./src/test/postman/lendico.postman_collection.json
```
