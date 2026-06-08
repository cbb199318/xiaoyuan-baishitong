import request from './request'

export const listBeautyGoods = (params = {}) =>
  request.get('/beauty/goods', {
    params: {
      current: 1,
      size: 50,
      ...params,
    },
  })

export const getBeautyGoodDetail = (id) => request.get(`/beauty/goods/${id}`)

export const createBeautyGood = (payload) => request.post('/beauty/goods', payload)

export const listMyBeautyGoods = () => request.get('/beauty/goods/mine')

export const listMyBeautyFavorites = () => request.get('/beauty/favorites')

export const favoriteBeautyGood = (id) => request.post(`/beauty/goods/${id}/favorite`)

export const unfavoriteBeautyGood = (id) => request.post(`/beauty/goods/${id}/unfavorite`)
