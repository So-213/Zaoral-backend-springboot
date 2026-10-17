<#
.SYNOPSIS
  Spring Boot が接続するDBのホストを application.yml を書き換えて切り替える。

.DESCRIPTION
  src/main/resources/application.yml の
      url: ${DATABASE_URL:jdbc:postgresql://<host>:5432/zaoral}
  のホスト部分だけを置換する。ポート・DB名・ユーザー・パスワードは変更しない。

  DBコンテナ(docker compose up db pgadmin)を別マシンで動かす場合に、
  そのマシンのIPアドレスを指定して使う。

.EXAMPLE
  # 現在の設定を表示
  ./scripts/set-db-host.ps1 -Show

.EXAMPLE
  # 別マシンのDBに向ける
  ./scripts/set-db-host.ps1 192.168.0.219

.EXAMPLE
  # 同一マシンに戻す
  ./scripts/set-db-host.ps1 localhost

.EXAMPLE
  # 書き換えずに差分だけ確認 / 疎通確認
  ./scripts/set-db-host.ps1 192.168.0.219 -DryRun
  ./scripts/set-db-host.ps1 -Test 192.168.0.219
#>
[CmdletBinding()]
param(
    # DBのホスト。IPアドレス または localhost。
    [Parameter(Position = 0)]
    [string]$DbHost,

    # 現在の設定を表示して終了。
    [switch]$Show,

    # 書き換え内容を表示するだけでファイルは変更しない。
    [switch]$DryRun,

    # .bak を作らない。
    [switch]$NoBackup,

    # 指定ホストの 5432 / 5050 に繋がるか確認して終了。
    [string]$Test
)

$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$ymlPath  = Join-Path $repoRoot 'src\main\resources\application.yml'
$pattern  = '(jdbc:postgresql://)([^:/\s]+)(:\d+/)'

function Get-FileText {
    param([string]$Path)
    return [System.IO.File]::ReadAllText($Path, [System.Text.UTF8Encoding]::new($false))
}

function Set-FileText {
    param([string]$Path, [string]$Text)
    [System.IO.File]::WriteAllText($Path, $Text, [System.Text.UTF8Encoding]::new($false))
}

function Show-Current {
    if (-not (Test-Path $ymlPath)) {
        Write-Host "application.yml が見つからない ($ymlPath)" -ForegroundColor Red
        return
    }

    $text  = Get-FileText $ymlPath
    $lines = $text -split "`r?`n"
    $hit   = $false

    Write-Host ''
    Write-Host '現在のDB接続先' -ForegroundColor Cyan
    Write-Host '------------------------------------------------------------'
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i].TrimStart().StartsWith('#')) { continue }
        $m = [regex]::Match($lines[$i], $pattern)
        if (-not $m.Success) { continue }
        $hit = $true
        Write-Host ("  host : {0}" -f $m.Groups[2].Value) -ForegroundColor White
        Write-Host ("  L{0}   {1}" -f ($i + 1), $lines[$i].Trim()) -ForegroundColor DarkGray
    }
    if (-not $hit) {
        Write-Host '  jdbc:postgresql:// の行が見つからない' -ForegroundColor DarkYellow
    }

    # 環境変数が設定されていると application.yml の既定値より優先される。
    if ($env:DATABASE_URL) {
        Write-Host ''
        Write-Host '  [!] 環境変数 DATABASE_URL が設定されています。' -ForegroundColor Yellow
        Write-Host ("      {0}" -f $env:DATABASE_URL) -ForegroundColor Yellow
        Write-Host '      こちらが application.yml の既定値より優先されます。' -ForegroundColor Yellow
    }
    Write-Host ''
}

function Test-Ports {
    param([string]$TargetHost)

    Write-Host ''
    Write-Host "$TargetHost への疎通確認" -ForegroundColor Cyan
    Write-Host '------------------------------------------------------------'
    $ports = @(
        @{ Port = 5432; Name = 'PostgreSQL' },
        @{ Port = 5050; Name = 'pgAdmin' }
    )
    foreach ($p in $ports) {
        $ok = $false
        try {
            $r  = Test-NetConnection -ComputerName $TargetHost -Port $p.Port -WarningAction SilentlyContinue
            $ok = $r.TcpTestSucceeded
        } catch {
            $ok = $false
        }
        if ($ok) {
            Write-Host ("  OK   {0,-5} {1}" -f $p.Port, $p.Name) -ForegroundColor Green
        } else {
            Write-Host ("  NG   {0,-5} {1}" -f $p.Port, $p.Name) -ForegroundColor Red
        }
    }
    Write-Host ''
    Write-Host '5432 が NG の場合、DBマシン側で受信許可を追加する(管理者権限のPowerShell):' -ForegroundColor DarkYellow
    Write-Host '  New-NetFirewallRule -DisplayName "Zaoral dev postgres" -Direction Inbound -Protocol TCP -LocalPort 5432 -Action Allow' -ForegroundColor DarkGray
    Write-Host '  New-NetFirewallRule -DisplayName "Zaoral dev pgadmin"  -Direction Inbound -Protocol TCP -LocalPort 5050 -Action Allow' -ForegroundColor DarkGray
    Write-Host ''
}

# ---- 実行 ---------------------------------------------------------------------

if ($Test) {
    Test-Ports -TargetHost $Test
    return
}

if ($Show -or -not $DbHost) {
    if (-not $DbHost -and -not $Show) {
        Write-Host ''
        Write-Host '使い方:' -ForegroundColor Cyan
        Write-Host '  ./scripts/set-db-host.ps1 -Show              現在の設定を表示'
        Write-Host '  ./scripts/set-db-host.ps1 192.168.0.219      別マシンのDBに向ける'
        Write-Host '  ./scripts/set-db-host.ps1 localhost          同一マシンに戻す'
        Write-Host '  ./scripts/set-db-host.ps1 -Test 192.168.0.219 疎通確認'
    }
    Show-Current
    return
}

if (-not (Test-Path $ymlPath)) {
    Write-Host "application.yml が見つからない ($ymlPath)" -ForegroundColor Red
    return
}

$text    = Get-FileText $ymlPath
if ($text -match "`r`n") { $nl = "`r`n" } else { $nl = "`n" }
$lines   = $text -split "`r?`n"
$changed = 0

Write-Host ''
if ($DryRun) { Write-Host '[DryRun] ファイルは変更しない' -ForegroundColor Cyan }
Write-Host '書き換え' -ForegroundColor Cyan
Write-Host '------------------------------------------------------------'

for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    if ($line.TrimStart().StartsWith('#')) { continue }
    $m = [regex]::Match($line, $pattern)
    if (-not $m.Success) { continue }

    $old = $m.Groups[2].Value
    if ($old -eq $DbHost) {
        Write-Host "  [=] L$($i + 1) 既に $DbHost" -ForegroundColor DarkGray
        continue
    }

    $newLine   = $line.Remove($m.Groups[2].Index, $old.Length).Insert($m.Groups[2].Index, $DbHost)
    $lines[$i] = $newLine
    $changed++
    Write-Host "  [*] L$($i + 1) $old -> $DbHost" -ForegroundColor Green
    Write-Host "       - $($line.Trim())"    -ForegroundColor DarkGray
    Write-Host "       + $($newLine.Trim())" -ForegroundColor DarkGray
}

if ($changed -eq 0) {
    Write-Host '  変更なし' -ForegroundColor DarkGray
    Show-Current
    return
}

if ($DryRun) {
    Write-Host "  [dry] $changed 行が対象(未書き込み)" -ForegroundColor Cyan
    return
}

if (-not $NoBackup) {
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    Copy-Item $ymlPath "$ymlPath.bak-$stamp"
}
Set-FileText $ymlPath ($lines -join $nl)

Show-Current

Write-Host '次にやること' -ForegroundColor Cyan
Write-Host '  1. Spring Boot を再起動する'
Write-Host '  2. 繋がらない場合は DBマシン側のファイアウォール(5432)を確認:'
Write-Host "     ./scripts/set-db-host.ps1 -Test $DbHost"
Write-Host ''
