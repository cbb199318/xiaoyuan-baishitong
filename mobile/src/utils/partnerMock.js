export const partnerCategoryOptions = [
  { label: '全部', value: 'ALL' },
  { label: '学习搭子', value: 'STUDY' },
  { label: '干饭搭子', value: 'MEAL' },
  { label: '运动搭子', value: 'SPORT' },
  { label: '出行搭子', value: 'TRAVEL' },
  { label: '探店搭子', value: 'EXPLORE' },
]

export const partnerPublishCategoryOptions = partnerCategoryOptions.filter((item) => item.value !== 'ALL')

export const partnerCategoryMap = {
  STUDY: '学习搭子',
  MEAL: '干饭搭子',
  SPORT: '运动搭子',
  TRAVEL: '出行搭子',
  EXPLORE: '探店搭子',
}

export const partnerTimeSlotOptions = [
  '上午 08:00 - 10:00',
  '上午 10:00 - 12:00',
  '中午 12:00 - 14:00',
  '下午 14:00 - 16:00',
  '下午 16:00 - 18:00',
  '晚上 18:00 - 20:00',
  '晚上 20:00 - 22:00',
]

export const partnerStorageKeys = {
  published: 'partner-published-list',
  refreshTick: 'partner-refresh-tick',
  profileTags: 'partner-profile-tags',
}

const partnerDemoUserPhone = '18800000001'
const partnerDemoUserId = 14
const partnerDemoDemandId = 7101

const partnerUserProfileBaseMap = {
  900101: { phone: '13800001001', bio: '理工科自习搭子，擅长做题和整理复习节奏。' },
  900102: { phone: '13800001002', bio: '喜欢边干饭边聊天，熟悉学校食堂和周边小店。' },
  900103: { phone: '13800001003', bio: '晨跑打卡选手，节奏稳定，欢迎一起坚持运动。' },
  900104: { phone: '13800001004', bio: '周末经常出行，时间观念强，擅长拼车规划。' },
  900105: { phone: '13800001005', bio: '甜品和探店爱好者，喜欢拍照和记录校园生活。' },
  900106: { phone: '13800001006', bio: '英语口语慢热型选手，适合轻松交流互练。' },
  930201: { phone: '13800003001', bio: '平时爱泡图书馆，擅长梳理知识点，沟通耐心。' },
}

export const partnerPresetTags = [
  '性格开朗',
  '作息规律',
  '擅长学习',
  '爱吃探店',
  '热爱运动',
  '耐心靠谱',
  '时间自由',
  '极简社交',
]

const partnerBaseList = [
  {
    id: 1,
    publisherId: 900101,
    type: 'STUDY',
    timeText: '今晚 19:00 - 21:00',
    location: '图书馆三楼自习区',
    needTags: ['期末冲刺', '高数复习'],
    userTags: ['不鸽人', '会做思维导图'],
    description: '想找一位一起刷高数和线代题的同学，互相监督，效率拉满。',
    remainingSlots: 1,
    totalSlots: 2,
    nickname: '数分不挂科',
    createdAt: '2026-06-02T20:40:00',
    matchScore: 98,
  },
  {
    id: 2,
    publisherId: 900102,
    type: 'MEAL',
    timeText: '今天 18:10',
    location: '二食堂二楼小碗菜',
    needTags: ['晚饭拼桌', '边吃边聊'],
    userTags: ['社牛一点点', '口味清淡'],
    description: '一个人吃饭太无聊，想找个干饭搭子一起冲晚饭，吃完还能顺路散步。',
    remainingSlots: 2,
    totalSlots: 3,
    nickname: '米饭加两勺',
    createdAt: '2026-06-02T21:10:00',
    matchScore: 95,
  },
  {
    id: 3,
    publisherId: 900103,
    type: 'SPORT',
    timeText: '明早 06:30',
    location: '东操场看台集合',
    needTags: ['晨跑', '5 公里'],
    userTags: ['早起党', '节奏稳定'],
    description: '想坚持晨跑减脂，找能互相提醒的运动搭子，一起打卡更容易坚持。',
    remainingSlots: 1,
    totalSlots: 2,
    nickname: '晨跑计划中',
    createdAt: '2026-06-02T19:35:00',
    matchScore: 92,
  },
  {
    id: 4,
    publisherId: 900104,
    type: 'TRAVEL',
    timeText: '周六上午',
    location: '高铁站北广场',
    needTags: ['拼车', '短途出行'],
    userTags: ['行李少', '时间观念强'],
    description: '周六回家去高铁站，想找顺路搭子一起打车拼车，分摊车费更划算。',
    remainingSlots: 3,
    totalSlots: 4,
    nickname: '周末回家人',
    createdAt: '2026-06-02T18:55:00',
    matchScore: 89,
  },
  {
    id: 5,
    publisherId: 900105,
    type: 'EXPLORE',
    timeText: '周日 15:00',
    location: '校门口新开的甜品店',
    needTags: ['拍照打卡', '甜品探店'],
    userTags: ['会找角度', '安静好相处'],
    description: '想去新开的甜品店拍照打卡，找一个能一起试新品、顺便聊天的搭子。',
    remainingSlots: 1,
    totalSlots: 2,
    nickname: '奶油收藏家',
    createdAt: '2026-06-02T17:48:00',
    matchScore: 90,
  },
  {
    id: 6,
    publisherId: 900106,
    type: 'STUDY',
    timeText: '明天下午 14:00',
    location: '创新楼 204',
    needTags: ['英语口语', '互练表达'],
    userTags: ['i 人友好', '有耐心'],
    description: '准备英语口语展示，想找搭子互相练习表达和纠音，氛围轻松一点最好。',
    remainingSlots: 1,
    totalSlots: 2,
    nickname: '口语慢慢来',
    createdAt: '2026-06-01T22:16:00',
    matchScore: 87,
  },
]

const normalizeDemand = (item) => ({
  status: 'ACTIVE',
  ...item,
})

const safeParse = (raw, fallback) => {
  try {
    const parsed = JSON.parse(raw)
    return parsed ?? fallback
  } catch (error) {
    return fallback
  }
}

const getCurrentProfile = () => {
  const profileRaw = uni.getStorageSync('user-info')
  return typeof profileRaw === 'string' ? safeParse(profileRaw, null) : profileRaw
}

const shouldUsePartnerDemoData = (profile = getCurrentProfile(), userId) => {
  const currentUserId = Number(userId || profile?.userId || 0)
  return currentUserId === partnerDemoUserId || String(profile?.phone || '') === partnerDemoUserPhone
}

export const readPublishedPartnerList = () => {
  const raw = uni.getStorageSync(partnerStorageKeys.published) || '[]'
  const list = safeParse(raw, [])
  return Array.isArray(list) ? list.map(normalizeDemand) : []
}

const readProfileTagMap = () => {
  const raw = uni.getStorageSync(partnerStorageKeys.profileTags) || '{}'
  const map = safeParse(raw, {})
  return typeof map === 'object' && map ? map : {}
}

const writeProfileTagMap = (map) => {
  uni.setStorageSync(partnerStorageKeys.profileTags, JSON.stringify(map))
}

const touchRefreshTick = () => {
  uni.setStorageSync(partnerStorageKeys.refreshTick, String(Date.now()))
}

const ensurePartnerDemoData = (userId) => {
  const profile = getCurrentProfile()
  if (!shouldUsePartnerDemoData(profile, userId)) {
    return
  }
  const currentUserId = Number(userId || profile?.userId || partnerDemoUserId)
  const publishedList = readPublishedPartnerList()
  const hasDemoDemand = publishedList.some((item) => Number(item.id) === partnerDemoDemandId)
  if (!hasDemoDemand) {
    publishedList.unshift(normalizeDemand({
      id: partnerDemoDemandId,
      publisherId: currentUserId,
      type: 'STUDY',
      timeText: '本周五 晚上 18:00 - 20:00',
      location: '教学楼 A302 复习角',
      needTags: ['期末冲刺', '互相监督'],
      userTags: ['耐心靠谱', '擅长学习'],
      description: '想找一位一起整理重点和刷题的学习搭子，主要复习专业课和期末知识框架。',
      remainingSlots: 2,
      totalSlots: 2,
      nickname: profile?.nickname || '默认演示用户',
      createdAt: '2026-06-03T19:20:00',
      updatedAt: '2026-06-03T19:20:00',
      matchScore: 100,
    }))
    uni.setStorageSync(partnerStorageKeys.published, JSON.stringify(publishedList))
  }

  const profileTagMap = readProfileTagMap()
  if (!Array.isArray(profileTagMap[String(currentUserId)]) || !profileTagMap[String(currentUserId)].length) {
    profileTagMap[String(currentUserId)] = ['性格开朗', '擅长学习', '耐心靠谱']
    writeProfileTagMap(profileTagMap)
  }

  const appliedKey = `partner-applied-${currentUserId}`
  const appliedIds = safeParse(uni.getStorageSync(appliedKey) || '[]', [])
  if (Array.isArray(appliedIds) && !appliedIds.includes(2)) {
    uni.setStorageSync(appliedKey, JSON.stringify([...appliedIds, 2]))
  }

  touchRefreshTick()
}

export const getAllPartnerDemands = () =>
  (ensurePartnerDemoData(), [...readPublishedPartnerList(), ...partnerBaseList.map(normalizeDemand)])
    .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())

export const getPartnerMockList = () =>
  getAllPartnerDemands().filter((item) => item.status !== 'OFFLINE')

export const getPartnerDemandById = (id) =>
  getAllPartnerDemands().find((item) => String(item.id) === String(id)) || null

export const getPartnerUserProfile = (userId) => {
  if (!userId) {
    return null
  }
  const demand = getAllPartnerDemands().find((item) => Number(item.publisherId || 0) === Number(userId))
  const localProfile = getCurrentProfile()

  const isSelf = Number(localProfile?.userId || 0) === Number(userId)
  const source = isSelf
    ? {
        nickname: localProfile?.nickname || demand?.nickname || '搭子同学',
        phone: localProfile?.phone || partnerUserProfileBaseMap[userId]?.phone || '',
        bio: localProfile?.bio || partnerUserProfileBaseMap[userId]?.bio || '',
      }
    : {
        nickname: demand?.nickname || '搭子同学',
        phone: partnerUserProfileBaseMap[userId]?.phone || '',
        bio: partnerUserProfileBaseMap[userId]?.bio || '',
      }

  const publishedList = getAllPartnerDemands().filter((item) => Number(item.publisherId || 0) === Number(userId))
  return {
    userId: Number(userId),
    nickname: source.nickname,
    phone: source.phone,
    bio: source.bio,
    tags: getPartnerProfileTags(userId),
    demandCount: publishedList.length,
    activeDemandCount: publishedList.filter((item) => item.status !== 'OFFLINE').length,
  }
}

export const savePublishedPartner = (payload) => {
  const current = readPublishedPartnerList()
  const normalized = normalizeDemand(payload)
  const index = current.findIndex((item) => String(item.id) === String(normalized.id))
  const nextList = [...current]
  if (index >= 0) {
    nextList.splice(index, 1, normalized)
  } else {
    nextList.unshift(normalized)
  }
  uni.setStorageSync(partnerStorageKeys.published, JSON.stringify(nextList))
  touchRefreshTick()
  return nextList
}

export const updatePartnerDemandStatus = (id, status) => {
  const nextList = readPublishedPartnerList().map((item) =>
    String(item.id) === String(id)
      ? {
          ...item,
          status,
          updatedAt: new Date().toISOString(),
        }
      : item
  )
  uni.setStorageSync(partnerStorageKeys.published, JSON.stringify(nextList))
  touchRefreshTick()
  return nextList
}

export const getPartnerProfileTags = (userId) => {
  if (!userId) {
    return []
  }
  const map = readProfileTagMap()
  const tags = map[String(userId)]
  return Array.isArray(tags) ? tags : []
}

export const savePartnerProfileTags = (userId, tags) => {
  if (!userId) {
    return
  }
  const safeTags = (tags || []).slice(0, 8)
  const map = readProfileTagMap()
  map[String(userId)] = safeTags
  uni.setStorageSync(partnerStorageKeys.profileTags, JSON.stringify(map))

  const nextList = readPublishedPartnerList().map((item) =>
    Number(item.publisherId || 0) === Number(userId)
      ? {
          ...item,
          userTags: safeTags.slice(0, 4),
          updatedAt: new Date().toISOString(),
        }
      : item
  )
  uni.setStorageSync(partnerStorageKeys.published, JSON.stringify(nextList))
  touchRefreshTick()
}
