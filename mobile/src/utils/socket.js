let socketTask = null
let activeToken = ''

const listeners = new Set()

const wsUrl = (token) => `ws://localhost:9001/api/ws/connect?token=${encodeURIComponent(token)}`

const notify = (payload) => {
  listeners.forEach((listener) => {
    try {
      listener(payload)
    } catch (error) {
      console.error(error)
    }
  })
}

export const addSocketListener = (listener) => {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export const connectSocket = (token) => {
  if (!token) {
    return
  }
  if (socketTask && activeToken === token) {
    return
  }
  if (socketTask) {
    socketTask.close()
  }

  activeToken = token
  socketTask = uni.connectSocket({
    url: wsUrl(token),
    complete: () => {},
  })

  socketTask.onMessage((event) => {
    try {
      const payload = JSON.parse(event.data)
      notify(payload)
    } catch (error) {
      console.error(error)
    }
  })

  socketTask.onClose(() => {
    socketTask = null
    if (activeToken === token && uni.getStorageSync('token') === token) {
      setTimeout(() => {
        connectSocket(token)
      }, 1500)
    }
  })
}

export const closeSocket = () => {
  activeToken = ''
  if (socketTask) {
    socketTask.close()
    socketTask = null
  }
}
