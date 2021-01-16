# ucu-scala-final-project

## Requirements
You should have such software to work with this project:
> sbt==1.4.6  
> docker  
> docker-compose

## Build
To build the project with docker, use:
```
sbt assembly && docker-compose build
```

## Run
To run this project, you need to:
1. Create a new __.env__ file based on __.env.example__ one with your credentials
2. Execute ```docker-compose up```
