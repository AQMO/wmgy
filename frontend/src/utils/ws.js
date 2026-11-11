/** WebSocket 实时通道（聊天 + 通知），自动重连 */
let chatSock = null
let notifySock = null
let chatHandlers = []
let notifyHandlers = []
let retryChat = null
let retryNotify = null

function base() {
  return `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}`
}

function token() {
  return localStorage.getItem('token') || ''
}

function connect(handlerListRef, path, setRetry, handlers) {
  if (!token()) return null
  const sock = new WebSocket(`${base()}${path}?token=${encodeURIComponent(token())}`)
  sock.onmessage = e => {
    try {
      const msg = JSON.parse(e.data)
      handlers.forEach(h => { try { h(msg) } catch (err) { /* 忽略单个处理器异常 */ } })
    } catch (err) { /* 非 JSON 忽略 */ }
  }
  sock.onclose = () => {
    setRetry(setTimeout(() => { handlerListRef() }, 5000))
  }
  return sock
}

function connectChat() {
  chatSock = connect(connectChat, '/ws/chat', t => { retryChat = t }, chatHandlers)
}
function connectNotify() {
  notifySock = connect(connectNotify, '/ws/notify', t => { retryNotify = t }, notifyHandlers)
}

/** 连接聊天与通知两个通道（登录后调用） */
export function startWs() {
  stopWs()
  connectChat()
  connectNotify()
}

export function stopWs() {
  clearTimeout(retryChat)
  clearTimeout(retryNotify)
  if (chatSock) { try { chatSock.close() } catch (e) { /* 忽略 */ } chatSock = null }
  if (notifySock) { try { notifySock.close() } catch (e) { /* 忽略 */ } notifySock = null }
}

export function onChat(handler) {
  chatHandlers.push(handler)
  return () => { chatHandlers = chatHandlers.filter(h => h !== handler) }
}

export function onNotify(handler) {
  notifyHandlers.push(handler)
  return () => { notifyHandlers = notifyHandlers.filter(h => h !== handler) }
}
