# Minecraft Automatic Sorter Mod - Setup Complete ✓

## What Was Done

### 1. Java Configuration

- **Issue**: The project requires Java 21, but Gradle was using Java 18
- **Solution**: Downloaded and installed OpenJDK 21.0.2 to `~/.jdk/jdk-21.0.2`
- **Configuration**: Updated both global and project Gradle properties to use Java 21

### 2. Build Configuration

- Updated `gradle.properties` with Java 21 path:
  ```properties
  org.gradle.java.home=C:\\Users\\joeyr\\.jdk\\jdk-21.0.2
  ```

### 3. Build System

- Gradle version: 8.11.1
- Fabric Loom: 1.9.2
- Minecraft version: 1.21.1
- Fabric API: 0.115.3+1.21.1

## How to Build

Run the build command:

```bash
./gradlew clean build
```

The built mod jar will be located at:

```
build/libs/automaticsorter-1.3.0-1.21.1.jar
```

## How to Run/Test

### Run the Minecraft client with your mod:

```bash
./gradlew runClient
```

### Run the server:

```bash
./gradlew runServer
```

### Generate data:

```bash
./gradlew runDatagen
```

## Project Structure

- **Source code**: `src/main/java/cz/lukesmith/automaticsorter/`
- **Resources**: `src/main/resources/`
- **Build output**: `build/libs/`
- **Mod metadata**: `src/main/resources/fabric.mod.json`

## Additional Commands

### Clean build files:

```bash
./gradlew clean
```

### Generate sources (decompiled Minecraft):

```bash
./gradlew genSources
```

### View all available tasks:

```bash
./gradlew tasks --all
```

## Notes

- The project targets Java 17 for compilation (as per build.gradle)
- The mod is compatible with Minecraft 1.21.1
- Fabric Loader version: 0.16.10
- Includes dependency: `expandedstorage-fabric-1.21.3-15.1.2.jar`

## Troubleshooting

If you encounter Java version issues:

1. Ensure Java 21 is installed at `C:\Users\joeyr\.jdk\jdk-21.0.2`
2. Stop all Gradle daemons: `./gradlew --stop`
3. Try building again: `./gradlew clean build`

## Success!

✓ Build working
✓ Run client working
✓ All dependencies resolved
✓ Mod jar generated successfully
