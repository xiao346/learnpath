$ErrorActionPreference = 'Stop'

$workspaceRoot = Split-Path -Parent $PSScriptRoot
$redisDirectory = Join-Path $workspaceRoot '.tools\memurai'
$redisExecutable = Join-Path $redisDirectory 'memurai.exe'
$redisCli = Join-Path $redisDirectory 'memurai-cli.exe'
$dataDirectory = Join-Path $workspaceRoot '.data\redis'

if (-not (Test-Path -LiteralPath $redisExecutable)) {
    throw '未找到项目本地 Redis 兼容服务，请先安装 Memurai Developer。'
}

New-Item -ItemType Directory -Force -Path $dataDirectory | Out-Null

try {
    $ping = & $redisCli -h 127.0.0.1 -p 6379 ping 2>$null
    if ($ping -eq 'PONG') {
        Write-Output 'Redis 已在 127.0.0.1:6379 运行。'
        exit 0
    }
} catch {
    # 服务尚未启动，继续启动本地实例。
}

$arguments = @(
    '--bind', '127.0.0.1',
    '--port', '6379',
    '--protected-mode', 'yes',
    '--appendonly', 'yes',
    '--dir', $dataDirectory,
    '--dbfilename', 'learnpath.rdb'
)

Start-Process -FilePath $redisExecutable -ArgumentList $arguments -WorkingDirectory $dataDirectory -WindowStyle Hidden
Start-Sleep -Seconds 1

$result = & $redisCli -h 127.0.0.1 -p 6379 ping
if ($result -ne 'PONG') {
    throw 'Redis 启动失败。'
}

Write-Output 'Redis 已启动：127.0.0.1:6379'
