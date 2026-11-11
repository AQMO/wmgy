/** 小程序 WebSocket 实时通道（聊天 + 通知），自动重连 */
let chatSock = null
let notifySock = null
let retryChat = null
let retryNotify = null
const chatHandlers = []
const notifyHandlers = []

function wsBase() {
  // localhost:8080 与 request.js 保持一致；生产替换为 wss 域名
  return 'ws://localhost:8080'
}

function connect(path, handlers, setSock, setRetry, reconnect) {
  const token = uni.getStorageSync('token')
  if (!token) return
  const sock = uni.connectSocket({
    url: `${wsBase()}${path}?token=${encodeURIComponent(token)}`,
    complete: () => {}
  })
  setSock(sock)
  sock.onMessage(res => {
    try {
      const msg = JSON.parse(res.data)
      handlers.forEach(h => { try { h(msg) } catch (e) { /* 忽略 */ } })
    } catch (e) { /* 非 JSON 忽略 */ }
  })
  sock.onClose(() => {
    setRetry(setTimeout(reconnect, 5000))
  })
  sock.onError(() => { try { sock.close({}) } catch (e) { /* 忽略 */ } })
}

export function startWs() {
  stopWs()
  connect('/ws/chat', chatHandlers, s => { chatSock = s }, t => { retryChat = t }, () => { chatSock = null; startWsOnlyChat() })
  connect('/ws/notify', notifyHandlers, s => { notifySock = s }, t => { retryNotify = t }, () => { notifySock = null; startWsOnlyNotify() })
}

function startWsOnlyChat() {
  connect('/ws/chat', chatHandlers, s => { chatSock = s }, t => { retryChat = t }, startWsOnlyChat)
}
function startWsOnlyNotify() {
  connect('/ws/notify', notifyHandlers, s => { notifySock = s }, t => { retryNotify = t }, startWsOnlyNotify)
}

export function stopWs() {
  clearTimeout(retryChat)
  clearTimeout(retryNotify)
  if (chatSock) { try { chatSock.close({}) } catch (e) { /* 忽略 */ } chatSock = null }
  if (notifySock) { try { notifySock.close({}) } catch (e) { /* 忽略 */ } notifySock = null }
}

export function onChat(handler) { chatHandlers.push(handler) }
export function onNotify(handler) { notifyHandlers.push(handler) }
