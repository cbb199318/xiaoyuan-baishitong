const baseURL = 'http://localhost:9001/api'

export const uploadImage = ({ bizType = 'common' } = {}) =>
  new Promise((resolve, reject) => {
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      success: ({ tempFilePaths }) => {
        const filePath = tempFilePaths?.[0]
        if (!filePath) {
          reject(new Error('未选择图片'))
          return
        }
        uni.uploadFile({
          url: `${baseURL}/files/upload/image`,
          filePath,
          name: 'file',
          formData: { bizType },
          header: {
            Authorization: `Bearer ${uni.getStorageSync('token') || ''}`,
          },
          success: (uploadRes) => {
            try {
              const payload = JSON.parse(uploadRes.data || '{}')
              if (payload.code !== 0) {
                uni.showToast({ title: payload.message || '上传失败', icon: 'none' })
                reject(new Error(payload.message || '上传失败'))
                return
              }
              resolve(payload.data)
            } catch (error) {
              uni.showToast({ title: '上传响应解析失败', icon: 'none' })
              reject(error)
            }
          },
          fail: (error) => {
            uni.showToast({ title: '图片上传失败', icon: 'none' })
            reject(error)
          },
        })
      },
      fail: (error) => {
        reject(error)
      },
    })
  })

export const previewImage = (url) => {
  if (!url) {
    return
  }
  uni.previewImage({
    urls: [url],
    current: url,
  })
}
