#!/bin/bash
# ============================================================
# 智能停车场管理系统 - 一键启动脚本 (Mac/Linux)
# ============================================================

# ---- 配置 ----
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="smart_parking"
DB_USER="root"
DB_PASS="root"
BACKEND_PORT=8088
ADMIN_PORT=5173
FRONTEND_PORT=5174
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$PROJECT_DIR/.logs"
PID_DIR="$PROJECT_DIR/.pids"
SQL_DIR="$PROJECT_DIR/parking-backend/src/main/resources/sql"

# ---- 颜色 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m'

# ---- 辅助函数 ----
info()    { echo -e "${CYAN}[信息]${NC} $*"; }
success() { echo -e "${GREEN}[✓]${NC} $*"; }
warn()    { echo -e "${YELLOW}[!]${NC} $*"; }
error()   { echo -e "${RED}[✗]${NC} $*"; }

banner() {
  echo ""
  echo -e "${CYAN}╔════════════════════════════════════════════════════════╗${NC}"
  echo -e "${CYAN}║                                                        ║${NC}"
  echo -e "${CYAN}║${BOLD}${MAGENTA}        智 能 停 车 场 管 理 系 统${NC}${CYAN}                  ║${NC}"
  echo -e "${CYAN}║${NC}           Smart Parking Management System${CYAN}              ║${NC}"
  echo -e "${CYAN}║                                                        ║${NC}"
  echo -e "${CYAN}╚════════════════════════════════════════════════════════╝${NC}"
  echo ""
}

# 检查命令是否存在
check_cmd() {
  local cmd="$1"
  local hint="$2"
  if ! command -v "$cmd" &>/dev/null; then
    error "未安装 ${BOLD}$cmd${NC}，请先安装"
    [ -n "$hint" ] && echo -e "       安装方式: ${YELLOW}$hint${NC}"
    return 1
  fi
  return 0
}

# 检查版本号 (major)
check_version() {
  local cmd="$1"
  local min_major="$2"
  local label="$3"
  local version_str
  version_str=$($cmd --version 2>&1 | head -1)
  local major
  major=$(echo "$version_str" | grep -oE '[0-9]+\.[0-9]+' | head -1 | cut -d. -f1)
  if [ -n "$major" ] && [ "$major" -ge "$min_major" ] 2>/dev/null; then
    success "$label: $version_str"
    return 0
  else
    warn "$label 版本可能不满足要求 (需要 >= $min_major.x): $version_str"
    return 0
  fi
}

# 检查端口是否被占用
check_port() {
  local port=$1
  local name=$2
  if lsof -i ":$port" -sTCP:LISTEN &>/dev/null; then
    warn "端口 ${BOLD}$port${NC} ($name) 已被占用"
    local pid
    pid=$(lsof -ti ":$port" 2>/dev/null | head -1)
    if [ -n "$pid" ]; then
      warn "尝试终止占用端口 $port 的进程 (PID: $pid)..."
      kill -9 $pid 2>/dev/null || true
      sleep 1
      if lsof -i ":$port" -sTCP:LISTEN &>/dev/null; then
        error "无法释放端口 $port，请手动处理"
        return 1
      fi
      success "端口 $port 已释放"
    fi
  fi
  return 0
}

# 等待端口就绪
wait_port() {
  local port=$1
  local name=$2
  local timeout=$3
  local elapsed=0
  while [ $elapsed -lt $timeout ]; do
    if lsof -i ":$port" -sTCP:LISTEN &>/dev/null 2>&1; then
      return 0
    fi
    sleep 2
    elapsed=$((elapsed + 2))
  done
  return 1
}

# ---- Ctrl+C 信号处理 ----
cleanup() {
  echo ""
  warn "收到中断信号，正在停止所有服务..."
  if [ -f "$PROJECT_DIR/stop.sh" ]; then
    bash "$PROJECT_DIR/stop.sh"
  fi
  exit 0
}
trap cleanup INT TERM

# ============================================================
# 主流程
# ============================================================
banner

mkdir -p "$LOG_DIR" "$PID_DIR"

# ------ 第 1 步: 环境检查 ------
echo -e "${BOLD}${YELLOW}━━━ [1/6] 检查基础环境 ━━━${NC}"

MISSING=0

if check_cmd java "brew install openjdk@17 / sdkman"; then
  check_version java 17 "Java"
else
  MISSING=1
fi

if check_cmd mvn "brew install maven"; then
  check_version mvn 3 "Maven"
else
  MISSING=1
fi

if check_cmd node "brew install node"; then
  check_version node 18 "Node.js"
else
  MISSING=1
fi

if ! check_cmd npm "随 Node.js 一起安装"; then
  MISSING=1
fi

if ! check_cmd mysql "brew install mysql"; then
  MISSING=1
fi

if [ $MISSING -eq 1 ]; then
  error "缺少必要环境，请先安装上述工具后重试"
  exit 1
fi

success "基础环境检查通过"

# ------ 第 2 步: MySQL 检查 ------
echo ""
echo -e "${BOLD}${YELLOW}━━━ [2/6] 检查 MySQL 服务 ━━━${NC}"

if ! mysqladmin ping -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" --silent 2>/dev/null; then
  warn "MySQL 未运行，尝试自动启动..."
  if command -v brew &>/dev/null; then
    brew services start mysql 2>/dev/null && info "通过 brew services 启动 MySQL"
  elif command -v systemctl &>/dev/null; then
    sudo systemctl start mysql 2>/dev/null && info "通过 systemctl 启动 MySQL"
  elif command -v mysqld_safe &>/dev/null; then
    mysqld_safe &>/dev/null &
    info "通过 mysqld_safe 启动 MySQL"
  fi
  sleep 5
  if ! mysqladmin ping -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" --silent 2>/dev/null; then
    error "MySQL 启动失败，请手动启动后重试"
    exit 1
  fi
fi
success "MySQL 服务正在运行"

# 检查数据库是否存在
DB_EXISTS=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
  --default-character-set=utf8mb4 \
  -e "SHOW DATABASES LIKE '$DB_NAME'" --skip-column-names 2>/dev/null)

if [ -z "$DB_EXISTS" ]; then
  warn "数据库 ${BOLD}$DB_NAME${NC} 不存在，正在创建并导入..."

  if [ ! -f "$SQL_DIR/init.sql" ]; then
    error "找不到 SQL 初始化文件: $SQL_DIR/init.sql"
    exit 1
  fi

  mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
    --default-character-set=utf8mb4 < "$SQL_DIR/init.sql"
  success "数据库结构导入完成 (init.sql)"

  if [ -f "$SQL_DIR/data.sql" ]; then
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
      --default-character-set=utf8mb4 "$DB_NAME" < "$SQL_DIR/data.sql"
    success "测试数据导入完成 (data.sql)"
  fi
else
  success "数据库 ${BOLD}$DB_NAME${NC} 已存在"

  # 检查是否有数据
  USER_COUNT=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
    --default-character-set=utf8mb4 "$DB_NAME" \
    -e "SELECT COUNT(*) FROM sys_user" --skip-column-names 2>/dev/null || echo "0")

  if [ "$USER_COUNT" = "0" ] || [ -z "$USER_COUNT" ]; then
    warn "sys_user 表为空，正在导入测试数据..."
    if [ -f "$SQL_DIR/data.sql" ]; then
      mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
        --default-character-set=utf8mb4 "$DB_NAME" < "$SQL_DIR/data.sql"
      success "测试数据导入完成 (data.sql)"
    else
      warn "找不到 data.sql，跳过数据导入"
    fi
  else
    success "数据库已有 ${USER_COUNT} 条用户数据"
  fi
fi

# ------ 第 3 步: 依赖检查 ------
echo ""
echo -e "${BOLD}${YELLOW}━━━ [3/6] 检查项目依赖 ━━━${NC}"

# 后端编译
if [ ! -d "$PROJECT_DIR/parking-backend/target/classes" ]; then
  warn "后端未编译，正在编译（首次可能较慢）..."
  cd "$PROJECT_DIR/parking-backend"
  mvn compile -q 2>&1 | tail -5
  if [ $? -eq 0 ]; then
    success "后端编译完成"
  else
    error "后端编译失败，请检查日志"
    exit 1
  fi
else
  success "后端已编译"
fi

# 管理端依赖
if [ ! -d "$PROJECT_DIR/parking-admin/node_modules" ]; then
  warn "管理端依赖未安装，正在安装..."
  cd "$PROJECT_DIR/parking-admin"
  npm install 2>&1 | tail -3
  success "管理端依赖安装完成"
else
  success "管理端依赖就绪"
fi

# 用户端依赖
if [ ! -d "$PROJECT_DIR/parking-frontend/node_modules" ]; then
  warn "用户端依赖未安装，正在安装..."
  cd "$PROJECT_DIR/parking-frontend"
  npm install 2>&1 | tail -3
  success "用户端依赖安装完成"
else
  success "用户端依赖就绪"
fi

# ------ 第 4 步: 端口检查 ------
echo ""
echo -e "${BOLD}${YELLOW}━━━ [4/6] 检查端口占用 ━━━${NC}"

check_port $BACKEND_PORT  "后端API"  || exit 1
check_port $ADMIN_PORT    "管理端"   || exit 1
check_port $FRONTEND_PORT "用户端"   || exit 1
success "端口检查通过 ($BACKEND_PORT / $ADMIN_PORT / $FRONTEND_PORT)"

# ------ 第 5 步: 启动服务 ------
echo ""
echo -e "${BOLD}${YELLOW}━━━ [5/6] 启动服务 ━━━${NC}"

# 启动后端
cd "$PROJECT_DIR/parking-backend"
nohup mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > "$PID_DIR/backend.pid"
echo -e "  ${RED}[后端]${NC}   Spring Boot 启动中... (PID: $BACKEND_PID)"

# 启动管理端
cd "$PROJECT_DIR/parking-admin"
nohup npm run dev > "$LOG_DIR/admin.log" 2>&1 &
ADMIN_PID=$!
echo $ADMIN_PID > "$PID_DIR/admin.pid"
echo -e "  ${GREEN}[管理端]${NC} Vue + Ant Design 启动中... (PID: $ADMIN_PID)"

# 启动用户端
cd "$PROJECT_DIR/parking-frontend"
nohup npm run dev > "$LOG_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > "$PID_DIR/frontend.pid"
echo -e "  ${BLUE}[用户端]${NC} Vue + Naive UI 启动中... (PID: $FRONTEND_PID)"

# ------ 第 6 步: 等待就绪 ------
echo ""
echo -e "${BOLD}${YELLOW}━━━ [6/6] 等待服务就绪 ━━━${NC}"

# 等待后端
echo -ne "  ${RED}[后端]${NC}   等待端口 $BACKEND_PORT 就绪"
if wait_port $BACKEND_PORT "后端" 120; then
  echo -e " ${GREEN}✓${NC}"
else
  echo -e " ${RED}✗ 超时${NC}"
  warn "后端启动可能较慢，请查看日志: $LOG_DIR/backend.log"
fi

# 等待管理端
echo -ne "  ${GREEN}[管理端]${NC} 等待端口 $ADMIN_PORT 就绪"
if wait_port $ADMIN_PORT "管理端" 60; then
  echo -e " ${GREEN}✓${NC}"
else
  echo -e " ${RED}✗ 超时${NC}"
  warn "管理端启动可能较慢，请查看日志: $LOG_DIR/admin.log"
fi

# 等待用户端
echo -ne "  ${BLUE}[用户端]${NC} 等待端口 $FRONTEND_PORT 就绪"
if wait_port $FRONTEND_PORT "用户端" 60; then
  echo -e " ${GREEN}✓${NC}"
else
  echo -e " ${RED}✗ 超时${NC}"
  warn "用户端启动可能较慢，请查看日志: $LOG_DIR/frontend.log"
fi

# ---- 最终信息输出 ----
echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                                                        ║${NC}"
echo -e "${GREEN}║${BOLD}              所 有 服 务 已 启 动 !${NC}${GREEN}                    ║${NC}"
echo -e "${GREEN}║                                                        ║${NC}"
echo -e "${GREEN}╠════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║                                                        ║${NC}"
echo -e "${GREEN}║${NC}  ${RED}[后端]${NC}   后端 API:  ${BOLD}http://localhost:${BACKEND_PORT}${NC}            ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${GREEN}[管理端]${NC} 管理后台:  ${BOLD}http://localhost:${ADMIN_PORT}${NC}            ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${BLUE}[用户端]${NC} 用户前台:  ${BOLD}http://localhost:${FRONTEND_PORT}${NC}            ${GREEN}║${NC}"
echo -e "${GREEN}║                                                        ║${NC}"
echo -e "${GREEN}╠════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║                                                        ║${NC}"
echo -e "${GREEN}║${NC}  ${BOLD}测试账号:${NC}                                            ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}    管理员:  admin       / admin123                    ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}    操作员:  operator1   / admin123                    ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}    用  户:  user1~user10 / 123456                    ${GREEN}║${NC}"
echo -e "${GREEN}║                                                        ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  ${CYAN}日志目录:${NC} $LOG_DIR"
echo -e "  ${CYAN}PID目录:${NC}  $PID_DIR"
echo -e "  ${CYAN}停止服务:${NC} ${BOLD}./stop.sh${NC} 或 ${BOLD}Ctrl+C${NC}"
echo ""

# 实时输出日志（前台阻塞，Ctrl+C 触发 cleanup）
tail -f "$LOG_DIR/backend.log" "$LOG_DIR/admin.log" "$LOG_DIR/frontend.log" 2>/dev/null &
TAIL_PID=$!
echo $TAIL_PID > "$PID_DIR/tail.pid"
wait $TAIL_PID
