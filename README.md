# Introduction

This is a simple [java spark app](src/main/java/io/archetypal/spark/Spark.java) application that wraps
a git object store.

# Deploy

Docker image:

```bash
mvn clean package -DskipTests
docker build -t archetypal/git-api:v0.0.1 .
```

```bash
docker run --rm \
  -p 8787:8787 \
  archetypal/git-api:v0.0.1 https://github.com/dockcmd/aws-sh
```

# Run Local

```bash
java -jar target/git-api-jar-with-dependencies.jar https://github.com/dockcmd/aws-sh
```

# Test

```bash
curl http://localhost:8787/v1/aws-sh/README.md
```