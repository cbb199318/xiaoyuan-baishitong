const readCurrentPageOptions = () => {
  const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
  const current = pages[pages.length - 1]
  const pageOptions = current?.options || {}
  if (Object.keys(pageOptions).length) {
    return pageOptions
  }
  if (typeof window !== 'undefined') {
    const hash = window.location.hash || ''
    const query = hash.includes('?') ? hash.split('?')[1] : ''
    const params = new URLSearchParams(query)
    return Object.fromEntries(params.entries())
  }
  return {}
}

export const isFromJobsMine = (options = {}) => {
  const routeOptions = Object.keys(options).length ? options : readCurrentPageOptions()
  return String(routeOptions.from || '') === 'jobs-mine'
}

export const backToJobsMine = () => {
  uni.redirectTo({ url: '/pages/jobs/index?tab=mine' })
}

export const createJobsMineBackHandler = (options = {}) => () => {
  if (isFromJobsMine(options)) {
    backToJobsMine()
    return
  }
  uni.navigateBack({
    fail: () => {
      backToJobsMine()
    },
  })
}

export const handleJobsMineBackPress = (options = {}) => {
  if (!isFromJobsMine(options)) {
    return false
  }
  backToJobsMine()
  return true
}
