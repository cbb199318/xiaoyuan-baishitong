export const jobsCategoryOptions = [
  { label: '全部', value: 'ALL' },
  { label: '线上互助', value: 'ONLINE_HELP' },
  { label: 'PPT&文档服务', value: 'PPT_DOC' },
  { label: '文案代写', value: 'COPYWRITING' },
  { label: '家教辅导', value: 'TUTOR' },
  { label: '校内兼职', value: 'CAMPUS_PARTTIME' },
  { label: '假期兼职', value: 'HOLIDAY_PARTTIME' },
]

export const jobsPublishCategoryOptions = jobsCategoryOptions.filter((item) => item.value !== 'ALL')

export const jobsCategoryMap = {
  ONLINE_HELP: '线上互助',
  PPT_DOC: 'PPT&文档服务',
  COPYWRITING: '文案代写',
  TUTOR: '家教辅导',
  CAMPUS_PARTTIME: '校内兼职',
  HOLIDAY_PARTTIME: '假期兼职',
}

export const jobsRoleOptions = [
  { label: '学生发布', value: 'STUDENT' },
  { label: '商家发布', value: 'BUSINESS' },
]

export const jobsRoleMap = {
  STUDENT: '学生发布',
  BUSINESS: '商家发布',
}

export const jobsPublishTypeOptions = [
  { label: 'PPT制作', value: 'PPT' },
  { label: '文档格式修改', value: 'DOC_EDIT' },
  { label: '文案代写', value: 'COPYWRITING' },
  { label: '家教辅导', value: 'TUTOR' },
  { label: '校园互助', value: 'CAMPUS_HELP' },
  { label: '线下兼职', value: 'OFFLINE_PARTTIME' },
  { label: '假期岗位', value: 'HOLIDAY_JOB' },
]

export const jobsPublishTypeLabelMap = Object.fromEntries(jobsPublishTypeOptions.map((item) => [item.value, item.label]))
