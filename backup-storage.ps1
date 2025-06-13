<#
.SYNOPSIS
  Создает резервную копию данных из контейнера storage
#>

$containerName = "executive_documentation-storage-1"
$sourcePath = "/usr/share/nginx/html"
$backupDir = "$PSScriptRoot\backups\storage"
$prefix = "storage-backup"

# Проверяем состояние контейнера
if (-not (docker ps -q -f "name=$containerName")) {
    Write-Host "Контейнер $containerName не запущен!" -ForegroundColor Red
    exit 1
}

# Создаем папку для бэкапов
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

# Создаем бэкап
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupFile = "$prefix-$timestamp.tar.gz"
$backupPath = "$backupDir\$backupFile"

# Исправленная строка с выводом
Write-Host ("Создание бэкапа из {0}{1}..." -f $containerName, $sourcePath) -ForegroundColor Cyan

# Вариант 1: Самый надежный способ
docker exec $containerName sh -c "cd $sourcePath && tar -czf /tmp/$backupFile ."
docker cp "${containerName}:/tmp/$backupFile" $backupPath
docker exec $containerName rm "/tmp/$backupFile"

# Проверка результата
if (Test-Path $backupPath) {
    $size = [math]::Round((Get-Item $backupPath).Length / 1MB, 2)
    if ($size -gt 0) {
        Write-Host "Бэкап успешно создан!" -ForegroundColor Green
        Write-Host "Файл: $backupPath"
        Write-Host "Размер: $size MB"

        # Просмотр содержимого архива
        Write-Host "`nСодержимое архива:" -ForegroundColor Cyan
        tar -ztf $backupPath | Select-Object -First 10
        if ((tar -ztf $backupPath | Measure-Object).Count -gt 10) {
            Write-Host ("... и еще {0} файлов" -f ((tar -ztf $backupPath | Measure-Object).Count - 10))
        }
    } else {
        Write-Host "Создан пустой архив! Проверьте путь к данным." -ForegroundColor Red
    }
} else {
    Write-Host "Ошибка: файл бэкапа не был создан" -ForegroundColor Red
}