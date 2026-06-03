$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$runtimeDir = Join-Path $projectRoot "build\runtime"

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
  $javaCommand = Get-Command java -ErrorAction SilentlyContinue
  if ($javaCommand) {
    $javaHome = Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
  }
}

if (-not $javaHome) {
  throw "Java was not found. Install JDK 21 or set JAVA_HOME before building the desktop package."
}

$jlink = Join-Path $javaHome "bin\jlink.exe"
if (-not (Test-Path $jlink)) {
  throw "jlink was not found: $jlink. Use a JDK, not a JRE, to build the desktop package."
}

$jmods = Join-Path $javaHome "jmods"
if (-not (Test-Path $jmods)) {
  throw "JDK jmods directory was not found: $jmods."
}

if (Test-Path $runtimeDir) {
  Remove-Item -LiteralPath $runtimeDir -Recurse -Force
}

$modules = @(
  "java.base",
  "java.compiler",
  "java.datatransfer",
  "java.desktop",
  "java.instrument",
  "java.logging",
  "java.management",
  "java.management.rmi",
  "java.naming",
  "java.net.http",
  "java.prefs",
  "java.rmi",
  "java.scripting",
  "java.security.jgss",
  "java.security.sasl",
  "java.sql",
  "java.transaction.xa",
  "java.xml",
  "java.xml.crypto",
  "jdk.charsets",
  "jdk.crypto.ec",
  "jdk.jfr",
  "jdk.management",
  "jdk.naming.dns",
  "jdk.unsupported",
  "jdk.zipfs"
) -join ","

& $jlink `
  --module-path $jmods `
  --add-modules $modules `
  --output $runtimeDir `
  --strip-debug `
  --no-header-files `
  --no-man-pages `
  --compress=zip-6

Write-Output "Bundled Java runtime generated: $runtimeDir"
