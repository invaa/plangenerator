# Repayment plan generator for loans

Backend micro-service is configured for Docker containerisation and can be scaled with container management system.
Results are cached for better performance (currently, cache is not distributed).
Code is automatically validated against common style rules using checkstyles plugin.
In order to support encrypted properties the service is integrated with jasypt library (for details see Jasypt section below).

Out of scope:
- Authentication

Tech stack: Java 11, spring-boot, Docker

Limitations:
- montlyh interest amount to pay can't be greater than annuity (the borrower would never be able to pay the loan) 
  and therefore one would get an exception
- min loan amount should be greater than 0, max loan amount should not be greater than 1 mln
- startDate should be provided, but there is no validation if it's in the past or future
- nominalRate should be between 0 and 100
- duration should be between 1 month and 100 years

Test coverage:
- 91% classes, 93% lines

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

### 3. Running as cluster
There is an option to run multiple instances of the service with docker compose. 
Here is an expample of running 3 instances loadbalances as roundrobing by nginx:
```
docker-compose up --scale plangenerator=3
```

## Configuration
There are 2 plan generation properties that can be configured in application.properties or application.yaml 
``` 
plan.generator.division.precision (the non-negative precision for floating point calculations, default value = 8)
plan.generator.result.rounding (result fraction, default value = 2)
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

## Load test results
Demo run result is available here (random loan plan requests with different rates from 1.0 to 6.0, 1000 users over 2 mins, 1 service instance):
```
./docs/plangeneratorloadtest
```

For getting better throughput it's recommended to run application in cluster mode (See run option 3.).
First run application on port 8080 or change the port in the test PlanGeneratorLoadTest.scala
Then execute the load test:
``` 
./mvnw --settings=settings.xml gatling:test
```
