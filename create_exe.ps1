# Script to create optimized EXE using jlink + jpackage (JDK 21)

# 1. Define Paths
$JAVA_HOME = "C:\Program Files\Java\jdk-21"
$JLINK = "$JAVA_HOME\bin\jlink.exe"
$JPACKAGE = "$JAVA_HOME\bin\jpackage.exe"
$TARGET_DIR = "target"
$LIB_DIR = "$TARGET_DIR\lib"
$MAIN_JAR = "PerpustakaanJava-1.0-SNAPSHOT.jar"
$MAIN_CLASS = "com.mycompany.perpustakaanjava.PerpustakaanJava"
$APP_NAME = "PerpustakaanApp"
$OUTPUT_DIR = "dist_exe"
$RUNTIME_DIR = "$TARGET_DIR\custom-runtime"

# 2. Check for Requirements
if (-not (Test-Path $JLINK)) {
    Write-Error "jlink not found at $JLINK. Please install JDK 21 or update path in script."
    exit 1
}

if (-not (Test-Path $JPACKAGE)) {
    Write-Error "jpackage not found at $JPACKAGE. Please install JDK 21 or update path in script."
    exit 1
}

if (-not (Test-Path "$TARGET_DIR\$MAIN_JAR")) {
    Write-Error "Main JAR not found at $TARGET_DIR\$MAIN_JAR. Please run 'Clean and Build' in NetBeans first!"
    exit 1
}

if (-not (Test-Path $LIB_DIR)) {
    Write-Error "Library directory not found at $LIB_DIR. Please run 'Clean and Build' in NetBeans first!"
    exit 1
}

# 3. Clean previous builds
if (Test-Path $OUTPUT_DIR) {
    Remove-Item -Recurse -Force $OUTPUT_DIR
}

if (Test-Path $RUNTIME_DIR) {
    Remove-Item -Recurse -Force $RUNTIME_DIR
}

# 4. Create custom JRE with jlink (minimal modules)
Write-Host "Creating custom minimal JRE with jlink..."
Write-Host "This will significantly reduce the package size..."

& $JLINK `
  --add-modules java.base,java.desktop,java.sql,java.naming,java.xml,java.prefs,java.logging,jdk.crypto.ec `
  --strip-debug `
  --no-header-files `
  --no-man-pages `
  --compress=2 `
  --output $RUNTIME_DIR

if ($LASTEXITCODE -ne 0) {
    Write-Error "jlink failed!"
    exit 1
}

Write-Host "Custom runtime created successfully at: $RUNTIME_DIR"

# 5. Prepare input directory
Write-Host "Preparing input directory..."
$TEMP_INPUT = "$TARGET_DIR\jpackage_input"
if (Test-Path $TEMP_INPUT) { Remove-Item -Recurse -Force $TEMP_INPUT }
New-Item -ItemType Directory -Force -Path $TEMP_INPUT | Out-Null

# Copy Libs
Copy-Item "$LIB_DIR\*" -Destination $TEMP_INPUT
# Copy Main Jar
Copy-Item "$TARGET_DIR\$MAIN_JAR" -Destination $TEMP_INPUT

# 6. Run jpackage with custom runtime
Write-Host "Running jpackage with custom runtime..."
& $JPACKAGE `
  --type app-image `
  --input $TEMP_INPUT `
  --dest $OUTPUT_DIR `
  --name $APP_NAME `
  --main-jar $MAIN_JAR `
  --main-class $MAIN_CLASS `
  --runtime-image $RUNTIME_DIR

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "============================================"
    Write-Host "Build Successful!"
    Write-Host "============================================"
    Write-Host "Executable is located at:"
    Write-Host "  $PWD\$OUTPUT_DIR\$APP_NAME\$APP_NAME.exe"
    Write-Host ""
    
    # Show size comparison
    $size = (Get-ChildItem -Path "$OUTPUT_DIR\$APP_NAME" -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Host "Total package size: $([math]::Round($size, 2)) MB"
    Write-Host "============================================"
} else {
    Write-Error "Build Failed!"
}
