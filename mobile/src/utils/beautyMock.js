export const beautyCategoryOptions = [
  { label: '全部好物', value: 'ALL' },
  { label: '护肤彩妆', value: 'MAKEUP' },
  { label: '个护收纳', value: 'CARE' },
  { label: '配套好物', value: 'ACCESSORY' },
]

export const beautyCategoryMap = {
  ALL: '全部好物',
  MAKEUP: '护肤彩妆',
  CARE: '个护收纳',
  ACCESSORY: '配套好物',
}

const beautyGoodsStorageKey = 'beauty-user-goods'
const beautyFavoriteStorageKey = 'beauty-favorite-ids'

const escapeXml = (value = '') =>
  String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;')

const beautyImagePalettes = {
  rose: {
    bg1: '#fff4f8',
    bg2: '#ffd7e6',
    card: '#ffffff',
    accent: '#ec4899',
    accentSoft: '#f9a8d4',
    ink: '#831843',
  },
  peach: {
    bg1: '#fff7ed',
    bg2: '#fed7aa',
    card: '#fffaf5',
    accent: '#f97316',
    accentSoft: '#fdba74',
    ink: '#9a3412',
  },
  gold: {
    bg1: '#fffbea',
    bg2: '#fde68a',
    card: '#fffef7',
    accent: '#d97706',
    accentSoft: '#facc15',
    ink: '#92400e',
  },
  mint: {
    bg1: '#ecfeff',
    bg2: '#a5f3fc',
    card: '#f7feff',
    accent: '#06b6d4',
    accentSoft: '#67e8f9',
    ink: '#155e75',
  },
  lavender: {
    bg1: '#f5f3ff',
    bg2: '#ddd6fe',
    card: '#fbfaff',
    accent: '#8b5cf6',
    accentSoft: '#c4b5fd',
    ink: '#5b21b6',
  },
}

const buildBeautyArtwork = (accent, palette) => {
  if (accent === 'compact') {
    return `
      <circle cx="450" cy="308" r="118" fill="${palette.card}" stroke="${palette.accent}" stroke-width="18"/>
      <circle cx="450" cy="308" r="62" fill="${palette.accentSoft}" opacity="0.7"/>
      <rect x="324" y="438" width="252" height="92" rx="30" fill="${palette.accent}"/>
      <rect x="348" y="458" width="204" height="52" rx="22" fill="${palette.card}" opacity="0.95"/>
    `
  }
  if (accent === 'lip') {
    return `
      <rect x="388" y="228" width="124" height="234" rx="34" fill="${palette.card}" stroke="${palette.accent}" stroke-width="16"/>
      <rect x="388" y="390" width="124" height="72" rx="24" fill="${palette.accent}"/>
      <rect x="414" y="184" width="72" height="68" rx="22" fill="${palette.ink}" opacity="0.84"/>
      <path d="M560 418 C616 384 664 386 704 414 C644 448 600 456 560 418Z" fill="${palette.accentSoft}"/>
    `
  }
  if (accent === 'contour') {
    return `
      <rect x="292" y="220" width="316" height="208" rx="34" fill="${palette.card}" stroke="${palette.accent}" stroke-width="14"/>
      <rect x="322" y="256" width="78" height="118" rx="20" fill="${palette.accentSoft}" opacity="0.68"/>
      <rect x="410" y="256" width="78" height="118" rx="20" fill="${palette.accent}" opacity="0.58"/>
      <rect x="498" y="256" width="78" height="118" rx="20" fill="${palette.ink}" opacity="0.2"/>
      <path d="M350 492 L572 404" stroke="${palette.ink}" stroke-width="18" stroke-linecap="round"/>
      <path d="M560 408 L626 452 L594 486 L538 424 Z" fill="${palette.accent}"/>
    `
  }
  if (accent === 'brow') {
    return `
      <path d="M312 356 C378 302 448 296 520 336" stroke="${palette.ink}" stroke-width="20" stroke-linecap="round" fill="none"/>
      <path d="M326 390 C392 336 462 330 532 370" stroke="${palette.accentSoft}" stroke-width="16" stroke-linecap="round" fill="none"/>
      <rect x="542" y="206" width="34" height="250" rx="14" transform="rotate(24 559 331)" fill="${palette.accent}"/>
      <rect x="570" y="188" width="30" height="86" rx="10" transform="rotate(24 585 231)" fill="${palette.ink}"/>
    `
  }
  if (accent === 'organizer') {
    return `
      <rect x="266" y="254" width="368" height="246" rx="30" fill="${palette.card}" stroke="${palette.accent}" stroke-width="16"/>
      <path d="M388 254V500" stroke="${palette.accentSoft}" stroke-width="12"/>
      <path d="M510 254V500" stroke="${palette.accentSoft}" stroke-width="12"/>
      <path d="M266 362H634" stroke="${palette.accentSoft}" stroke-width="12"/>
      <rect x="296" y="286" width="62" height="142" rx="18" fill="${palette.accent}" opacity="0.42"/>
      <rect x="420" y="286" width="62" height="92" rx="18" fill="${palette.accent}" opacity="0.56"/>
      <rect x="542" y="286" width="62" height="162" rx="18" fill="${palette.accent}" opacity="0.3"/>
    `
  }
  if (accent === 'brush') {
    return `
      <path d="M390 446 C414 376 460 318 524 264" stroke="${palette.ink}" stroke-width="24" stroke-linecap="round"/>
      <path d="M522 264 C570 230 622 228 660 264 C626 318 580 340 532 322 Z" fill="${palette.accentSoft}"/>
      <rect x="354" y="436" width="104" height="34" rx="16" transform="rotate(-18 406 453)" fill="${palette.accent}"/>
      <path d="M566 368 C616 348 656 350 702 384" stroke="${palette.accent}" stroke-width="16" stroke-linecap="round"/>
    `
  }
  if (accent === 'mirror') {
    return `
      <circle cx="444" cy="292" r="118" fill="${palette.card}" stroke="${palette.accent}" stroke-width="16"/>
      <circle cx="444" cy="292" r="82" fill="${palette.accentSoft}" opacity="0.45"/>
      <rect x="420" y="398" width="48" height="126" rx="20" fill="${palette.accent}"/>
      <rect x="364" y="504" width="160" height="32" rx="16" fill="${palette.ink}" opacity="0.15"/>
      <circle cx="594" cy="234" r="18" fill="${palette.accent}" opacity="0.65"/>
      <circle cx="638" cy="280" r="12" fill="${palette.accent}" opacity="0.42"/>
    `
  }
  if (accent === 'remover') {
    return `
      <rect x="370" y="196" width="156" height="260" rx="40" fill="${palette.card}" stroke="${palette.accent}" stroke-width="14"/>
      <rect x="404" y="154" width="88" height="54" rx="18" fill="${palette.accent}"/>
      <rect x="402" y="270" width="92" height="104" rx="20" fill="${palette.accentSoft}" opacity="0.75"/>
      <circle cx="602" cy="406" r="46" fill="${palette.card}" stroke="${palette.accent}" stroke-width="12"/>
      <circle cx="650" cy="446" r="28" fill="${palette.card}" stroke="${palette.accentSoft}" stroke-width="10"/>
    `
  }
  if (accent === 'banner-shelf') {
    return `
      <rect x="238" y="248" width="424" height="226" rx="38" fill="${palette.card}" opacity="0.94"/>
      <rect x="286" y="282" width="80" height="136" rx="24" fill="${palette.accentSoft}"/>
      <rect x="392" y="250" width="118" height="168" rx="28" fill="${palette.accent}"/>
      <rect x="536" y="300" width="76" height="118" rx="24" fill="${palette.ink}" opacity="0.18"/>
      <path d="M236 490H664" stroke="${palette.accent}" stroke-width="18" stroke-linecap="round" opacity="0.5"/>
    `
  }
  if (accent === 'banner-routine') {
    return `
      <circle cx="344" cy="308" r="86" fill="${palette.card}" stroke="${palette.accent}" stroke-width="14"/>
      <rect x="458" y="212" width="98" height="206" rx="28" fill="${palette.card}" stroke="${palette.accent}" stroke-width="14"/>
      <rect x="582" y="266" width="58" height="152" rx="22" fill="${palette.accentSoft}"/>
      <path d="M282 436 C356 398 418 398 484 430" stroke="${palette.accent}" stroke-width="18" stroke-linecap="round"/>
    `
  }
  return `
    <rect x="280" y="238" width="340" height="232" rx="36" fill="${palette.card}" opacity="0.96"/>
    <circle cx="360" cy="320" r="56" fill="${palette.accentSoft}"/>
    <circle cx="462" cy="320" r="56" fill="${palette.accent}" opacity="0.75"/>
    <circle cx="564" cy="320" r="56" fill="${palette.ink}" opacity="0.16"/>
    <path d="M286 440 C356 388 430 384 612 438" stroke="${palette.accent}" stroke-width="18" stroke-linecap="round"/>
  `
}

const createBeautyMockImage = ({ title, subtitle, tone = 'rose', accent = 'compact', badge = '' }) => {
  const palette = beautyImagePalettes[tone] || beautyImagePalettes.rose
  const safeTitle = escapeXml(title)
  const safeSubtitle = escapeXml(subtitle || '')
  const safeBadge = escapeXml(badge || '')
  const artwork = buildBeautyArtwork(accent, palette)
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="900" height="900" viewBox="0 0 900 900">
      <defs>
        <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="${palette.bg1}"/>
          <stop offset="100%" stop-color="${palette.bg2}"/>
        </linearGradient>
      </defs>
      <rect width="900" height="900" rx="56" fill="url(#bg)"/>
      <circle cx="160" cy="128" r="92" fill="${palette.card}" opacity="0.42"/>
      <circle cx="768" cy="174" r="74" fill="${palette.accentSoft}" opacity="0.32"/>
      <circle cx="728" cy="716" r="112" fill="${palette.card}" opacity="0.26"/>
      <rect x="78" y="82" width="744" height="736" rx="46" fill="${palette.card}" opacity="0.34"/>
      ${safeBadge ? `<rect x="666" y="126" width="116" height="48" rx="24" fill="${palette.card}" opacity="0.92"/><text x="724" y="158" font-size="24" text-anchor="middle" fill="${palette.ink}" font-family="Arial, PingFang SC, Microsoft YaHei, sans-serif">${safeBadge}</text>` : ''}
      ${artwork}
      <rect x="124" y="612" width="652" height="154" rx="30" fill="${palette.card}" opacity="0.94"/>
      <text x="160" y="676" font-size="42" font-weight="700" fill="${palette.ink}" font-family="Arial, PingFang SC, Microsoft YaHei, sans-serif">${safeTitle}</text>
      <text x="160" y="728" font-size="24" fill="${palette.ink}" opacity="0.78" font-family="Arial, PingFang SC, Microsoft YaHei, sans-serif">${safeSubtitle}</text>
    </svg>
  `
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

const buildBeautyVisualSet = ({ title, tone, accent, coverSubtitle, gallerySubtitles }) => ({
  image: createBeautyMockImage({
    title,
    subtitle: coverSubtitle,
    tone,
    accent,
    badge: 'Mock',
  }),
  gallery: gallerySubtitles.map((subtitle, index) =>
    createBeautyMockImage({
      title,
      subtitle,
      tone,
      accent,
      badge: `0${index + 1}`,
    })
  ),
})

const banner1Visual = createBeautyMockImage({
  title: '热门平价美妆产品',
  subtitle: '气垫、唇釉与学生党高频通勤单品',
  tone: 'rose',
  accent: 'banner-shelf',
})

const banner2Visual = createBeautyMockImage({
  title: '当季学生妆容推荐',
  subtitle: '早八伪素颜与轻通勤妆的基础搭配',
  tone: 'mint',
  accent: 'banner-routine',
})

const banner3Visual = createBeautyMockImage({
  title: '新人特惠平价好物',
  subtitle: '刷具、补妆镜与收纳组合一次配齐',
  tone: 'peach',
  accent: 'banner-shelf',
})

const good5001Visual = buildBeautyVisualSet({
  title: '轻透持妆气垫粉底',
  tone: 'rose',
  accent: 'compact',
  coverSubtitle: '主打轻薄底妆与早八快速拍开',
  gallerySubtitles: ['气垫外观展示', '粉扑与粉芯细节', '伪素颜底妆场景'],
})

const good5002Visual = buildBeautyVisualSet({
  title: '奶杏豆沙镜面唇釉',
  tone: 'peach',
  accent: 'lip',
  coverSubtitle: '黄皮友好 豆沙奶杏 日常提气色',
  gallerySubtitles: ['唇釉管身展示', '镜面光泽感示意', '约会淡妆搭配场景'],
})

const good5003Visual = buildBeautyVisualSet({
  title: '三色修容高光盘',
  tone: 'gold',
  accent: 'contour',
  coverSubtitle: '鼻影 高光 下颌修饰一盘完成',
  gallerySubtitles: ['三色盘面展示', '刷具取粉细节', '通勤轮廓修饰场景'],
})

const good5004Visual = buildBeautyVisualSet({
  title: '新手顺滑眉笔套装',
  tone: 'lavender',
  accent: 'brow',
  coverSubtitle: '自然毛流感 适合手残党快速上手',
  gallerySubtitles: ['眉笔与眉刷组合', '眉形勾勒示意', '早八淡妆使用场景'],
})

const good5005Visual = buildBeautyVisualSet({
  title: '桌面分格化妆收纳盒',
  tone: 'mint',
  accent: 'organizer',
  coverSubtitle: '底妆 唇妆 眉笔分区摆放更顺手',
  gallerySubtitles: ['分格结构展示', '宿舍桌面收纳示意', '高频单品摆放场景'],
})

const good5006Visual = buildBeautyVisualSet({
  title: '柔软抓粉腮红刷',
  tone: 'rose',
  accent: 'brush',
  coverSubtitle: '刷毛细软 适合新手慢慢叠色',
  gallerySubtitles: ['刷型外观展示', '抓粉与晕染示意', '腮红通勤妆场景'],
})

const good5007Visual = buildBeautyVisualSet({
  title: '便携补妆镜小夜灯款',
  tone: 'lavender',
  accent: 'mirror',
  coverSubtitle: '宿舍与图书馆补妆都能看清底妆状态',
  gallerySubtitles: ['镜面与灯光展示', '便携握持示意', '课间补妆场景'],
})

const good5008Visual = buildBeautyVisualSet({
  title: '温和卸妆水按压瓶',
  tone: 'mint',
  accent: 'remover',
  coverSubtitle: '淡妆清洁友好 适合晚间快速卸妆',
  gallerySubtitles: ['按压瓶外观展示', '化妆棉搭配示意', '宿舍晚间卸妆场景'],
})

export const beautyBannerList = [
  {
    id: 'banner-1',
    title: '热门平价美妆产品',
    subtitle: '学生党也能轻松入手的高性价比底妆与唇妆',
    image: banner1Visual,
  },
  {
    id: 'banner-2',
    title: '当季学生妆容推荐',
    subtitle: '早八伪素颜、夏日轻透妆，通勤上课都能用',
    image: banner2Visual,
  },
  {
    id: 'banner-3',
    title: '新人特惠平价好物',
    subtitle: '从刷具到收纳，一次补齐新手化妆包',
    image: banner3Visual,
  },
]

export const beautyTopicList = [
  {
    id: 'topic-1',
    title: '早八伪素颜妆',
    scene: '上课通勤',
    desc: '强调轻薄底妆、自然气色和快速完成，适合宿舍十分钟出门。',
    relatedProductIds: [5001, 5002, 5005],
  },
  {
    id: 'topic-2',
    title: '日常通勤妆',
    scene: '社团 / 实习',
    desc: '整体妆面干净利落，眉眼有神但不过分浓重，适合全天佩戴。',
    relatedProductIds: [5001, 5003, 5006],
  },
  {
    id: 'topic-3',
    title: '约会淡妆',
    scene: '聚餐 / 出游',
    desc: '更注重腮红和唇色氛围感，整体呈现温柔自然的清透状态。',
    relatedProductIds: [5002, 5004, 5006],
  },
  {
    id: 'topic-4',
    title: '新手入门淡妆',
    scene: '零基础入门',
    desc: '从底妆、唇妆到工具一步配齐，尽量减少复杂步骤和翻车概率。',
    relatedProductIds: [5001, 5004, 5007],
  },
]

const beautyGoodsList = [
  {
    id: 5001,
    category: 'MAKEUP',
    title: '轻透持妆气垫粉底',
    price: 69,
    skinTags: ['混油皮', '自然妆感'],
    summary: '粉质细腻，上脸轻薄不闷，适合早八赶时间快速拍开。',
    tutorial: '底妆快速拍匀、局部叠加遮瑕，适合伪素颜妆和日常通勤妆。',
    sceneText: '适合早八上课、日常通勤和社团外出等需要快速完成底妆的学生场景。',
    reviewList: [
      { id: 'r-5001-1', user: '大二通勤党', content: '遮瑕够日常，拍开很快，宿舍出门前十分钟完全来得及。' },
      { id: 'r-5001-2', user: '混油皮测评', content: '面中持妆比想象中好，午后补一次就够，不会假面。' },
    ],
    evaluation:
      '这款气垫更偏轻薄自然路线，适合不追求厚重遮瑕、但希望妆面干净有气色的学生党。上脸后不容易卡在鼻翼和嘴角，搭配简单定妆就能完成基础通勤妆。',
    dormExperience:
      '宿舍使用体验比较友好，开盖即用，不需要另外调和。赶早八时直接快速拍匀，再叠一个提气色唇釉，整体状态就会精神很多。',
    avoidanceGuide:
      '如果是明显干皮，上妆前要先把保湿做好；如果本身瑕疵较多，不建议单独依赖它完成高遮瑕妆面，局部最好叠加遮瑕产品。',
    gallery: good5001Visual.gallery,
    image: good5001Visual.image,
    reportTargetType: 'good',
  },
  {
    id: 5002,
    category: 'MAKEUP',
    title: '奶杏豆沙镜面唇釉',
    price: 39,
    skinTags: ['黄皮友好', '提气色'],
    summary: '颜色温柔显白，成膜快，不挑场景，日常和约会都能搭。',
    tutorial: '薄涂偏自然，叠涂更显氛围感，适合约会淡妆和新手妆容。',
    sceneText: '适合课堂通勤、社团活动和轻约会等需要自然提气色的日常妆容。',
    reviewList: [
      { id: 'r-5002-1', user: '黄皮唇妆控', content: '颜色真的很日常，不挑妆面，素颜薄涂也不会突兀。' },
      { id: 'r-5002-2', user: '宿舍试色组', content: '镜面感很漂亮，拍照显得嘴巴状态特别好。' },
    ],
    evaluation:
      '整体颜色偏温柔奶杏豆沙调，适合学生党日常低门槛使用。成膜后光泽感比较自然，能让整体妆面看起来更有精神。',
    dormExperience:
      '宿舍日常随手补涂很方便，色彩存在感足够，但不会一上嘴就太成熟。搭配简单底妆就能撑起完整感。',
    avoidanceGuide:
      '如果嘴唇起皮明显，先做好润唇打底再上色，否则镜面唇釉会放大唇纹和死皮问题。',
    gallery: good5002Visual.gallery,
    image: good5002Visual.image,
    reportTargetType: 'good',
  },
  {
    id: 5003,
    category: 'MAKEUP',
    title: '三色修容高光盘',
    price: 48,
    skinTags: ['新手适用', '通勤轮廓'],
    summary: '一盘满足鼻影、高光和下颌修饰，粉质细腻不容易结块。',
    tutorial: '少量多次扫在面中和鼻梁，适合日常通勤和面部轮廓入门。',
    sceneText: '适合通勤妆、活动妆和拍照前临时补轮廓，让面部更立体。',
    reviewList: [
      { id: 'r-5003-1', user: '新手修容组', content: '粉质比较柔和，不容易一下下手太重，适合慢慢叠。' },
    ],
    evaluation:
      '这一盘更适合刚入门的学生党做基础轮廓修饰，颜色不会太脏，拿来练手容错率比较高。',
    dormExperience:
      '宿舍灯光下也比较容易控制用量，早上快速修容不会太容易翻车。',
    avoidanceGuide:
      '刷子沾粉后先抖掉浮粉，再轻扫边缘，避免一开始就把颜色压得太实。',
    gallery: good5003Visual.gallery,
    image: good5003Visual.image,
    reportTargetType: 'good',
  },
  {
    id: 5004,
    category: 'MAKEUP',
    title: '新手顺滑眉笔套装',
    price: 29,
    skinTags: ['扁平眉头', '自然毛流'],
    summary: '笔芯软硬适中，附带眉刷，新手不容易一下画太重。',
    tutorial: '适合宿舍赶时间的快速勾勒，也适合作为淡妆基础单品。',
    sceneText: '适合早八淡妆、新手入门妆和日常快速整理眉形。',
    reviewList: [
      { id: 'r-5004-1', user: '眉毛手残党', content: '颜色自然，附带的眉刷很实用，不容易画成蜡笔眉。' },
    ],
    evaluation:
      '属于比较稳妥的入门型眉笔，颜色柔和，适合希望快速修整眉形、但又担心下手太重的学生党。',
    dormExperience:
      '日常放在宿舍桌面或者化妆包里都很方便，早上简单画几笔就能让整个人精神不少。',
    avoidanceGuide:
      '眉头不要一次下笔过重，建议先画眉尾再把余粉轻扫到眉头，整体会更自然。',
    gallery: good5004Visual.gallery,
    image: good5004Visual.image,
    reportTargetType: 'good',
  },
  {
    id: 5005,
    category: 'CARE',
    title: '桌面分格化妆收纳盒',
    price: 25,
    skinTags: ['宿舍友好', '防尘整理'],
    summary: '把底妆、唇妆、眉笔按格收好，桌面看起来更整洁，拿取也方便。',
    tutorial: '适合把高频通勤妆单品集中收纳，早晨拿取更顺手。',
    sceneText: '适合宿舍桌面收纳、化妆台整理和高频单品集中摆放。',
    reviewList: [
      { id: 'r-5005-1', user: '宿舍收纳组', content: '容量比想象中大，基础通勤妆单品一盒就能装下。' },
    ],
    evaluation:
      '虽然不是彩妆单品，但对学生宿舍场景非常友好，能明显减少桌面杂乱和找东西的时间成本。',
    dormExperience:
      '日常把底妆、眉笔和唇妆按格摆好，早上出门补妆和拿取都更顺手。',
    avoidanceGuide:
      '如果放在靠窗高温位置，建议不要长期暴晒，避免塑料材质老化变形。',
    gallery: good5005Visual.gallery,
    image: good5005Visual.image,
    reportTargetType: 'good',
  },
  {
    id: 5006,
    category: 'ACCESSORY',
    title: '柔软抓粉腮红刷',
    price: 19,
    skinTags: ['敏感肌可用', '均匀晕染'],
    summary: '刷毛细软不扎脸，抓粉力适中，适合新手慢慢叠色。',
    tutorial: '搭配腮红和高光更容易做出自然氛围感，适合约会和通勤妆。',
    sceneText: '适合腮红晕染、高光辅助和新手建立基础刷具习惯。',
    reviewList: [
      { id: 'r-5006-1', user: '新手刷具党', content: '刷毛很软，晕染腮红不容易一坨堆在脸上。' },
    ],
    evaluation:
      '这类基础刷具对学生党更实用，价格不高但能明显提升上妆均匀度，尤其适合刚开始学腮红晕染的人。',
    dormExperience:
      '日常拿来扫腮红和高光都够用，宿舍化妆时不用准备太多复杂工具。',
    avoidanceGuide:
      '第一次使用前先轻洗一次并晾干，可以减少浮毛，也更卫生。',
    gallery: good5006Visual.gallery,
    image: good5006Visual.image,
    reportTargetType: 'good',
  },
  {
    id: 5007,
    category: 'ACCESSORY',
    title: '便携补妆镜小夜灯款',
    price: 32,
    skinTags: ['宿舍补妆', '便携外出'],
    summary: '镜面清晰，自带柔光灯，图书馆和宿舍补妆都很方便。',
    tutorial: '适合新手检查底妆是否斑驳，也适合出游随手补妆。',
    sceneText: '适合宿舍补妆、图书馆整理妆面和外出旅行随身携带。',
    reviewList: [
      { id: 'r-5007-1', user: '补妆焦虑患者', content: '灯光很实用，晚上回宿舍也能看清底妆状态。' },
    ],
    evaluation:
      '对经常在外补妆的学生党来说很实用，便携性强，灯光辅助能减少看不清底妆状态的问题。',
    dormExperience:
      '放在书包里不占太多地方，课间和外出时都能快速拿出来补妆。',
    avoidanceGuide:
      '出门携带时建议放进软袋里，避免和钥匙等硬物一起磕碰镜面。',
    gallery: good5007Visual.gallery,
    image: good5007Visual.image,
    reportTargetType: 'good',
  },
  {
    id: 5008,
    category: 'CARE',
    title: '温和卸妆水按压瓶',
    price: 35,
    skinTags: ['敏感肌', '日常淡妆'],
    summary: '卸除日常淡妆够用，清爽不紧绷，适合经常化通勤妆的学生党。',
    tutorial: '搭配化妆棉按压使用，日常晚间清洁更省事。',
    sceneText: '适合每天上轻薄淡妆、回宿舍后快速卸妆清洁的日常场景。',
    reviewList: [
      { id: 'r-5008-1', user: '敏感肌卸妆党', content: '日常卸淡妆很够用，用完脸上不会太干。' },
    ],
    evaluation:
      '对学生党而言属于性价比较高的基础消耗品，尤其适合日常只画淡妆、不想用厚重卸妆油的人。',
    dormExperience:
      '晚上回宿舍按压几下配合化妆棉就能快速清洁，流程简单，不会太占时间。',
    avoidanceGuide:
      '如果是防水眼妆或浓妆，不建议完全靠它单独卸除，最好搭配专门眼唇卸妆产品。',
    gallery: good5008Visual.gallery,
    image: good5008Visual.image,
    reportTargetType: 'good',
  },
]

const parseStorageList = (key) => {
  const raw = uni.getStorageSync(key)
  if (!raw) {
    return []
  }
  if (Array.isArray(raw)) {
    return raw
  }
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return []
  }
}

const saveStorageList = (key, list) => {
  uni.setStorageSync(key, JSON.stringify(list))
}

const buildFallbackText = (title) => `${title}由平台用户上传，已补充实拍图与购买凭证，适合学生党日常选购参考。`

const normalizePublishedGood = (item) => ({
  ...item,
  id: Number(item.id),
  category: item.category || 'MAKEUP',
  price: Number(item.price || 0),
  title: item.title || '未命名好物',
  summary: item.summary || buildFallbackText(item.title || '该好物'),
  tutorial: item.tutorial || item.usageGuide || '支持查看用户补充的使用说明与选购建议。',
  sceneText: item.sceneText || item.usageGuide || '适合宿舍日常使用、学生党平价选购和基础入门场景。',
  evaluation:
    item.evaluation || item.usageGuide || '由用户补充的使用感受与实拍内容组成，方便快速判断是否适合自己。',
  dormExperience:
    item.dormExperience || '已上传商品实拍图与购买凭证，宿舍党可以直接参考外观、价格和使用方式。',
  avoidanceGuide:
    item.avoidanceGuide || '下单前建议结合购买凭证、产品说明和个人需求综合判断，减少踩雷概率。',
  skinTags: Array.isArray(item.skinTags) && item.skinTags.length ? item.skinTags : ['用户发布'],
  reviewList: Array.isArray(item.reviewList) ? item.reviewList : [],
  gallery:
    Array.isArray(item.gallery) && item.gallery.length
      ? item.gallery
      : [item.image, item.productImageFile?.url, item.receiptFile?.url].filter(Boolean),
  image: item.image || item.productImageFile?.url || item.receiptFile?.url || '',
  reportTargetType: item.reportTargetType || 'good',
  createdAt: item.createdAt || new Date().toISOString(),
})

const getPublishedBeautyGoods = () =>
  parseStorageList(beautyGoodsStorageKey).map((item) => normalizePublishedGood(item))

const sortBeautyGoods = (list) =>
  [...list].sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())

export const getBeautyGoodsList = () =>
  sortBeautyGoods([
    ...getPublishedBeautyGoods(),
    ...beautyGoodsList.map((item, index) => ({
      ...item,
      createdAt: item.createdAt || `2026-05-${String(28 - index).padStart(2, '0')}T10:00:00`,
    })),
  ]).map((item) => ({ ...item }))

export const getBeautyGoodById = (id) =>
  getBeautyGoodsList().find((item) => String(item.id) === String(id)) || null

export const getBeautyTopicById = (id) =>
  beautyTopicList.find((item) => String(item.id) === String(id)) || null

export const getBeautyTopicGoods = (topicId) => {
  const topic = getBeautyTopicById(topicId)
  if (!topic) {
    return []
  }
  const ids = topic.relatedProductIds || []
  return getBeautyGoodsList().filter((item) => ids.includes(item.id))
}

export const getBeautyFavoriteIds = () => parseStorageList(beautyFavoriteStorageKey)

export const getBeautyFavoriteGoods = () => {
  const favoriteIds = getBeautyFavoriteIds().map((item) => String(item))
  return getBeautyGoodsList().filter((item) => favoriteIds.includes(String(item.id)))
}

export const getBeautyUserPosts = (profile = {}) => {
  const userId = profile?.id ? String(profile.id) : ''
  const phone = profile?.phone || ''
  return getPublishedBeautyGoods().filter(
    (item) =>
      (userId && String(item.publisherId || '') === userId) ||
      (phone && String(item.publisherPhone || '') === String(phone))
  )
}

export const createBeautyGood = (payload, profile = {}) => {
  const current = parseStorageList(beautyGoodsStorageKey)
  const nextItem = normalizePublishedGood({
    id: Date.now(),
    category: payload.category,
    title: payload.title,
    price: payload.price,
    summary: payload.summary,
    usageGuide: payload.usageGuide,
    productImageFile: payload.productImageFile,
    receiptFile: payload.receiptFile,
    image: payload.productImageFile?.url || payload.receiptFile?.url || '',
    gallery: [payload.productImageFile?.url, payload.receiptFile?.url].filter(Boolean),
    skinTags: ['用户发布', beautyCategoryMap[payload.category] || '平价好物'],
    reportTargetType: 'good',
    publisherId: profile?.id || '',
    publisherPhone: profile?.phone || '',
    publisherName: profile?.nickname || '校园用户',
    createdAt: new Date().toISOString(),
  })
  current.unshift(nextItem)
  saveStorageList(beautyGoodsStorageKey, current)
  return nextItem
}
