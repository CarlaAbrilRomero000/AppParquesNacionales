# ============================================================
#  Configura tu SQL Server LOCAL (instancia por defecto MSSQLSERVER)
#  para que la aplicacion Spring Boot pueda conectarse desde IntelliJ.
#
#  Hace 4 cosas:
#    1) Habilita el protocolo TCP/IP en el puerto 1433
#    2) Activa la autenticacion mixta (Windows + SQL)
#    3) Reinicia el servicio MSSQLSERVER para aplicar los cambios
#    4) Habilita el login 'sa' con la contrasena que usa la app
#
#  COMO EJECUTARLO:
#    Clic derecho sobre este archivo  ->  "Ejecutar con PowerShell"
#    (si pide permisos de administrador, aceptar)
#  o desde una PowerShell ABIERTA COMO ADMINISTRADOR:
#    powershell -ExecutionPolicy Bypass -File .\setup-sqlserver-local.ps1
# ============================================================

$ErrorActionPreference = 'Stop'

# --- 0) Verificar que se ejecuta como Administrador ---
$principal = New-Object Security.Principal.WindowsPrincipal(
    [Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "ERROR: Este script debe ejecutarse como ADMINISTRADOR." -ForegroundColor Red
    Write-Host "Cerra esta ventana, hace clic derecho en el archivo y elegi 'Ejecutar con PowerShell'." -ForegroundColor Yellow
    Read-Host "Presiona Enter para salir"
    exit 1
}

# Instancia por defecto detectada en esta PC (SQL Server 2022 = MSSQL16)
$instancia = 'MSSQL16.MSSQLSERVER'
$claveBase = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\$instancia"

if (-not (Test-Path $claveBase)) {
    Write-Host "No se encontro la instancia $instancia en el registro." -ForegroundColor Red
    Write-Host "Revisa el nombre de tu instancia en SQL Server Configuration Manager." -ForegroundColor Yellow
    Read-Host "Presiona Enter para salir"
    exit 1
}

# --- 1) Habilitar TCP/IP en el puerto 1433 ---
$tcp = "$claveBase\MSSQLServer\SuperSocketNetLib\Tcp"
Set-ItemProperty -Path $tcp            -Name 'Enabled'          -Value 1
Set-ItemProperty -Path "$tcp\IPAll"    -Name 'TcpPort'          -Value '1433'
Set-ItemProperty -Path "$tcp\IPAll"    -Name 'TcpDynamicPorts'  -Value ''
Write-Host "[1/4] TCP/IP habilitado en el puerto 1433." -ForegroundColor Green

# --- 2) Activar autenticacion mixta (Windows + SQL) ---
Set-ItemProperty -Path "$claveBase\MSSQLServer" -Name 'LoginMode' -Value 2
Write-Host "[2/4] Autenticacion mixta (Windows + SQL) activada." -ForegroundColor Green

# --- 3) Reiniciar el servicio para aplicar los cambios ---
Write-Host "[3/4] Reiniciando el servicio MSSQLSERVER..." -ForegroundColor Cyan
Restart-Service -Name 'MSSQLSERVER' -Force
Write-Host "[3/4] Servicio reiniciado." -ForegroundColor Green

# --- 4) Habilitar 'sa' y fijar la contrasena de la app ---
# Se conecta con autenticacion de Windows (tu usuario es administrador de SQL).
$comando = "ALTER LOGIN [sa] ENABLE; ALTER LOGIN [sa] WITH PASSWORD = N'Parques2024!';"
sqlcmd -S localhost -E -C -Q $comando
if ($LASTEXITCODE -eq 0) {
    Write-Host "[4/4] Login 'sa' habilitado con la contrasena 'Parques2024!'." -ForegroundColor Green
} else {
    Write-Host "[4/4] No se pudo configurar 'sa' automaticamente (codigo $LASTEXITCODE)." -ForegroundColor Yellow
    Write-Host "      Podes hacerlo manualmente en SSMS con:" -ForegroundColor Yellow
    Write-Host "      ALTER LOGIN [sa] ENABLE; ALTER LOGIN [sa] WITH PASSWORD = N'Parques2024!';" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " LISTO. Tu SQL Server local ya acepta conexiones de la app." -ForegroundColor Cyan
Write-Host " Ahora, desde IntelliJ, ejecuta la clase ParquesApplication" -ForegroundColor Cyan
Write-Host " y abri en el navegador:  http://localhost:8080" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Read-Host "Presiona Enter para salir"
