#!/bin/bash
# ============================================================
# 智能停车场管理系统 - 一键停止脚本 (Mac/Linux)
# ============================================================

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_DIR="$PROJECT_DIR/.pids"
LOG_DIR="$PROJECT_DIR/.logs"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

echo ""
echo -e "${RED}${BOLD}━━━ 智能停车场管理系统 - 停止服务 ━━━${NC}"
echo ""

STOPPED=0

# 1. 先停止 tail 监控进程
if [ -f "$PID_DIR/tail.pid" ]; then
  TAIL_PID=$(cat "$PID_DIR/tail.pid")
  if kill -0 "$TAIL_PID" 2>/dev/null; then
    kill "$TAIL_PID" 2>/dev/null
    echo -e "  ${GREEN}[✓]${NC} 停止日志监控 (PID: $TAIL_PID)"
    STOPPED=$((STOPPED + 1))
  fi
  rm -f "$PID_DIR/tail.pid"
fi

# 2. 通过 PID 文件停止服务
for SERVICE in backend admin frontend; do
  PID_FILE="$PID_DIR/${SERVICE}.pid"
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
      # 尝试优雅停止，3秒后强制终止
      kill "$PID" 2>/dev/null
      sleep 1
      if kill -0 "$PID" 2>/dev/null; then
        kill -9 "$PID" 2>/dev/null
      fi
      case "$SERVICE" in
        backend)  echo -e "  ${GREEN}[✓]${NC} 停止 ${RED}[后端]${NC}   (PID: $PID)" ;;
        admin)    echo -e "  ${GREEN}[✓]${NC} 停止 ${GREEN}[管理端]${NC} (PID: $PID)" ;;
        frontend) echo -e "  ${GREEN}[✓]${NC} 停止 ${CYAN}[用户端]${NC} (PID: $PID)" ;;
      esac
      STOPPED=$((STOPPED + 1))
    fi
    rm -f "$PID_FILE"
  fi
done

# 3. 通过端口号双重清理（确保不留残余进程）
echo ""
echo -e "  ${YELLOW}[~]${NC} 清理残余端口进程..."
for PORT in 8088 5173 5174; do
  PIDS=$(lsof -ti ":$PORT" 2>/dev/null)
  if [ -n "$PIDS" ]; then
    echo "$PIDS" | xargs kill -9 2>/dev/null
    echo -e "  ${GREEN}[✓]${NC} 清理端口 $PORT 上的残余进程"
    STOPPED=$((STOPPED + 1))
  fi
done

# 4. 清理 PID 目录
rm -rf "$PID_DIR" 2>/dev/null

# 5. 结果输出
echo ""
if [ $STOPPED -gt 0 ]; then
  echo -e "${GREEN}${BOLD}  所有服务已停止 ✓${NC}"
else
  echo -e "${YELLOW}  没有正在运行的服务${NC}"
fi
echo ""
