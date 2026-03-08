@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion
title 智能停车场管理系统 - 停止服务
color 0C

echo.
echo   ━━━ 智能停车场管理系统 - 停止服务 ━━━
echo.

set "STOPPED=0"

:: 1. 通过端口号停止服务
for %%P in (8088 5173 5174) do (
    for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":%%P " ^| findstr "LISTENING"') do (
        taskkill /PID %%a /F >nul 2>&1
        if not errorlevel 1 (
            echo   [√] 停止端口 %%P 上的进程 ^(PID: %%a^)
            set /a STOPPED+=1
        )
    )
)

:: 2. 通过窗口标题关闭服务窗口
taskkill /FI "WINDOWTITLE eq [后端]*" /F >nul 2>&1
if not errorlevel 1 (
    echo   [√] 关闭后端服务窗口
    set /a STOPPED+=1
)

taskkill /FI "WINDOWTITLE eq [管理端]*" /F >nul 2>&1
if not errorlevel 1 (
    echo   [√] 关闭管理端服务窗口
    set /a STOPPED+=1
)

taskkill /FI "WINDOWTITLE eq [用户端]*" /F >nul 2>&1
if not errorlevel 1 (
    echo   [√] 关闭用户端服务窗口
    set /a STOPPED+=1
)

:: 3. 清理可能残留的 Java/Node 进程（可选，仅针对项目端口）
:: 已在第 1 步通过端口处理

echo.
if !STOPPED! GTR 0 (
    echo   所有服务已停止 √
) else (
    echo   没有正在运行的服务
)
echo.
timeout /t 3 >nul
