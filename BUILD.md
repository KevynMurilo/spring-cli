# Build and Deployment Guide

## Prerequisites

### For JAR Build
- Java 21+
- Maven 3.8+

### For Native Image Build
- GraalVM 21+ (Community or Enterprise Edition)
- Native Image component installed
- Maven 3.8+

## Installing GraalVM

### Linux/MacOS

```bash
# Download GraalVM
wget https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-21.0.1/graalvm-community-jdk-21.0.1_linux-x64_bin.tar.gz

# Extract
tar -xzf graalvm-community-jdk-21.0.1_linux-x64_bin.tar.gz

# Move to /opt
sudo mv graalvm-community-openjdk-21.0.1 /opt/graalvm

# Set JAVA_HOME
export JAVA_HOME=/opt/graalvm
export PATH=$JAVA_HOME/bin:$PATH

# Add to ~/.bashrc or ~/.zshrc for persistence
echo 'export JAVA_HOME=/opt/graalvm' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
```

### Windows

```powershell
# Download GraalVM from https://github.com/graalvm/graalvm-ce-builds/releases

# Extract to C:\graalvm

# Set environment variables
setx JAVA_HOME "C:\graalvm"
setx PATH "%JAVA_HOME%\bin;%PATH%"
```

## Build Options

### 1. Build JAR (Quick Build)

```bash
# Clean and package
mvn clean package -DskipTests

# Run the JAR
java -jar target/spring-cli-1.0.0.jar
```

### 2. Build Native Image (Recommended)

```bash
# Build native executable
mvn clean package -Pnative -DskipTests

# This will create: target/spring-cli (Linux/Mac) or target/spring-cli.exe (Windows)

# Run the native executable
./target/spring-cli
```

### 3. Build with Tests

```bash
# JAR with tests
mvn clean package

# Native image with tests
mvn clean package -Pnative
```

## Build Performance

### JAR Build
- Time: ~30-60 seconds
- Size: ~50 MB
- Startup: ~2-3 seconds

### Native Image Build
- Time: ~5-10 minutes (first build)
- Size: ~100 MB
- Startup: ~0.1 seconds

## Installation

### Global Installation (Linux/Mac)

```bash
# After building native image
sudo cp target/spring-cli /usr/local/bin/
chmod +x /usr/local/bin/spring-cli

# Now you can run from anywhere
spring-cli
```

### Global Installation (Windows)

```powershell
# Copy to a directory in your PATH
copy target\spring-cli.exe C:\Windows\System32\

# Or add target directory to PATH
```

## Troubleshooting

### Build Fails with "Cannot find GraalVM"

Ensure JAVA_HOME points to GraalVM:
```bash
java -version
# Should show: GraalVM
```

### Native Image Build Fails

1. Ensure native-image is installed:
```bash
gu install native-image
```

2. Check memory:
Native image compilation requires at least 8GB RAM

3. Increase heap size:
```bash
export MAVEN_OPTS="-Xmx8g"
mvn clean package -Pnative
```

### Template Not Found Errors

Ensure templates are included in the JAR:
```bash
# Check if templates are in the JAR
jar tf target/spring-cli-1.0.0.jar | grep templates
```

## Docker Build

### Build Docker Image

```bash
# Multi-stage build
docker build -t spring-cli:1.0.0 .

# Run in Docker
docker run -it --rm spring-cli:1.0.0
```

### Dockerfile for Native Image

```dockerfile
FROM ghcr.io/graalvm/native-image:21 AS builder

WORKDIR /app
COPY . .

RUN ./mvnw clean package -Pnative -DskipTests

FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y libz-dev && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/spring-cli /usr/local/bin/spring-cli

ENTRYPOINT ["spring-cli"]
```

## Performance Optimization

### Native Image Optimization Flags

Already configured in `pom.xml`:
- `--no-fallback` - Pure native image, no JVM fallback
- `-H:+AddAllCharsets` - Include all character sets
- `--initialize-at-build-time` - Initialize classes at build time
- `-H:IncludeResources` - Include template resources

### JVM Optimization (JAR mode)

```bash
java -XX:+UseG1GC -XX:MaxRAMPercentage=75 -jar target/spring-cli-1.0.0.jar
```

## Distribution

### Create Release Package

```bash
# Build native image
mvn clean package -Pnative -DskipTests

# Create distribution
mkdir -p dist
cp target/spring-cli dist/
cp README.md dist/
cp LICENSE dist/

# Create archive
tar -czf spring-cli-1.0.0-linux-x64.tar.gz dist/

# Or ZIP for Windows
zip -r spring-cli-1.0.0-windows-x64.zip dist/
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build Native Image

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup GraalVM
        uses: graalvm/setup-graalvm@v1
        with:
          java-version: '21'
          distribution: 'graalvm'

      - name: Build Native Image
        run: mvn clean package -Pnative -DskipTests

      - name: Upload Artifact
        uses: actions/upload-artifact@v3
        with:
          name: spring-cli-native
          path: target/spring-cli
```

## Verification

After build, verify the installation:

```bash
# Check version
spring-cli version

# Check available commands
spring-cli help

# Test generation
spring-cli
spring:>new --artifactId test-app --groupId com.test --architecture MVC --output /tmp
```

## Support

For build issues, check:
1. Java/GraalVM version: `java -version`
2. Maven version: `mvn -version`
3. Build logs: `mvn clean package -X`
4. Native image logs: Add `-Dverbose` flag
