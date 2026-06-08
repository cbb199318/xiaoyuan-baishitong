import request from './request'

export const listJobs = (params = {}) => request.get('/jobs/posts', { params })

export const getJobDetail = (id) => request.get(`/jobs/posts/${id}`)

export const createJobPost = (payload) => request.post('/jobs/posts', payload)

export const updateJobPost = (id, payload) => request.put(`/jobs/posts/${id}`, payload)

export const deleteJobPost = (id) => request.delete(`/jobs/posts/${id}`)

export const updateJobVisibility = (id, publicVisible) =>
  request.put(`/jobs/posts/${id}/visibility`, { publicVisible })

export const listMyJobs = () => request.get('/jobs/posts/mine')

export const listFavoriteJobs = () => request.get('/jobs/favorites')

export const favoriteJob = (id) => request.post(`/jobs/posts/${id}/favorite`)

export const unfavoriteJob = (id) => request.post(`/jobs/posts/${id}/unfavorite`)

export const createJobApplication = (payload) => request.post('/jobs/applications', payload)

export const listMyJobApplications = () => request.get('/jobs/applications/mine')

export const listJobsConversations = () => request.get('/jobs/conversations')

export const getJobsConversationDetail = (id) => request.get(`/jobs/conversations/${id}`)

export const ensureJobConversation = (postId) => request.get(`/jobs/posts/${postId}/conversation`)

export const hideJobsConversation = (id) => request.post(`/conversations/${id}/hide`)

export const getJobsResume = () => request.get('/jobs/resume')

export const saveJobsResume = (payload) => request.put('/jobs/resume', payload)

export const getJobsReviewProgress = () => request.get('/jobs/review-progress')
