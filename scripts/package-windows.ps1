$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw "Instala Maven y añádelo al PATH." }
if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) { throw "Instala JDK 21 completo y añade su bin al PATH." }
# Salida nueva: no sobrescribe una distribución que puedas estar utilizando.
$output = Join-Path "dist" ("build-" + (Get-Date -Format "yyyyMMdd-HHmmss"))
& mvn clean verify
if ($LASTEXITCODE -ne 0) { throw "Falló la compilación o las pruebas." }
New-Item -ItemType Directory -Force "target/package-input" | Out-Null
Copy-Item "target/todo-list-1.0.0.jar" "target/package-input/"
Copy-Item "target/lib/*.jar" "target/package-input/"
& jpackage --type app-image --name ToDoList --app-version 1.0.0 --input target/package-input --main-jar todo-list-1.0.0.jar --main-class todolist.Launcher --dest $output --add-modules java.se,jdk.crypto.ec,jdk.unsupported
if ($LASTEXITCODE -ne 0) { throw "Falló jpackage." }
Write-Host "Aplicación creada en $output/ToDoList/ToDoList.exe. Conserva TODA la carpeta."
