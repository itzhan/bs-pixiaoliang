@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion
title 智能停车场管理系统 - 启动中
color 0F

:: ---- 配置 ----
set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=smart_parking"
set "DB_USER=root"
set "DB_PASS=root"
set "BACKEND_PORT=8088"
set "ADMIN_PORT=5173"
set "FRONTEND_PORT=5174"
set "PROJECT_DIR=%~dp0"
set "SQL_DIR=%PROJECT_DIR%parking-backend\src\main\resources\sql"

:: ---- Banner ----
echo.
echo   ╔════════════════════════════════════════════════════════╗
echo   ║                                                        ║
echo   ║        智 能 停 车 场 管 理 系 统                      ║
echo   ║        Smart Parking Management System                 ║
echo   ║                                                        ║
echo   ╚════════════════════════════════════════════════════════╝
echo.

:: ============================================================
:: 第 1 步: 环境检查
:: ============================================================
echo ━━━ [1/6] 检查基础环境 ━━━
echo.

where java >nul 2>&1
if errorlevel 1 (
    echo   [✗] 未安装 Java，请安装 JDK 17+
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do echo   [√] %%i

where mvn >nul 2>&1
if errorlevel 1 (
    echo   [✗] 未安装 Maven，请安装 Maven 3.9+
    pause
    exit /b 1
)
echo   [√] Maven 已安装

where node >nul 2>&1
if errorlevel 1 (
    echo   [✗] 未安装 Node.js，请安装 Node.js 18+
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('node --version') do echo   [√] Node.js %%i

where npm >nul 2>&1
if errorlevel 1 (
    echo   [✗] 未安装 npm
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('npm --version') do echo   [√] npm %%i

where mysql >nul 2>&1
if errorlevel 1 (
    echo   [✗] 未安装 MySQL Client
    pause
    exit /b 1
)
echo   [√] MySQL Client 已安装

echo.
echo   [√] 环境检查通过
echo.

:: ============================================================
:: 第 2 步: MySQL 检查
:: ============================================================
echo ━━━ [2/6] 检查 MySQL 服务 ━━━
echo.

mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo   [!] MySQL 未运行，尝试启动...
    net start mysql >nul 2>&1
    timeout /t 5 /nobreak >nul
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 -e "SELECT 1" >nul 2>&1
    if errorlevel 1 (
        echo   [✗] MySQL 启动失败，请手动启动后重试
        pause
        exit /b 1
    )
)
echo   [√] MySQL 服务正在运行

:: 检查数据库是否存在
set "DB_FOUND="
for /f "tokens=*" %%a in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 -e "SHOW DATABASES LIKE '%DB_NAME%'" --skip-column-names 2^>nul') do set "DB_FOUND=%%a"

if "%DB_FOUND%"=="" (
    echo   [!] 数据库 %DB_NAME% 不存在，正在创建...
    if not exist "%SQL_DIR%\init.sql" (
        echo   [✗] 找不到 %SQL_DIR%\init.sql
        pause
        exit /b 1
    )
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 < "%SQL_DIR%\init.sql"
    echo   [√] 数据库结构导入完成 (init.sql^)
    if exist "%SQL_DIR%\data.sql" (
        mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB_NAME% < "%SQL_DIR%\data.sql"
        echo   [√] 测试数据导入完成 (data.sql^)
    )
) else (
    echo   [√] 数据库 %DB_NAME% 已存在
    :: 检查是否有数据
    set "USER_COUNT=0"
    for /f "tokens=*" %%a in ('mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB_NAME% -e "SELECT COUNT(*) FROM sys_user" --skip-column-names 2^>nul') do set "USER_COUNT=%%a"
    if "!USER_COUNT!"=="0" (
        echo   [!] sys_user 表为空，正在导入测试数据...
        if exist "%SQL_DIR%\data.sql" (
            mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB_NAME% < "%SQL_DIR%\data.sql"
            echo   [√] 测试数据导入完成
        )
    ) else (
        echo   [√] 数据库已有 !USER_COUNT! 条用户数据
    )
)
echo.

:: ============================================================
:: 第 3 步: 依赖检查
:: ============================================================
echo ━━━ [3/6] 检查项目依赖 ━━━
echo.

:: 后端编译
if not exist "%PROJECT_DIR%parking-backend\target\classes" (
    echo   [!] 后端未编译，正在编译（首次较慢）...
    cd /d "%PROJECT_DIR%parking-backend"
    call mvn compile -q
    if errorlevel 1 (
        echo   [✗] 后端编译失败
        pause
        exit /b 1
    )
    echo   [√] 后端编译完成
) else (
    echo   [√] 后端已编译
)

:: 管理端依赖
if not exist "%PROJECT_DIR%parking-admin\node_modules" (
    echo   [!] 管理端依赖未安装，正在安装...
    cd /d "%PROJECT_DIR%parking-admin"
    call npm install
    echo   [√] 管理端依赖安装完成
) else (
    echo   [√] 管理端依赖就绪
)

:: 用户端依赖
if not exist "%PROJECT_DIR%parking-frontend\node_modules" (
    echo   [!] 用户端依赖未安装，正在安装...
    cd /d "%PROJECT_DIR%parking-frontend"
    call npm install
    echo   [√] 用户端依赖安装完成
) else (
    echo   [√] 用户端依赖就绪
)
echo.

:: ============================================================
:: 第 4 步: 端口检查
:: ============================================================
echo ━━━ [4/6] 检查端口占用 ━━━
echo.

for %%P in (%BACKEND_PORT% %ADMIN_PORT% %FRONTEND_PORT%) do (
    netstat -aon 2>nul | findstr ":%%P " | findstr "LISTENING" >nul 2>&1
    if not errorlevel 1 (
        echo   [!] 端口 %%P 被占用，尝试释放...
        for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%%P " ^| findstr "LISTENING"') do (
            taskkill /PID %%a /F >nul 2>&1
        )
        timeout /t 1 /nobreak >nul
    )
)
echo   [√] 端口检查通过 (%BACKEND_PORT% / %ADMIN_PORT% / %FRONTEND_PORT%^)
echo.

:: ============================================================
:: 第 5 步: 启动服务（独立窗口）
:: ============================================================
echo ━━━ [5/6] 启动服务 ━━━
echo.

:: 后端 - 红色窗口
start "[后端] Spring Boot :8088" /D "%PROJECT_DIR%parking-backend" cmd /k "title [后端] Spring Boot :8088 && color 0C && echo 正在启动后端服务... && mvn spring-boot:run"
echo   [后端]   Spring Boot 启动中... (端口: %BACKEND_PORT%^)

:: 管理端 - 绿色窗口
start "[管理端] Vue Admin :5173" /D "%PROJECT_DIR%parking-admin" cmd /k "title [管理端] Vue Admin :5173 && color 0A && echo 正在启动管理端... && npm run dev"
echo   [管理端] Vue + Ant Design 启动中... (端口: %ADMIN_PORT%^)

:: 用户端 - 蓝色窗口
start "[用户端] Vue Frontend :5174" /D "%PROJECT_DIR%parking-frontend" cmd /k "title [用户端] Vue Frontend :5174 && color 09 && echo 正在启动用户端... && npm run dev"
echo   [用户端] Vue + Naive UI 启动中... (端口: %FRONTEND_PORT%^)

echo.

:: ============================================================
:: 第 6 步: 等待就绪
:: ============================================================
echo ━━━ [6/6] 等待服务就绪 ━━━
echo.
echo   等待后端启动（最长 120 秒）...

set "WAIT_COUNT=0"
:wait_loop
timeout /t 3 /nobreak >nul
set /a WAIT_COUNT+=3

:: 检查后端端口
netstat -aon 2>nul | findstr ":%BACKEND_PORT% " | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo   [√] 后端已就绪 (%WAIT_COUNT%s^)
    goto :wait_done
)

if %WAIT_COUNT% GEQ 120 (
    echo   [!] 后端启动超时，请查看后端窗口日志
    goto :wait_done
)

goto :wait_loop

:wait_done
:: 等几秒让前端也启动
timeout /t 5 /nobreak >nul

:: ---- 显示信息 ----
echo.
echo   ╔════════════════════════════════════════════════════════╗
echo   ║                                                        ║
echo   ║              所 有 服 务 已 启 动 !                    ║
echo   ║                                                        ║
echo   ╠════════════════════════════════════════════════════════╣
echo   ║                                                        ║
echo   ║  [后端]   后端 API:  http://localhost:%BACKEND_PORT%              ║
echo   ║  [管理端] 管理后台:  http://localhost:%ADMIN_PORT%              ║
echo   ║  [用户端] 用户前台:  http://localhost:%FRONTEND_PORT%              ║
echo   ║                                                        ║
echo   ╠════════════════════════════════════════════════════════╣
echo   ║                                                        ║
echo   ║  测试账号:                                              ║
echo   ║    管理员:  admin       / admin123                      ║
echo   ║    操作员:  operator1   / admin123                      ║
echo   ║    用  户:  user1~user10 / 123456                      ║
echo   ║                                                        ║
echo   ╚════════════════════════════════════════════════════════╝
echo.
echo   提示: 关闭此窗口不影响服务运行
echo   停止服务请运行: stop.bat
echo.
pause
