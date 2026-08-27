$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

function Find-Executable($name) {
    $command = Get-Command $name -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    return $null
}

$maven = Find-Executable "mvn.cmd"
if (-not $maven -and (Test-Path (Join-Path $projectRoot "mvnw.cmd"))) {
    $maven = Join-Path $projectRoot "mvnw.cmd"
}

if (-not $maven) {
    $jetBrainsRoot = Join-Path ${env:ProgramFiles} "JetBrains"
    if (Test-Path $jetBrainsRoot) {
        $maven = Get-ChildItem -Path $jetBrainsRoot -Recurse -Filter "mvn.cmd" -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty FullName
    }
}

if (-not $maven) {
    throw "Maven was not found. Install Maven and add it to PATH, or configure Maven in IntelliJ IDEA."
}

if (-not $env:JAVA_HOME) {
    $java = Get-ChildItem -Path (Join-Path ${env:ProgramFiles} "JetBrains") -Recurse -Filter "java.exe" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($java) {
        $env:JAVA_HOME = Split-Path (Split-Path $java.FullName -Parent) -Parent
    }
}

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    throw "Java was not found. Install a JDK and set the JAVA_HOME environment variable."
}

Write-Host "Tests are running..."
& $maven clean test

if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed. Application will not start." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Tests passed. Starting the application..." -ForegroundColor Green
& $maven spring-boot:run
