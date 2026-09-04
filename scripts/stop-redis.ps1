$ErrorActionPreference = 'Stop'

$workspaceRoot = Split-Path -Parent $PSScriptRoot
$redisCli = Join-Path $workspaceRoot '.tools\memurai\memurai-cli.exe'

if (-not (Test-Path -LiteralPath $redisCli)) {
    throw '未找到项目本地 Redis 客户端。'
}

& $redisCli -h 127.0.0.1 -p 6379 shutdown save
Write-Output 'Redis 已停止并保存数据。'
