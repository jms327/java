<script setup>
import { ref } from 'vue'

const emit = defineEmits(['back'])

/* 是否显示剧透段落：默认折叠，避免一上来就剧透结局 */
const revealSpoiler = ref(false)

/* ===== 九大赛段 ===== */
const stages = [
  { no: '01', name: '圣迭戈海滩 → 圣玛利亚·诺维拉教堂', en: 'San Diego Beach → San María Novella', note: '15,000 米冲刺，横断赛的起点。乔尼第一次见识杰洛的铁球。' },
  { no: '02', name: '亚利桑那沙漠穿越', en: 'Arizona Desert', note: '圣遗体左眼争夺，迪亚哥在此继承「骇人恶兽」。' },
  { no: '03', name: '落基山脉突破', en: 'Rocky Mountains', note: '海拔与严寒的考验，砂男与曼登·提姆登场。' },
  { no: '04', name: '大草原上的小墓标', en: 'The Great Plains', note: '草原上的狙击战，替身「寂静之夜」的威胁逼近。' },
  { no: '05', name: '伊利诺伊天际线', en: 'Illinois Skyline', note: '芝加哥在望，赫特·潘兹与遗体线索交错。' },
  { no: '06', name: '密歇根湖路线', en: 'Lake Michigan', note: '湖畔决战，波克罗克的「好运」成为关键变量。' },
  { no: '07', name: '费城三角', en: 'Philadelphia Triangle', note: '遗体收集进入白热化，瓦伦泰的刺客全面出动。' },
  { no: '08', name: '两侧皆然', en: 'Both Sides, Now', note: '平行世界的门被打开，D4C 的秘密浮出水面。' },
  { no: '09', name: '曼哈顿狂想曲', en: 'Manhattan Rhapsody', note: '终点三一教堂，黄金旋转与 LOVE TRAIN 的终局。' }
]

/* ===== 主要人物 ===== */
const characters = [
  { name: '乔尼·乔斯达', jp: 'ジョニィ・ジョースター', en: 'Johnny Joestar', stand: '牙 / TUSK ACT 4', cv: '坂田将吾（2026 TV）', role: '主角', desc: '曾是天才骑手，因枪击下半身瘫痪。为重新站起来而追随杰洛的铁球参加 SBR，在旅途中从自暴自弃成长为直面命运的勇者。' },
  { name: '杰洛·齐贝林', jp: 'ジャイロ・ツェペリ', en: 'Gyro Zeppeli', stand: '铁球回转 / Ball Spin', cv: '阿座上洋平（2026 TV）', role: '主角', desc: '那不勒斯王国的医生兼死刑执行人，为替无辜少年马尔科求恩赦而参赛。传授乔尼「旋转」，最终以生命完成对瓦伦泰的重创。' },
  { name: '迪亚哥·布兰度', jp: 'ディエゴ・ブランドー', en: 'Diego Brando', stand: '骇人恶兽 / Scary Monsters', cv: '石川界人（2026 TV）／子安武人（游戏）', role: '主要对手', desc: '平行世界的 DIO。出身英国的贫穷天才骑手，为站上社会顶点不择手段。详见下方专题档案。' },
  { name: '法尼·瓦伦泰', jp: 'ファニー・ヴァレンタイン', en: 'Funny Valentine', stand: 'D4C / LOVE TRAIN', cv: '（2026 TV 阵容待官方公布）', role: '最终反派', desc: '第 23 任美国总统，把「爱国心」推向极端。为让美国成为世界命运的中心而收集圣遗体，以平行世界之力制造悖论。' },
  { name: '露西·史提尔', jp: 'ルーシー・スティール', en: 'Lucy Steel', stand: '—', cv: '高桥李依（2026 TV）', role: '关键变量', desc: '大赛主办者史帝芬的年轻妻子。从旁观者逐渐成为遗体争夺的核心人物，最终阻止了平行世界的迪亚哥。' },
  { name: '史帝芬·史提尔', jp: 'スティーブン・スティール', en: 'Steven Steel', stand: '—', cv: '三宅健太（2026 TV）', role: '主办者', desc: 'SBR 大赛的发起人，试图借赛事重振事业，却被总统的意图步步牵制。' },
  { name: '砂男', jp: 'サンドマン', en: 'Sandman', stand: '—（肉搏）', cv: '—', role: '强力竞争者', desc: '亚利桑那出身的印第安跑者，不用马而凭双腿参赛，目标是买回被夺走的土地。' },
  { name: '波克洛克', jp: 'ポコロコ', en: 'Pocoloco', stand: '—（好运）', cv: '—', role: '冠军', desc: '被预言拥有罕见好运的黑人农夫，实力平平却总能化险为夷，最终意外拿下 SBR 总冠军。' },
  { name: '赫特·潘兹', jp: 'ホット・パンツ', en: 'Hot Pants', stand: '奶油起司 / Cream Cheese', cv: '—', role: '参赛修女', desc: '男装参赛的修女，使用肉喷雾治疗与潜入，为赎罪而收集圣遗体。' },
  { name: '曼登·提姆', jp: 'マンデン・ティム', en: 'Mander Tim', stand: '孤独的我 / In My Own Little Corner', cv: '—', role: '牛仔', desc: '牛仔兼临时保安官助手，替身能使身体与绳索同化，在第三赛段与乔尼、杰洛交手。' }
]

/* ===== 声优阵容（2026 TV 动画官方公布） ===== */
const cast = [
  { role: '乔尼·乔斯达', tv: '坂田将吾', game: '梶裕贵', note: 'TV 版启用新生代声优' },
  { role: '杰洛·齐贝林', tv: '阿座上洋平', game: '三木真一郎', note: '' },
  { role: '迪亚哥·布兰度', tv: '石川界人', game: '子安武人', note: '重点：TV 与游戏声优不同' },
  { role: '露西·史提尔', tv: '高桥李依', game: '—', note: '' },
  { role: '史帝芬·史提尔', tv: '三宅健太', game: '—', note: '曾为第三部阿布德尔配音' }
]

/* ===== 经典台词 · 场景剧场（纯 CSS 动画） ===== */
const activeScene = ref(0)
const scenes = [
  {
    key: 'spin',
    tab: '黄金旋转',
    who: '杰洛·齐贝林',
    cv: 'CV 阿座上洋平（TV）',
    jp: '近道は遠回り、遠回りが近道だ！',
    cn: '最短的捷径就是绕远路，绕远路才是我最短的捷径。',
    source: 'Lesson 5 · 传给乔尼的最后教诲',
    color: '#f6c544'
  },
  {
    key: 'horse',
    tab: '绕远路的尽头',
    who: '乔尼·乔斯达',
    cv: 'CV 坂田将吾（TV）',
    jp: '本当に本当に、なんて遠い廻り道…',
    cn: '真的，真的……真是绕了好大一圈啊……',
    source: '第 22 卷 · 觉醒 TUSK ACT 4 前的名台词',
    color: '#3fe0f2'
  },
  {
    key: 'world',
    tab: '时间与咆哮',
    who: '迪亚哥·布兰度 / DIO',
    cv: 'CV 石川界人（TV）· 子安武人（游戏 / DIO）',
    jp: 'WRYYYYYYYY——！時間よ止まれ！',
    cn: 'WRYYYYYYYY——！时间啊，停止吧！',
    source: '平行世界迪亚哥继承的 DIO 之咆哮，游戏版由子安武人献声',
    color: '#e23b54'
  },
  {
    key: 'justice',
    tab: '正义的独白',
    who: '法尼·瓦伦泰',
    cv: 'CV 2026 TV 阵容待官方公布',
    jp: '我が心と行動に一点の曇りなし……！全てが「正義」だ',
    cn: '吾心吾行澄如明镜，所作所为皆为正义。',
    source: '最终决战 · 与乔尼谈判时的最后台词',
    color: '#c89320'
  }
]
</script>

<template>
  <div class="sbr-page">
    <!-- ============ HERO：主题背景图 ============ -->
    <header class="sbr-hero">
      <div class="hero-bg"></div>
      <!-- 主角与主要角色肖像：半透明融入西部风景背景 -->
      <div class="hero-cast" aria-hidden="true">
        <span class="cast-figure cast-johnny"></span>
        <span class="cast-figure cast-diego"></span>
        <span class="cast-figure cast-lucy"></span>
        <span class="cast-figure cast-valentine"></span>
        <span class="cast-figure cast-gyro"></span>
      </div>
      <div class="hero-veil"></div>
      <div class="hero-stars">
        <span v-for="i in 18" :key="i" :style="{ left: ((i * 37) % 96) + '%', top: ((i * 53) % 70) + '%', animationDelay: (i % 6) + 's' }">✦</span>
      </div>

      <button class="sbr-back" type="button" @click="emit('back')">← 返回冒险</button>

      <div class="hero-inner">
        <div class="hero-kicker">JOJO'S BIZARRE ADVENTURE · PART 7</div>
        <h1 class="hero-title"><span>STEEL</span><br /><em>BALL</em> RUN</h1>
        <div class="hero-jp">ジョジョの奇妙な冒険 第7部 スティール・ボール・ラン</div>
        <div class="hero-sub">飙 马 野 郎 · 黄 金 旋 转 的 西 部 史 诗</div>
        <div class="hero-meta">
          <span>荒木飞吕彦</span><i></i><span>2004–2011 连载</span><i></i><span>全 24 卷 · 95 话</span><i></i><span>2026 动画化</span>
        </div>
        <a class="hero-scroll" href="#sbr-intro">向下探索 ↓</a>
      </div>
    </header>

    <!-- ============ 概览 ============ -->
    <section class="sbr-section" id="sbr-intro">
      <div class="sec-head"><span class="sec-no">01</span><h2>作品概览</h2><span class="sec-line"></span></div>
      <p class="lead">
        《STEEL BALL RUN》是荒木飞吕彦创作的《JOJO 的奇妙冒险》第七部，2004 年至 2011 年连载于《周刊少年 Jump》，后移籍《Ultra Jump》，单行本全 24 卷。
        故事发生在 1890 年的美国——一场奖金高达 5000 万美元、横贯北美大陆的赛马「SBR 大赛」拉开帷幕。
        替身概念被重新阐释为「恶魔掌心」与「旋转」的产物，荒木以西部公路片的方式，写下了关于命运、救赎与黄金精神的最恢弘一部。
      </p>
      <div class="fact-grid">
        <div class="fact"><span>时代舞台</span><b>1890 · 美国</b></div>
        <div class="fact"><span>赛事距离</span><b>约 6000 公里</b></div>
        <div class="fact"><span>优胜奖金</span><b>5000 万美元</b></div>
        <div class="fact"><span>核心概念</span><b>Spin 旋转 · 圣遗体</b></div>
      </div>
    </section>

    <!-- ============ 剧情 ============ -->
    <section class="sbr-section" id="sbr-story">
      <div class="sec-head"><span class="sec-no">02</span><h2>剧情梗概</h2><span class="sec-line"></span></div>
      <div class="story-cols">
        <div class="story-card">
          <h3>① 起点：跌入谷底的天才</h3>
          <p>曾为天才骑手的乔尼·乔斯达因枪击事件下半身瘫痪，人生跌至谷底。在赛场边，他被神秘男子杰洛·齐贝林手中的铁球所展现的治愈之力震撼——那颗球能让濒死的马重新奔跑。为了让自己再次站起来，乔尼决定与杰洛一同参加横断大陆的 SBR 大赛。</p>
        </div>
        <div class="story-card">
          <h3>② 主线：圣遗体的争夺</h3>
          <p>随着赛程推进，两人卷入「圣者遗体」的争夺。传说约一千九百年前逝去的圣人，其遗体分为九部位散落北美，拥有者将获得千年荣光。第 23 任美国总统法尼·瓦伦泰觊觎遗体，不断派出刺客；而乔尼与杰洛也在战斗中逐渐理解「旋转」的真谛，替身不断进化。</p>
        </div>
        <div class="story-card">
          <h3>③ 终局：黄金旋转与平行世界</h3>
          <p>瓦伦泰以替身 D4C 穿梭平行世界，并获得「LOVE TRAIN」的不败之力。杰洛领悟更高层次的旋转，却在决战中牺牲。乔尼继承挚友的铁球，以「牙 ACT 4」的黄金旋转突破 Love Train，击败瓦伦泰。然而故事并未结束——另一个平行世界的迪亚哥携「THE WORLD」登场，展开最后争夺。</p>
        </div>
        <div class="story-card">
          <h3>④ 终点：重新站起来的男人</h3>
          <p>真正阻止平行世界迪亚哥的人是露西·史提尔。最终赢得 SBR 总冠军的既不是乔尼也不是杰洛，而是运气惊人的波克洛克。而乔尼在这段旅程中获得的，远不止是双腿的康复——这是一个跌倒的人如何重新学会前进的故事。</p>
        </div>
      </div>
      <button class="spoiler-toggle" type="button" @click="revealSpoiler = !revealSpoiler">
        {{ revealSpoiler ? '▾ 收起结局详解（含原作剧透）' : '▸ 展开结局详解（含原作剧透）' }}
      </button>
      <div v-if="revealSpoiler" class="spoiler-box">
        <b>【原作结局 · 重大剧透】</b> 杰洛在与瓦伦泰的决战中死亡；乔尼以 TUSK ACT 4 击杀瓦伦泰。此后平行世界的迪亚哥（拥有时间停止替身 THE WORLD）现身，与乔尼争夺遗体与冠军，最终被露西阻止并死亡。乔尼带着对杰洛的悼念继续前行，而总冠军由波克洛克获得。
      </div>

      <div class="stages-wrap">
        <h3 class="sub-title">九大赛段 · STAGES</h3>
        <div class="stages">
          <div v-for="s in stages" :key="s.no" class="stage">
            <span class="stage-no">{{ s.no }}</span>
            <div><b>{{ s.name }}</b><small>{{ s.en }}</small><p>{{ s.note }}</p></div>
          </div>
        </div>
      </div>
    </section>

    <!-- ============ 人物 ============ -->
    <section class="sbr-section" id="sbr-people">
      <div class="sec-head"><span class="sec-no">03</span><h2>主要人物</h2><span class="sec-line"></span></div>
      <div class="char-grid">
        <article v-for="c in characters" :key="c.en" class="char-card" :class="{ focus: c.en === 'Diego Brando' }">
          <div class="char-top">
            <span class="char-role">{{ c.role }}</span>
            <h3>{{ c.name }}</h3>
            <div class="char-jp">{{ c.jp }} · {{ c.en }}</div>
          </div>
          <p>{{ c.desc }}</p>
          <div class="char-foot"><span>替身：{{ c.stand }}</span><span>CV：{{ c.cv }}</span></div>
        </article>
      </div>
    </section>

    <!-- ============ 声优阵容 ============ -->
    <section class="sbr-section" id="sbr-cast">
      <div class="sec-head"><span class="sec-no">04</span><h2>声优阵容</h2><span class="sec-line"></span></div>
      <p class="lead">2026 年 Netflix 动画版公布了主要角色声优。作为系列新作，TV 版启用了新一代声优；下表同时列出游戏版的差异，其中<b>迪亚哥一角的声优更替最为粉丝热议</b>。</p>
      <div class="cast-table">
        <div class="cast-row cast-head"><span>角色</span><span>TV 动画（2026）</span><span>游戏版</span><span>备注</span></div>
        <div v-for="r in cast" :key="r.role" class="cast-row" :class="{ hot: r.role === '迪亚哥·布兰度' }">
          <span>{{ r.role }}</span><span>{{ r.tv }}</span><span>{{ r.game }}</span><span>{{ r.note }}</span>
        </div>
      </div>
      <p class="cast-note">※ 迪亚哥幼年期由中野彩麻（TV）配音；VOMIC 版由加藤将之配音；子安武人曾在《ASB》《EOH》等游戏中为迪亚哥献声。</p>
    </section>

    <!-- ============ 迪亚哥专题 ============ -->
    <section class="sbr-section diego" id="sbr-diego">
      <div class="sec-head"><span class="sec-no">05</span><h2>焦点档案 · 迪亚哥·布兰度</h2><span class="sec-line"></span></div>

      <div class="diego-hero">
        <div class="diego-mark">DIO</div>
        <div class="diego-quote">WRYYYYYYYYYY——！</div>
        <div class="diego-name">ディエゴ・ブランドー / Diego Brando</div>
        <div class="diego-tags"><span>平行世界的 DIO</span><span>天才骑手</span><span>野心家</span><span>反英雄</span></div>
      </div>

      <div class="diego-cols">
        <div class="diego-card">
          <h3>档案</h3>
          <dl class="profile">
            <div><dt>本名</dt><dd>迪亚哥·布兰度（Diego Brando）</dd></div>
            <div><dt>年龄 / 身高</dt><dd>20 岁 / 180cm（动画设定）</dd></div>
            <div><dt>出身</dt><dd>英国 · SBR 大赛选手</dd></div>
            <div><dt>爱马</dt><dd>银色子弹（Silver Bullet，阿拉伯马）</dd></div>
            <div><dt>替身</dt><dd>骇人恶兽（Scary Monsters）／后期：世界（THE WORLD）</dd></div>
            <div><dt>声优</dt><dd>石川界人（2026 TV）／子安武人（游戏）／加藤将之（VOMIC）</dd></div>
          </dl>
        </div>
        <div class="diego-card">
          <h3>生平与性格</h3>
          <p>迪亚哥刚出生便被父亲达利欧抛弃，与母亲在农场相依为命，母亲不久后也病逝。幼年的他目睹社会的不公，遂立誓不择手段站上顶点。他以天才骑手身份参加 SBR，追求胜利、财富、名誉与支配地位。</p>
          <p>他傲慢、冷酷、极具攻击性，为取胜不惜作弊与暗算；同时又有着对母亲的执念与近乎自毁的倔强。这种「宁可与全世界为敌也要登顶」的姿态，使他成为平行世界里 DIO 的镜像。</p>
        </div>
        <div class="diego-card">
          <h3>替身能力</h3>
          <p><b>骇人恶兽（Scary Monsters）：</b>源自大卫·鲍伊的同名歌曲。能力者可将自己或他人变成恐龙，并借恐龙化的身体高速移动、撕咬。迪亚哥在第二赛段取得圣遗体左眼后继承了费迪南特博士的这一替身。</p>
          <p><b>世界（THE WORLD）：</b>平行世界的另一个迪亚哥所拥有的替身，能够停止时间——这也是系列粉丝最熟悉的 DIO 能力在 SBR 世界的重现。</p>
        </div>
        <div class="diego-card">
          <h3>剧中轨迹</h3>
          <p>第二赛段他从费迪南特博士处继承恐龙化替身，一度成为乔尼与杰洛的强劲威胁。终局之后，平行世界的迪亚哥携「世界」现身，以时间停止与乔尼展开最后的争夺，最终被露西·史提尔阻止。基本世界的迪亚哥则死于火车之下——「被麻美」的经典命运再度上演。</p>
        </div>
      </div>
    </section>

    <!-- ============ 子安武人专题 ============ -->
    <section class="sbr-section koyasu" id="sbr-koyasu">
      <div class="sec-head"><span class="sec-no">06</span><h2>焦点档案 · 子安武人</h2><span class="sec-line"></span></div>

      <div class="koyasu-banner">
        <div class="koyasu-kanji">子安 武人</div>
        <div class="koyasu-romaji">Koyasu Takehito · こやす たけひと</div>
        <div class="koyasu-quote">「恶人的魅力，来自那份毫不掩饰的自信。」</div>
      </div>

      <div class="koyasu-cols">
        <div class="diego-card">
          <h3>履历</h3>
          <dl class="profile">
            <div><dt>出生</dt><dd>1967 年 5 月 5 日 · 神奈川县横滨市南区</dd></div>
            <div><dt>血型 / 身高</dt><dd>A 型 / 176cm</dd></div>
            <div><dt>所属</dt><dd>T's Factory（1998 年创立，本人任社长）</dd></div>
            <div><dt>出道</dt><dd>1988 年《IQ 零蛋多毛狗》；1989 年《天空战记》首次正式角色</dd></div>
            <div><dt>其他身份</dt><dd>小说创作、绘画、作曲作词；音乐组合 Weiß Kreuz 成员</dd></div>
          </dl>
        </div>
        <div class="diego-card">
          <h3>声线特色</h3>
          <p>子安武人的音色兼具中低音的厚度与优雅的冷感，擅长塑造高智商反派与贵族式角色，被戏称为「恶役专业户」。他能在一句台词里从从容不迫切换到近乎癫狂，这种张力让 DIO 的「WRYYYY」成为声优史上的标志性演出。</p>
          <p>除反派外，他也留下大量喜剧与热血角色，证明其戏路远比「冷酷反派」宽广。</p>
        </div>
        <div class="diego-card wide">
          <h3>与 JOJO 的缘分：DIO、迪亚哥，以及 2026 的更替</h3>
          <p>在 JOJO 系列中，子安武人为<b>第三部《星尘斗士》的迪奥·布兰度（DIO）</b>献声，那句「WRYYYYYYYY」与「时间啊，停止吧！」成为一代观众的记忆。由于《飙马野郎》的迪亚哥是平行世界的 DIO，他在《JOJO 的奇妙冒险 全明星之战（ASB）》《EOH》等游戏中也为迪亚哥配音，让两个世界的「DIO」在同一声线下交汇。</p>
          <p><b>需要注意的是：</b>2026 年 Netflix 动画版《飙马野郎》中，迪亚哥·布兰度的声优改由<b>石川界人</b>担任，子安武人不再为该角色献声。这一更换在粉丝间引发热议。本页按官方公布信息如实标注：TV 版为石川界人，游戏版为子安武人。</p>
        </div>
        <div class="diego-card wide">
          <h3>代表作</h3>
          <p class="works">《JOJO 的奇妙冒险》迪奥·布兰度 · 《进击的巨人》吉克·耶格尔 · 《海贼王》青雉／库赞 · 《银魂》高杉晋助 · 《头文字D》高桥凉介 · 《机动战士高达 SEED》穆·拉·弗拉达 · 《Keroro 军曹》Kururu 曹长 · 《Re:从零开始的异世界生活》罗兹瓦尔·L·梅札斯 · 《紫罗兰永恒花园》克劳迪亚·霍金斯 · 《宇宙骑士》相羽星野</p>
        </div>
      </div>
    </section>

    <!-- ============ 经典台词 · 场景动画剧场 ============ -->
    <section class="sbr-section quotes-section" id="sbr-quotes">
      <div class="sec-head"><span class="sec-no">07</span><h2>经典台词 · 名场面剧场</h2><span class="sec-line"></span></div>
      <p class="lead">四幕名场面，以纯 CSS 动画重现。点击选项卡切换场景与台词。</p>

      <div class="theater">
        <div class="theater-tabs">
          <button
            v-for="(s, i) in scenes"
            :key="s.key"
            type="button"
            class="theater-tab"
            :class="{ active: activeScene === i }"
            :style="{ '--tab-color': s.color }"
            @click="activeScene = i"
          >
            <span class="tt-no">0{{ i + 1 }}</span>{{ s.tab }}
          </button>
        </div>

        <!-- 场景舞台：随所选场景切换，key 触发重播动画 -->
        <div class="stage-frame" :class="'stage-' + scenes[activeScene].key">
          <div class="stage-sky"></div>

          <!-- 场景 1：黄金旋转（铁球与螺旋） -->
          <template v-if="scenes[activeScene].key === 'spin'">
            <div class="spiral-ring">
              <div class="spiral-ring2"></div>
            </div>
            <div class="spin-ball"><span></span></div>
            <div class="spin-ball spin-ball2"><span></span></div>
            <div class="dust dust-a"></div><div class="dust dust-b"></div>
          </template>

          <!-- 场景 2：策马奔腾（剪影与沙尘） -->
          <template v-else-if="scenes[activeScene].key === 'horse'">
            <div class="mesa mesa-1"></div><div class="mesa mesa-2"></div>
            <div class="sun"></div>
            <div class="ground-line"></div>
            <div class="rider">
              <div class="rider-body"></div>
              <div class="horse"><i class="leg l1"></i><i class="leg l2"></i><i class="leg l3"></i><i class="leg l4"></i><i class="tail"></i></div>
            </div>
            <div class="dust-cloud"><i></i><i></i><i></i><i></i></div>
          </template>

          <!-- 场景 3：时间停止（定格时钟与红光） -->
          <template v-else-if="scenes[activeScene].key === 'world'">
            <div class="time-flash"></div>
            <div class="clock"><div class="hand hand-h"></div><div class="hand hand-m"></div><div class="clock-core"></div></div>
            <div class="time-words"><span>時 間 よ</span><span>止 ま れ</span></div>
            <div class="red-rays"></div>
          </template>

          <!-- 场景 4：正义的独白（星条与总统剪影） -->
          <template v-else>
            <div class="justice-stars"><span v-for="i in 12" :key="i" :style="{ left: ((i * 29) % 94) + '%', top: ((i * 47) % 70) + '%', animationDelay: (i % 5) + 's' }">★</span></div>
            <div class="president">
              <div class="p-head"></div><div class="p-body"></div>
            </div>
            <div class="justice-glow"></div>
          </template>

          <!-- 台词语录层 -->
          <div class="quote-layer" :key="activeScene">
            <div class="quote-who">{{ scenes[activeScene].who }}</div>
            <div class="quote-jp">「{{ scenes[activeScene].jp }}」</div>
            <div class="quote-cn">{{ scenes[activeScene].cn }}</div>
            <div class="quote-source">— {{ scenes[activeScene].source }}</div>
            <div class="quote-cv">{{ scenes[activeScene].cv }}</div>
          </div>
        </div>
      </div>

      <!-- 台词卡片墙：一览全部金句 -->
      <h3 class="sub-title">名台词一览 · QUOTE WALL</h3>
      <div class="quote-wall">
        <div
          v-for="(s, i) in scenes"
          :key="s.key"
          class="quote-card"
          :style="{ '--card-color': s.color }"
          @click="activeScene = i"
        >
          <div class="qc-index">0{{ i + 1 }}</div>
          <div class="qc-text">{{ s.cn }}</div>
          <div class="qc-foot"><b>{{ s.who }}</b><span>{{ s.source }}</span></div>
        </div>
      </div>
    </section>

    <footer class="sbr-foot">
      <div>—— 人类的赞歌，就是勇气的赞歌 ——</div>
      <button type="button" @click="emit('back')">返回冒险首页 →</button>
    </footer>
  </div>
</template>

<style scoped>
.sbr-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #0a0a10 0%, #101019 40%, #0a0a10 100%);
  color: var(--paper, #f4ecd8);
  font-family: "Noto Serif JP", "Songti SC", "STSong", serif;
  overflow: hidden;
}

/* ===== HERO ===== */
.sbr-hero { position: relative; min-height: 88vh; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.hero-bg {
  position: absolute; inset: 0;
  background: url('/sbr-hero.png') center 42% / cover no-repeat;
  filter: saturate(1.05) contrast(1.05);
  animation: heroZoom 24s ease-in-out infinite alternate;
}
@keyframes heroZoom { from { transform: scale(1); } to { transform: scale(1.08); } }

/* 角色肖像背景层：与风景融合，不遮挡中央标题 */
.hero-cast { position: absolute; inset: 0; z-index: 1; pointer-events: none; overflow: hidden; }
.cast-figure {
  position: absolute; bottom: -4%; width: 19vw; height: 84%;
  background-repeat: no-repeat; background-size: cover; background-position: center 16%;
  opacity: .46; mix-blend-mode: screen;
  filter: contrast(1.08) saturate(1.06) blur(.3px);
  -webkit-mask-image: linear-gradient(to top, transparent 0%, #000 14%, #000 72%, transparent 100%);
  mask-image: linear-gradient(to top, transparent 0%, #000 14%, #000 72%, transparent 100%);
  animation: castDrift 16s ease-in-out infinite alternate;
}
@keyframes castDrift { from { transform: translateY(0) scale(1); } to { transform: translateY(-14px) scale(1.02); } }
.cast-johnny { left: 3%; background-image: url('/characters/johnny.png'); animation-delay: 0s; }
.cast-diego { left: 22%; background-image: url('/characters/diego.png'); animation-delay: -4s; }
.cast-lucy { left: 42%; width: 16vw; opacity: .42; background-image: url('/characters/lucy.png'); animation-delay: -8s; }
.cast-valentine { right: 22%; background-image: url('/characters/valentine.png'); animation-delay: -6s; }
.cast-gyro { right: 3%; background-image: url('/characters/gyro.png'); animation-delay: -2s; }

.hero-veil {
  position: absolute; inset: 0; z-index: 2;
  background: linear-gradient(180deg, rgba(5,5,10,.32) 0%, rgba(5,5,10,.15) 35%, rgba(5,5,10,.88) 88%, #0a0a10 100%),
              radial-gradient(70% 60% at 50% 40%, rgba(246,197,68,.12), transparent 70%);
}
.hero-stars { position: absolute; inset: 0; z-index: 3; pointer-events: none; }
.hero-stars span { position: absolute; color: var(--gold-bright, #ffe08a); font-size: 14px; opacity: .55; animation: twinkle 3.5s ease-in-out infinite; }
@keyframes twinkle { 0%,100% { opacity: .2; transform: scale(.8); } 50% { opacity: .85; transform: scale(1.25); } }
.sbr-back {
  position: absolute; top: 26px; left: 30px; z-index: 5;
  background: rgba(10,10,16,.68); color: var(--gold-bright, #ffe08a);
  border: 1px solid rgba(246,197,68,.55); border-radius: 999px;
  padding: 9px 20px; font-size: 13px; letter-spacing: .12em; cursor: pointer;
  transition: .25s;
}
.sbr-back:hover { background: rgba(246,197,68,.18); box-shadow: 0 0 18px rgba(246,197,68,.35); }
.hero-inner { position: relative; z-index: 4; text-align: center; padding: 40px 20px; }
.hero-kicker { font-family: "Cinzel", Georgia, serif; letter-spacing: .48em; font-size: 12px; color: var(--teal-glow, #3fe0f2); margin-bottom: 22px; }
.hero-title {
  font-family: "Cinzel", Georgia, serif; font-size: clamp(52px, 11vw, 132px); line-height: .92;
  letter-spacing: .04em; font-weight: 900; color: transparent;
  background: linear-gradient(180deg, #fff6d8 5%, var(--gold, #f6c544) 48%, #b47a12 100%);
  -webkit-background-clip: text; background-clip: text;
  text-shadow: 0 8px 40px rgba(0,0,0,.55); margin-bottom: 18px;
}
.hero-title em { font-style: normal; color: transparent; background: linear-gradient(180deg, #fff, var(--teal-glow, #3fe0f2)); -webkit-background-clip: text; background-clip: text; }
.hero-jp { font-size: 14px; letter-spacing: .18em; color: rgba(244,236,216,.78); margin-bottom: 10px; }
.hero-sub { font-size: clamp(14px, 2vw, 20px); letter-spacing: .5em; color: var(--gold-bright, #ffe08a); margin: 18px 0 26px; text-shadow: 0 0 18px rgba(246,197,68,.5); }
.hero-meta { display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 14px; font-size: 12px; letter-spacing: .1em; color: rgba(244,236,216,.65); }
.hero-meta i { width: 4px; height: 4px; border-radius: 50%; background: var(--gold, #f6c544); }
.hero-scroll { display: inline-block; margin-top: 46px; font-size: 12px; letter-spacing: .28em; color: var(--teal-glow, #3fe0f2); text-decoration: none; animation: float 2s ease-in-out infinite; }
@keyframes float { 0%,100% { transform: translateY(0); } 50% { transform: translateY(8px); } }

/* ===== 通用区块 ===== */
.sbr-section { max-width: 1080px; margin: 0 auto; padding: 76px 28px 20px; }
.sec-head { display: flex; align-items: center; gap: 16px; margin-bottom: 30px; }
.sec-no { font-family: "Cinzel", Georgia, serif; font-size: 13px; color: var(--teal-glow, #3fe0f2); border: 1px solid rgba(63,224,242,.4); border-radius: 4px; padding: 4px 9px; }
.sec-head h2 { font-size: clamp(22px, 3.4vw, 34px); letter-spacing: .12em; color: var(--gold-bright, #ffe08a); white-space: nowrap; }
.sec-line { flex: 1; height: 1px; background: linear-gradient(90deg, rgba(246,197,68,.6), transparent); }
.lead { font-size: 15px; line-height: 2.05; color: rgba(244,236,216,.86); margin-bottom: 26px; text-align: justify; }
.lead b { color: var(--gold-bright, #ffe08a); }
.sub-title { font-size: 18px; letter-spacing: .2em; color: var(--gold, #f6c544); margin: 46px 0 20px; }

.fact-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 14px; }
.fact { border: 1px solid rgba(246,197,68,.28); background: rgba(246,197,68,.04); padding: 18px 20px; border-radius: 8px; text-align: center; }
.fact span { display: block; font-size: 11px; letter-spacing: .2em; color: rgba(244,236,216,.55); margin-bottom: 8px; }
.fact b { font-size: 17px; color: var(--gold-bright, #ffe08a); }

/* ===== 剧情 ===== */
.story-cols { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 18px; }
.story-card { border-left: 3px solid var(--gold, #f6c544); background: linear-gradient(135deg, rgba(246,197,68,.07), rgba(10,10,16,.4)); padding: 20px 22px; border-radius: 0 10px 10px 0; }
.story-card h3 { font-size: 16px; color: var(--gold-bright, #ffe08a); letter-spacing: .08em; margin-bottom: 12px; }
.story-card p { font-size: 14px; line-height: 1.95; color: rgba(244,236,216,.82); text-align: justify; }
.spoiler-toggle { margin-top: 22px; background: transparent; border: 1px dashed rgba(226,59,84,.6); color: #ff8fa0; padding: 10px 18px; border-radius: 6px; font-size: 13px; cursor: pointer; letter-spacing: .08em; }
.spoiler-toggle:hover { background: rgba(226,59,84,.12); }
.spoiler-box { margin-top: 12px; border: 1px solid rgba(226,59,84,.35); background: rgba(226,59,84,.08); padding: 18px 20px; border-radius: 8px; font-size: 14px; line-height: 2; color: rgba(244,236,216,.88); }
.spoiler-box b { color: #ff8fa0; }

.stages { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 12px; }
.stage { display: flex; gap: 14px; border: 1px solid rgba(31,182,201,.22); background: rgba(31,182,201,.045); border-radius: 8px; padding: 14px 16px; }
.stage-no { font-family: "Cinzel", Georgia, serif; font-size: 22px; color: rgba(63,224,242,.55); min-width: 34px; }
.stage b { font-size: 14px; color: var(--paper, #f4ecd8); }
.stage small { display: block; font-size: 10px; letter-spacing: .08em; color: rgba(244,236,216,.45); margin: 3px 0 6px; }
.stage p { font-size: 12px; line-height: 1.75; color: rgba(244,236,216,.68); }

/* ===== 人物 ===== */
.char-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(310px, 1fr)); gap: 18px; }
.char-card { border: 1px solid rgba(246,197,68,.24); border-radius: 10px; background: linear-gradient(160deg, rgba(20,20,28,.9), rgba(10,10,16,.75)); padding: 22px; transition: .3s; }
.char-card:hover { transform: translateY(-4px); border-color: rgba(246,197,68,.7); box-shadow: 0 12px 36px rgba(0,0,0,.45); }
.char-card.focus { border-color: rgba(226,59,84,.7); background: linear-gradient(160deg, rgba(226,59,84,.12), rgba(10,10,16,.8)); }
.char-role { font-size: 10px; letter-spacing: .2em; color: var(--teal-glow, #3fe0f2); border: 1px solid rgba(63,224,242,.35); padding: 3px 10px; border-radius: 999px; }
.char-card h3 { font-size: 20px; color: var(--gold-bright, #ffe08a); margin: 12px 0 4px; letter-spacing: .06em; }
.char-jp { font-size: 11px; color: rgba(244,236,216,.5); margin-bottom: 14px; }
.char-card p { font-size: 13px; line-height: 1.95; color: rgba(244,236,216,.8); min-height: 100px; }
.char-foot { margin-top: 14px; padding-top: 12px; border-top: 1px dashed rgba(246,197,68,.2); display: flex; flex-direction: column; gap: 5px; font-size: 11px; color: rgba(244,236,216,.6); }
.char-foot span b { color: var(--gold, #f6c544); }

/* ===== 声优表 ===== */
.cast-table { border: 1px solid rgba(246,197,68,.25); border-radius: 10px; overflow: hidden; }
.cast-row { display: grid; grid-template-columns: 1.1fr 1fr 1fr 1.4fr; gap: 10px; padding: 14px 18px; font-size: 13px; border-bottom: 1px solid rgba(246,197,68,.14); align-items: center; }
.cast-row:last-child { border-bottom: none; }
.cast-row:nth-child(even) { background: rgba(246,197,68,.035); }
.cast-head { background: rgba(246,197,68,.12) !important; color: var(--gold-bright, #ffe08a); font-size: 12px; letter-spacing: .1em; }
.cast-row.hot { background: rgba(226,59,84,.14); box-shadow: inset 3px 0 0 var(--crimson, #e23b54); }
.cast-row.hot span:nth-child(2) { color: var(--gold-bright, #ffe08a); font-weight: bold; }
.cast-row span:first-child { color: var(--gold, #f6c544); }
.cast-note { margin-top: 14px; font-size: 12px; color: rgba(244,236,216,.55); line-height: 1.8; }

/* ===== 迪亚哥 ===== */
.diego-hero { position: relative; border: 1px solid rgba(226,59,84,.4); border-radius: 12px; padding: 46px 28px 38px; text-align: center; background: radial-gradient(circle at 50% 0%, rgba(226,59,84,.25), transparent 65%), linear-gradient(180deg, rgba(20,10,14,.9), rgba(10,10,16,.9)); overflow: hidden; margin-bottom: 22px; }
.diego-mark { position: absolute; top: -30px; left: 50%; transform: translateX(-50%); font-family: "Cinzel", Georgia, serif; font-size: 220px; font-weight: 900; color: rgba(226,59,84,.07); line-height: 1; pointer-events: none; }
.diego-quote { position: relative; font-family: "Cinzel", Georgia, serif; font-size: clamp(28px, 6vw, 62px); font-weight: 900; letter-spacing: .06em; color: transparent; background: linear-gradient(180deg, #fff, #ff6b7f 60%, #a01028); -webkit-background-clip: text; background-clip: text; text-shadow: 0 0 50px rgba(226,59,84,.35); }
.diego-name { position: relative; margin-top: 10px; font-size: 14px; letter-spacing: .2em; color: var(--paper, #f4ecd8); }
.diego-tags { position: relative; margin-top: 18px; display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
.diego-tags span { font-size: 11px; letter-spacing: .1em; padding: 5px 14px; border: 1px solid rgba(226,59,84,.45); border-radius: 999px; color: #ff9fb0; }
.diego-cols, .koyasu-cols { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 16px; }
.diego-card { border: 1px solid rgba(246,197,68,.22); border-radius: 10px; padding: 20px 22px; background: linear-gradient(160deg, rgba(20,20,28,.85), rgba(10,10,16,.7)); }
.diego-card.wide { grid-column: 1 / -1; }
.diego-card h3 { font-size: 16px; color: var(--gold-bright, #ffe08a); letter-spacing: .12em; margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid rgba(246,197,68,.2); }
.diego-card p { font-size: 13px; line-height: 2; color: rgba(244,236,216,.82); margin-bottom: 10px; text-align: justify; }
.diego-card p b { color: var(--gold, #f6c544); }
.profile div { display: grid; grid-template-columns: 90px 1fr; gap: 10px; padding: 7px 0; border-bottom: 1px dashed rgba(246,197,68,.12); font-size: 13px; }
.profile dt { color: rgba(244,236,216,.5); }
.profile dd { color: var(--paper, #f4ecd8); }

/* ===== 子安武人 ===== */
.koyasu-banner { position: relative; border: 1px solid rgba(63,224,242,.35); border-radius: 12px; padding: 40px 26px; text-align: center; background: radial-gradient(circle at 50% 0%, rgba(31,182,201,.22), transparent 65%), linear-gradient(180deg, rgba(10,20,26,.9), rgba(10,10,16,.9)); margin-bottom: 22px; overflow: hidden; }
.koyasu-kanji { font-size: clamp(34px, 7vw, 68px); letter-spacing: .3em; color: transparent; background: linear-gradient(180deg, #fff, var(--teal-glow, #3fe0f2)); -webkit-background-clip: text; background-clip: text; font-weight: 900; }
.koyasu-romaji { margin-top: 8px; font-family: "Cinzel", Georgia, serif; font-size: 13px; letter-spacing: .25em; color: rgba(244,236,216,.65); }
.koyasu-quote { margin-top: 20px; font-size: 14px; color: var(--gold-bright, #ffe08a); letter-spacing: .1em; }
.works { line-height: 2.2 !important; font-size: 12.5px !important; }

/* ===== 页脚 ===== */
.sbr-foot { text-align: center; padding: 80px 20px 60px; color: rgba(246,197,68,.45); font-size: 13px; letter-spacing: .22em; }
.sbr-foot button { display: block; margin: 22px auto 0; background: transparent; border: 1px solid rgba(246,197,68,.45); color: var(--gold-bright, #ffe08a); padding: 12px 34px; border-radius: 999px; cursor: pointer; letter-spacing: .1em; transition: .25s; }
.sbr-foot button:hover { background: rgba(246,197,68,.12); }

/* ===== 经典台词 · 场景动画剧场 ===== */
.theater { border: 1px solid rgba(246,197,68,.3); border-radius: 12px; overflow: hidden; background: #08080e; }
.theater-tabs { display: flex; flex-wrap: wrap; background: linear-gradient(180deg, rgba(246,197,68,.1), transparent); border-bottom: 1px solid rgba(246,197,68,.2); }
.theater-tab {
  flex: 1 1 150px; background: transparent; border: none; border-right: 1px solid rgba(246,197,68,.12);
  color: rgba(244,236,216,.6); padding: 14px 10px; cursor: pointer; font-family: inherit; font-size: 13px;
  letter-spacing: .1em; transition: .3s; position: relative;
}
.theater-tab:last-child { border-right: none; }
.theater-tab .tt-no { display: block; font-family: "Cinzel", Georgia, serif; font-size: 10px; color: rgba(244,236,216,.35); margin-bottom: 3px; }
.theater-tab:hover { color: var(--tab-color); background: rgba(246,197,68,.06); }
.theater-tab.active { color: var(--tab-color); background: rgba(246,197,68,.1); text-shadow: 0 0 14px var(--tab-color); }
.theater-tab.active::after { content: ''; position: absolute; left: 12%; right: 12%; bottom: 0; height: 2px; background: var(--tab-color); box-shadow: 0 0 12px var(--tab-color); }

.stage-frame { position: relative; height: 380px; overflow: hidden; background: linear-gradient(180deg, #141a24 0%, #1a1420 60%, #0a0a10 100%); }
.stage-sky { position: absolute; inset: 0; background: radial-gradient(80% 70% at 50% 20%, rgba(246,197,68,.16), transparent 70%); }
.quote-layer { position: absolute; left: 0; right: 0; bottom: 0; padding: 26px 30px 22px; background: linear-gradient(180deg, transparent, rgba(5,5,10,.9) 40%); z-index: 20; }
.quote-who { font-size: 11px; letter-spacing: .24em; color: var(--gold, #f6c544); margin-bottom: 6px; }
.quote-jp { font-size: clamp(15px, 2.4vw, 21px); color: var(--gold-bright, #ffe08a); margin-bottom: 6px; animation: quoteIn .7s ease both; }
.quote-cn { font-size: 13px; color: rgba(244,236,216,.9); margin-bottom: 8px; animation: quoteIn .9s ease both; }
.quote-source { font-size: 11px; color: rgba(244,236,216,.55); animation: quoteIn 1.1s ease both; }
.quote-cv { font-size: 10px; color: var(--teal-glow, #3fe0f2); margin-top: 6px; letter-spacing: .06em; animation: quoteIn 1.3s ease both; }
@keyframes quoteIn { from { opacity: 0; transform: translateY(14px); } to { opacity: 1; transform: translateY(0); } }

/* 场景 1：黄金旋转 */
.stage-spin { background: radial-gradient(circle at 45% 42%, rgba(246,197,68,.2), transparent 55%), #0d1018; }
.spiral-ring { position: absolute; left: 45%; top: 42%; width: 210px; height: 210px; margin: -105px 0 0 -105px; border: 2px solid rgba(246,197,68,.35); border-radius: 50%; animation: spiralSpin 4s linear infinite; }
.spiral-ring::before, .spiral-ring::after { content: ''; position: absolute; inset: 22px; border: 1px dashed rgba(246,197,68,.45); border-radius: 50%; }
.spiral-ring::after { inset: 48px; border-style: solid; border-color: rgba(63,224,242,.5); }
.spiral-ring2 { position: absolute; inset: -28px; border: 1px dotted rgba(246,197,68,.3); border-radius: 50%; animation: spiralSpin 7s linear infinite reverse; }
@keyframes spiralSpin { to { transform: rotate(360deg); } }
.spin-ball { position: absolute; left: calc(45% + 92px); top: 42%; width: 34px; height: 34px; margin-top: -17px; border-radius: 50%; background: radial-gradient(circle at 32% 30%, #fff6d8, var(--gold) 45%, #9a6a0c); box-shadow: 0 0 26px rgba(246,197,68,.8); animation: ballOrbit 4s linear infinite; }
.spin-ball span { position: absolute; inset: 6px; border: 1px solid rgba(255,255,255,.6); border-radius: 50%; }
.spin-ball2 { animation-duration: 6.5s; animation-direction: reverse; width: 24px; height: 24px; margin-top: -12px; left: calc(45% + 60px); background: radial-gradient(circle at 32% 30%, #fff, var(--teal-glow) 50%, #136f7e); box-shadow: 0 0 20px rgba(63,224,242,.7); }
@keyframes ballOrbit { from { transform: rotate(0deg) translateX(96px) rotate(0deg); } to { transform: rotate(360deg) translateX(96px) rotate(-360deg); } }
.dust { position: absolute; width: 8px; height: 8px; border-radius: 50%; background: rgba(246,197,68,.5); filter: blur(2px); animation: dustFloat 3s ease-in-out infinite; }
.dust-a { left: 20%; top: 60%; }
.dust-b { left: 70%; top: 30%; animation-delay: 1.4s; }
@keyframes dustFloat { 0%,100% { transform: translate(0,0) scale(1); opacity: .2; } 50% { transform: translate(16px,-20px) scale(1.6); opacity: .7; } }

/* 场景 2：策马奔腾 */
.stage-horse { background: linear-gradient(180deg, #3a2418 0%, #7a3f22 45%, #241610 100%); }
.sun { position: absolute; left: 50%; top: 34%; width: 120px; height: 120px; margin: -60px 0 0 -60px; border-radius: 50%; background: radial-gradient(circle, #fff3c4, #f6a830 55%, transparent 72%); filter: blur(2px); animation: sunPulse 4s ease-in-out infinite; }
@keyframes sunPulse { 0%,100% { transform: scale(1); opacity: .9; } 50% { transform: scale(1.1); opacity: 1; } }
.mesa { position: absolute; bottom: 26%; width: 130px; height: 90px; background: #4a2a1c; clip-path: polygon(12% 100%, 22% 30%, 36% 22%, 44% 32%, 62% 18%, 78% 28%, 88% 100%); opacity: .85; }
.mesa-1 { left: 8%; animation: mesaDrift 18s linear infinite; }
.mesa-2 { right: 6%; transform: scale(.8); animation: mesaDrift 24s linear infinite reverse; }
@keyframes mesaDrift { 0%,100% { opacity: .7; } 50% { opacity: .95; } }
.ground-line { position: absolute; left: 0; right: 0; bottom: 16%; height: 2px; background: linear-gradient(90deg, transparent, rgba(246,197,68,.5), transparent); }
.rider { position: absolute; left: -180px; bottom: 16%; width: 150px; height: 90px; animation: rideAcross 7s linear infinite; }
@keyframes rideAcross { 0% { left: -180px; } 100% { left: 105%; } }
.rider-body { position: absolute; left: 52px; top: 2px; width: 20px; height: 42px; background: #100c0a; border-radius: 6px 6px 0 0; transform: rotate(-12deg); }
.rider-body::before { content: ''; position: absolute; left: -3px; top: -12px; width: 20px; height: 20px; border-radius: 50%; background: #100c0a; }
.rider-body::after { content: ''; position: absolute; left: 14px; top: 4px; width: 26px; height: 4px; background: #100c0a; transform: rotate(18deg); }
.horse { position: absolute; left: 0; bottom: 0; width: 110px; height: 46px; background: #100c0a; border-radius: 44% 52% 30% 24%; }
.horse::before { content: ''; position: absolute; right: -22px; top: -30px; width: 34px; height: 40px; background: #100c0a; border-radius: 50% 50% 20% 40%; transform: rotate(24deg); }
.horse .leg { position: absolute; bottom: -22px; width: 6px; height: 30px; background: #100c0a; border-radius: 3px; transform-origin: top; }
.horse .l1 { left: 16px; animation: gallop .42s ease-in-out infinite; }
.horse .l2 { left: 34px; animation: gallop .42s ease-in-out infinite .21s; }
.horse .l3 { left: 72px; animation: gallop .42s ease-in-out infinite .1s; }
.horse .l4 { left: 90px; animation: gallop .42s ease-in-out infinite .31s; }
@keyframes gallop { 0%,100% { transform: rotate(28deg); } 50% { transform: rotate(-26deg); } }
.horse .tail { position: absolute; left: -22px; top: 4px; width: 28px; height: 8px; background: #100c0a; border-radius: 8px; transform: rotate(-18deg); transform-origin: right; animation: tailWave .5s ease-in-out infinite alternate; }
@keyframes tailWave { to { transform: rotate(-34deg); } }
.dust-cloud { position: absolute; right: 0; bottom: 14%; }
.dust-cloud i { position: absolute; width: 26px; height: 16px; border-radius: 50%; background: rgba(214,160,110,.5); filter: blur(3px); animation: dustRoll 1.4s ease-out infinite; }
.dust-cloud i:nth-child(2) { animation-delay: .35s; bottom: 8px; }
.dust-cloud i:nth-child(3) { animation-delay: .7s; bottom: -4px; }
.dust-cloud i:nth-child(4) { animation-delay: 1s; bottom: 14px; }
@keyframes dustRoll { 0% { right: 150px; opacity: .7; transform: scale(.6); } 100% { right: -40px; opacity: 0; transform: scale(1.8); } }

/* 场景 3：时间停止 */
.stage-world { background: radial-gradient(circle at 50% 40%, rgba(226,59,84,.3), transparent 60%), #10060a; }
.time-flash { position: absolute; inset: 0; background: #fff; animation: timeFlash 4s ease-in-out infinite; }
@keyframes timeFlash { 0%, 88%, 100% { opacity: 0; } 90% { opacity: .55; } 92% { opacity: 0; } 94% { opacity: .3; } }
.clock { position: absolute; left: 50%; top: 40%; width: 150px; height: 150px; margin: -75px 0 0 -75px; border: 3px solid rgba(226,59,84,.7); border-radius: 50%; box-shadow: 0 0 40px rgba(226,59,84,.45), inset 0 0 30px rgba(226,59,84,.25); }
.clock::before { content: ''; position: absolute; inset: 12px; border: 1px dotted rgba(226,59,84,.5); border-radius: 50%; animation: spiralSpin 12s linear infinite; }
.hand { position: absolute; left: 50%; top: 50%; background: #ff9fb0; transform-origin: bottom center; border-radius: 2px; }
.hand-h { width: 5px; height: 42px; margin-left: -2.5px; margin-top: -42px; animation: handTick 4s steps(1) infinite; }
.hand-m { width: 3px; height: 58px; margin-left: -1.5px; margin-top: -58px; animation: handTick 4s steps(1) infinite reverse; }
@keyframes handTick { 0%, 100% { transform: rotate(0deg); } 30%, 70% { transform: rotate(72deg); } 40% { transform: rotate(-90deg); } }
.clock-core { position: absolute; left: 50%; top: 50%; width: 12px; height: 12px; margin: -6px; border-radius: 50%; background: #ff9fb0; box-shadow: 0 0 16px #e23b54; }
.time-words { position: absolute; left: 50%; top: 40%; transform: translate(-50%,-50%); display: flex; gap: 18px; pointer-events: none; }
.time-words span { font-size: 26px; font-weight: 900; letter-spacing: .3em; color: transparent; -webkit-text-stroke: 1px rgba(255,159,176,.5); animation: wordStop 4s ease-in-out infinite; }
@keyframes wordStop { 0%, 100% { opacity: 0; transform: scale(1.25); } 20%, 80% { opacity: 1; transform: scale(1); } }
.red-rays { position: absolute; left: 50%; top: 40%; width: 400px; height: 400px; margin: -200px; background: repeating-conic-gradient(from 0deg, rgba(226,59,84,.16) 0deg 6deg, transparent 6deg 22deg); border-radius: 50%; animation: spiralSpin 18s linear infinite; mask-image: radial-gradient(circle, transparent 22%, #000 60%, transparent 75%); }

/* 场景 4：正义的独白 */
.stage-justice { background: radial-gradient(ellipse at 50% 80%, rgba(200,147,32,.35), transparent 65%), #12100a; }
.justice-stars span { position: absolute; color: rgba(246,197,68,.7); font-size: 12px; animation: twinkle 3s ease-in-out infinite; }
.justice-glow { position: absolute; left: 50%; bottom: -80px; width: 500px; height: 250px; margin-left: -250px; background: radial-gradient(ellipse, rgba(246,197,68,.28), transparent 70%); }
.president { position: absolute; left: 50%; top: 34%; transform: translateX(-50%); }
.p-head { width: 40px; height: 44px; margin: 0 auto 6px; border-radius: 45% 45% 40% 40%; background: #0a0a0a; box-shadow: 0 -8px 0 -2px #0a0a0a; }
.p-body { width: 120px; height: 96px; border-radius: 40px 40px 0 0; background: #0a0a0a; clip-path: polygon(18% 0, 82% 0, 100% 100%, 0 100%); animation: presBreathe 4s ease-in-out infinite; }
@keyframes presBreathe { 0%,100% { transform: scaleY(1); } 50% { transform: scaleY(1.03); } }

/* 名台词一览墙 */
.quote-wall { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 14px; }
.quote-card { position: relative; border: 1px solid color-mix(in srgb, var(--card-color) 45%, transparent); border-radius: 10px; padding: 20px 20px 18px 58px; background: linear-gradient(140deg, rgba(255,255,255,.04), rgba(10,10,16,.7)); cursor: pointer; transition: .3s; overflow: hidden; }
.quote-card::before { content: '“'; position: absolute; left: 16px; top: 6px; font-family: Georgia, serif; font-size: 44px; color: var(--card-color); opacity: .45; }
.quote-card:hover { transform: translateY(-4px); border-color: var(--card-color); box-shadow: 0 12px 30px rgba(0,0,0,.4); }
.qc-index { position: absolute; right: 14px; top: 12px; font-family: "Cinzel", Georgia, serif; font-size: 11px; color: var(--card-color); opacity: .7; }
.qc-text { font-size: 14px; line-height: 1.8; color: var(--paper, #f4ecd8); margin-bottom: 12px; }
.qc-foot { display: flex; flex-direction: column; gap: 3px; font-size: 11px; }
.qc-foot b { color: var(--card-color); }
.qc-foot span { color: rgba(244,236,216,.5); }

@media (max-width: 640px) {
  .cast-row { grid-template-columns: 1fr 1fr; font-size: 12px; }
  .cast-head { display: none; }
  .hero-title { font-size: 48px; }
  .sec-head h2 { font-size: 20px; }
  .stage-frame { height: 340px; }
  .quote-layer { padding: 20px 16px 16px; }
}
</style>
