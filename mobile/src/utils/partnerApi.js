import request from './request'

export const listPartnerDemands = (params = {}) => request.get('/partner/demands', { params })

export const getPartnerDemandDetail = (id) => request.get(`/partner/demands/${id}`)

export const listMyPartnerDemands = () => request.get('/partner/demands/mine')

export const listPartnerConversations = () => request.get('/partner/conversations')

export const getPartnerConversationDetail = (id) => request.get(`/partner/conversations/${id}`)

export const createPartnerDemand = (payload) => request.post('/partner/demands', payload)

export const updatePartnerDemand = (id, payload) => request.put(`/partner/demands/${id}`, payload)

export const offlinePartnerDemand = (id) => request.post(`/partner/demands/${id}/offline`)

export const applyPartnerDemand = (id) => request.post(`/partner/demands/${id}/apply`)

export const sendPartnerConversationMessage = (id, payload) =>
  request.post(`/partner/conversations/${id}/messages`, payload)

export const markPartnerConversationRead = (id) => request.post(`/partner/conversations/${id}/read`)

export const updatePartnerConversationStatus = (id, payload) =>
  request.post(`/partner/conversations/${id}/status`, payload)

export const getPartnerUserProfile = (userId) => request.get(`/partner/users/${userId}`)
