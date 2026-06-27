<template>
  <div class="chat-page">
    <div class="chat-header">
      <div class="chat-header-left">
        <div class="chat-header-icon">💬</div>
        <div>
          <h3>在线客服</h3>
          <p class="chat-header-sub">有问题随时咨询，我们会尽快回复</p>
        </div>
      </div>
      <div class="connection-status">
        <span class="status-dot" :class="connectionClass"></span>
        <span class="status-text">{{ connectionStatus }}</span>
        <el-button v-if="!isConnected" size="small" type="primary" text @click="reconnect" :loading="reconnecting">重新连接</el-button>
      </div>
    </div>
    <div class="chat-box" ref="chatBox">
      <div v-if="!messages.length && isConnected" class="chat-empty">
        <div class="chat-empty-icon">👋</div>
        <p>你好！有什么可以帮到你的吗？</p>
        <div class="chat-quick-actions">
          <el-button size="small" round @click="sendQuick('我想查询订单状态')">查询订单</el-button>
          <el-button size="small" round @click="sendQuick('我想退换货')">退换货</el-button>
          <el-button size="small" round @click="sendQuick('商品有问题')">商品问题</el-button>
          <el-button size="small" round @click="sendQuick('其他问题')">其他问题</el-button>
        </div>
      </div>
      <div v-if="!isConnected && !messages.length" class="chat-empty">
        <div class="chat-empty-icon">🔌</div>
        <p>正在连接客服系统...</p>
      </div>
      <div v-for="(msg, i) in messages" :key="i" class="msg-wrapper">
        <div v-if="msg.type === 'system'" class="msg-system">
          <span class="system-badge">系统</span> {{ msg.content }}
        </div>
        <div v-else :class="['msg', msg.fromUserId === userId ? 'msg-right' : 'msg-left']">
          <el-avatar v-if="msg.fromUserId !== userId" :size="36" class="msg-avatar" style="background:#67c23a">{{ (msg.fromUsername || '客服').charAt(0) }}</el-avatar>
          <div class="msg-body">
            <div class="msg-user">{{ msg.fromUserId === userId ? '我' : (msg.fromUsername || '客服') }}</div>
            <div class="msg-content">{{ msg.content }}</div>
            <div class="msg-time">{{ formatTime(msg.time || msg.createTime) }}</div>
          </div>
          <el-avatar v-if="msg.fromUserId === userId" :size="36" class="msg-avatar" style="background:#409eff">{{ (msg.fromUsername || username).charAt(0) }}</el-avatar>
        </div>
      </div>
    </div>
    <div class="chat-input">
      <el-input
        v-model="input"
        placeholder="输入消息..."
        @keyup.enter="sendMessage"
        style="flex:1"
        :disabled="!isConnected"
        maxlength="500"
        show-word-limit
        size="large"
      >
        <template #append>
          <el-button @click="sendMessage" :disabled="!isConnected || !input.trim()" type="primary">
            发送
          </el-button>
        </template>
      </el-input>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'

const messages = ref([])
const input = ref('')
const chatBox = ref(null)
const userId = Number(localStorage.getItem('userId') || 0)
const username = localStorage.getItem('username') || ''
const wsState = ref('disconnected')
const reconnecting = ref(false)
let ws = null
let reconnectTimer = null
let reconnectAttempts = 0
const MAX_RECONNECT = 10

const isConnected = computed(() => wsState.value === 'connected')
const connectionStatus = computed(() => {
  const map = { connected: '已连接', connecting: '连接中...', disconnected: '未连接' }
  return map[wsState.value] || '未连接'
})
const connectionClass = computed(() => ({
  'dot-connected': wsState.value === 'connected',
  'dot-connecting': wsState.value === 'connecting',
  'dot-disconnected': wsState.value === 'disconnected'
}))

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  const timeStr = `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  if (isToday) return timeStr
  return `${d.getMonth() + 1}/${d.getDate()} ${timeStr}`
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBox.value) {
      chatBox.value.scrollTop = chatBox.value.scrollHeight
    }
  })
}

const getWsUrl = () => {
  const token = localStorage.getItem('token')
  if (!token) return null
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = location.host
  return `${protocol}//${host}/ws/chat/${token}`
}

const connect = () => {
  const url = getWsUrl()
  if (!url) return

  wsState.value = 'connecting'
  ws = new WebSocket(url)

  ws.onopen = () => {
    wsState.value = 'connected'
    reconnectAttempts = 0
    if (!messages.value.length) {
      messages.value.push({ type: 'system', content: '已连接到客服，请描述您的问题' })
    } else {
      messages.value.push({ type: 'system', content: '连接已恢复' })
    }
    scrollToBottom()
  }

  ws.onmessage = (e) => {
    try {
      const data = JSON.parse(e.data)
      messages.value.push(data)
      scrollToBottom()
    } catch (err) {}
  }

  ws.onclose = () => {
    wsState.value = 'disconnected'
    if (reconnectAttempts < MAX_RECONNECT) {
      const delay = Math.min(5000, 1000 * Math.pow(2, reconnectAttempts))
      reconnectAttempts++
      messages.value.push({ type: 'system', content: `连接已断开，${Math.ceil(delay / 1000)}秒后尝试重连...` })
      scrollToBottom()
      reconnectTimer = setTimeout(connect, delay)
    } else {
      messages.value.push({ type: 'system', content: '连接已断开，请点击"重新连接"按钮' })
      scrollToBottom()
    }
  }

  ws.onerror = () => {
    wsState.value = 'disconnected'
  }
}

const reconnect = () => {
  if (ws) ws.close()
  reconnectAttempts = 0
  reconnecting.value = true
  setTimeout(() => {
    reconnecting.value = false
    connect()
  }, 500)
}

const sendMessage = () => {
  if (!input.value.trim()) return
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.warning('连接未建立，请稍后重试')
    return
  }
  ws.send(JSON.stringify({ content: input.value.trim() }))
  input.value = ''
}

const sendQuick = (text) => {
  input.value = text
  sendMessage()
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  connect()
})

onBeforeUnmount(() => {
  if (reconnectTimer) clearTimeout(reconnectTimer)
  if (ws) ws.close()
})
</script>

<style scoped>
.chat-page {
  max-width: 750px; margin: 0 auto; background: #fff; border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08); overflow: hidden;
}
.chat-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 24px; background: linear-gradient(135deg, #409eff, #337ecc); color: #fff;
}
.chat-header-left { display: flex; align-items: center; gap: 12px; }
.chat-header-icon { font-size: 28px; }
.chat-header h3 { margin: 0; font-size: 18px; }
.chat-header-sub { margin: 2px 0 0; font-size: 12px; opacity: 0.85; }
.connection-status { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.dot-connected { background: #67c23a; box-shadow: 0 0 8px rgba(103,194,58,0.6); }
.dot-connecting { background: #e6a23c; animation: blink 1s infinite; }
.dot-disconnected { background: #f56c6c; }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }

.chat-box {
  height: 480px; overflow-y: auto; padding: 20px 24px; background: #f5f7fa;
  scroll-behavior: smooth;
}
.chat-box::-webkit-scrollbar { width: 6px; }
.chat-box::-webkit-scrollbar-track { background: transparent; }
.chat-box::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 3px; }
.chat-box::-webkit-scrollbar-thumb:hover { background: #c0c4cc; }

.chat-empty {
  display: flex; flex-direction: column; justify-content: center; align-items: center;
  height: 100%; color: #909399;
}
.chat-empty-icon { font-size: 48px; margin-bottom: 12px; }
.chat-empty p { font-size: 14px; margin-bottom: 16px; }
.chat-quick-actions { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }

.msg-wrapper { margin-bottom: 16px; }
.msg-system {
  text-align: center; color: #909399; font-size: 12px; padding: 6px 14px;
  background: #ebeef5; border-radius: 12px; display: inline-block;
}
.system-badge {
  background: #909399; color: #fff; padding: 1px 6px; border-radius: 3px;
  font-size: 10px; margin-right: 4px;
}

.msg { display: flex; align-items: flex-start; gap: 10px; }
.msg-right { flex-direction: row-reverse; }
.msg-avatar { flex-shrink: 0; margin-top: 4px; font-size: 14px; }
.msg-body { max-width: 65%; }
.msg-user { font-size: 12px; color: #909399; margin-bottom: 4px; }
.msg-right .msg-user { text-align: right; }
.msg-content {
  display: inline-block; padding: 12px 16px; border-radius: 16px;
  font-size: 14px; line-height: 1.6; word-break: break-word;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.msg-left .msg-content {
  background: #fff; color: #303133;
  border-top-left-radius: 4px;
}
.msg-right .msg-content {
  background: linear-gradient(135deg, #409eff, #66b1ff); color: #fff;
  border-top-right-radius: 4px;
}
.msg-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
.msg-right .msg-time { text-align: right; }

.chat-input {
  display: flex; gap: 10px; padding: 16px 24px; border-top: 1px solid #ebeef5;
  background: #fff;
}

@media (max-width: 768px) {
  .chat-page { border-radius: 0; }
  .chat-box { height: calc(100vh - 260px); min-height: 300px; padding: 12px 16px; }
  .msg-body { max-width: 78%; }
  .chat-header { padding: 14px 16px; }
  .chat-header-sub { display: none; }
  .chat-input { padding: 12px 16px; }
  .chat-quick-actions { padding: 0 16px; }
}
</style>
