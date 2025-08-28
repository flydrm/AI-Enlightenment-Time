package com.enlightenment.offline

import com.enlightenment.domain.model.StoryCategory



/**
 * 离线故事模板
 * 提供预设的故事内容，用于离线模式
 */
object OfflineStoryTemplates {
    
    /**
     * 获取指定类别的故事模板
     */
    fun getTemplates(category: StoryCategory): List<StoryTemplate> {
        return when (category) {
            StoryCategory.ADVENTURE -> adventureTemplates
            StoryCategory.FAIRY_TALE -> fairyTaleTemplates
            StoryCategory.ANIMAL -> animalTemplates
            StoryCategory.SCIENCE -> scienceTemplates
            StoryCategory.FRIENDSHIP -> friendshipTemplates
            StoryCategory.FANTASY -> fantasyTemplates
        }
    }
    
    private val adventureTemplates = listOf(
        StoryTemplate(
            title = "小熊猫的森林探险",
            content = """
                在一个阳光明媚的早晨，小熊猫乐乐决定去森林深处探险。
                
                他背上小背包，装满了竹子零食和水壶，兴高采烈地出发了。
                
                走着走着，乐乐发现了一条从未见过的小路。路边开满了五颜六色的野花，蝴蝶在花丛中翩翩起舞。
                
                "哇，好漂亮！"乐乐忍不住赞叹道。
                
                他小心翼翼地沿着小路前进，突然听到了一阵呼救声。原来是一只小松鼠被困在了树洞里。
                
                乐乐立刻爬上树，用他灵巧的爪子帮助小松鼠脱困。
                
                "谢谢你，小熊猫！"小松鼠感激地说，"作为感谢，我带你去看森林里最美的瀑布吧！"
                
                他们一起来到了瀑布前，彩虹在水雾中闪闪发光，美得让人屏住呼吸。
                
                这真是一次难忘的探险！乐乐不仅看到了美丽的风景，还交到了新朋友。
            """.trimIndent(),
            imageResourceId = "drawable/story_adventure_panda"
        ),
        StoryTemplate(
            title = "寻找神奇的彩虹果",
            content = """
                传说在森林的尽头，有一棵会结出彩虹果的神奇大树。
                
                小熊猫乐乐听说吃了彩虹果就能实现一个愿望，于是决定去寻找它。
                
                路上，他遇到了聪明的猫头鹰爷爷。
                
                "想找到彩虹果，你需要有勇气、智慧和善良的心。"猫头鹰爷爷说。
                
                乐乐点点头，继续前进。他帮助了迷路的小兔子找到回家的路，用智慧解开了古老石门的谜题，还勇敢地走过了摇摇晃晃的独木桥。
                
                终于，在夕阳西下时，乐乐看到了那棵传说中的大树。树上挂着一颗闪闪发光的彩虹果。
                
                但乐乐想了想，他在这次旅程中已经收获了友谊、勇气和智慧，这些比任何愿望都更珍贵。
                
                于是，他把彩虹果留在了树上，让其他需要的小动物也能找到它。
                
                回家的路上，乐乐的心里充满了快乐和满足。
            """.trimIndent(),
            imageResourceId = "drawable/story_rainbow_fruit"
        )
    )
    
    private val fairyTaleTemplates = listOf(
        StoryTemplate(
            title = "会说话的魔法竹子",
            content = """
                很久很久以前，在一片翠绿的竹林里，住着一只可爱的小熊猫。
                
                有一天，小熊猫发现了一根特别的竹子，它闪闪发光，还会说话！
                
                "你好，小熊猫！"竹子温柔地说，"我是一根魔法竹子，我可以实现你三个愿望。"
                
                小熊猫想了想，说："我的第一个愿望是让森林里所有的动物都有足够的食物。"
                
                魔法竹子挥动叶子，森林里立刻长满了各种美味的果实和嫩叶。
                
                "我的第二个愿望是让生病的动物都康复。"
                
                魔法竹子再次施展魔法，所有生病的动物都恢复了健康。
                
                "那你的第三个愿望呢？"魔法竹子问。
                
                小熊猫微笑着说："我希望你能一直留在森林里，成为大家的朋友。"
                
                魔法竹子感动极了，从此它就扎根在森林中心，用魔法守护着这片美丽的家园。
            """.trimIndent(),
            imageResourceId = "drawable/story_magic_bamboo"
        )
    )
    
    private val animalTemplates = listOf(
        StoryTemplate(
            title = "小熊猫学游泳",
            content = """
                夏天到了，森林里的小河变得清凉诱人。
                
                小熊猫乐乐看着朋友们在水里快乐地游泳，心里很羡慕。但是他不会游泳，有点害怕水。
                
                好朋友小鸭子看出了乐乐的心思，游过来说："乐乐，我来教你游泳吧！"
                
                乐乐有点犹豫："可是...可是我怕水。"
                
                "别担心，我们从浅水区开始，一步一步来。"小鸭子鼓励道。
                
                在小鸭子的耐心指导下，乐乐先学会了在水里站立，然后练习漂浮。
                
                "放松身体，相信水会托住你。"小鸭子在旁边守护着。
                
                慢慢地，乐乐克服了恐惧，学会了狗爬式游泳。虽然动作还不太熟练，但他已经能在水里自由移动了。
                
                "我会游泳啦！"乐乐开心地喊道。
                
                从那以后，每个炎热的夏日午后，你都能看到一只快乐的小熊猫在河里游泳的身影。
            """.trimIndent(),
            imageResourceId = "drawable/story_swimming_panda"
        )
    )
    
    private val scienceTemplates = listOf(
        StoryTemplate(
            title = "竹子的秘密",
            content = """
                小熊猫乐乐最喜欢吃竹子了，但他一直很好奇：为什么竹子这么好吃呢？
                
                一天，乐乐遇到了森林学校的袋鼠老师。
                
                "老师，您能告诉我竹子的秘密吗？"乐乐问。
                
                袋鼠老师笑着说："当然可以！竹子可是很神奇的植物呢。"
                
                "首先，竹子生长得特别快，有些竹子一天能长一米多高！"
                
                "哇！"乐乐惊讶地张大了嘴。
                
                "而且，竹子虽然看起来像树，但其实是一种巨大的草。"
                
                "竹子的内部是空心的，这让它既轻巧又坚固，就像天然的建筑材料。"
                
                "最重要的是，"老师眨眨眼，"竹子含有很多营养，特别适合熊猫的消化系统。"
                
                乐乐恍然大悟："原来竹子有这么多秘密！大自然真是太奇妙了。"
                
                从此，乐乐吃竹子时都会想起这些有趣的知识，觉得竹子更加美味了。
            """.trimIndent(),
            imageResourceId = "drawable/story_bamboo_science"
        )
    )
    
    private val friendshipTemplates = listOf(
        StoryTemplate(
            title = "最好的朋友",
            content = """
                小熊猫乐乐有一个最好的朋友——小猴子跳跳。
                
                他们每天一起玩耍，一起探险，形影不离。
                
                有一天，跳跳收到了一个坏消息：他们一家要搬到山的另一边去了。
                
                乐乐听到这个消息，眼泪忍不住掉了下来："那我们就不能一起玩了。"
                
                跳跳也很难过，但他擦擦眼泪说："距离不会改变我们的友谊！"
                
                搬家前的最后一天，两个好朋友一起做了一个友谊盒子，里面放了他们一起收集的松果、漂亮的石头，还有一起画的画。
                
                "无论走到哪里，看到这些东西，我就会想起你。"跳跳说。
                
                搬家后，乐乐和跳跳通过森林邮递员小鸟传递信件，分享各自的冒险故事。
                
                每个月的满月之夜，他们都会爬到最高的山顶，朝着对方的方向大声喊："我们永远是最好的朋友！"
                
                真正的友谊，永远不会因为距离而改变。
            """.trimIndent(),
            imageResourceId = "drawable/story_friendship"
        )
    )
    
    private val fantasyTemplates = listOf(
        StoryTemplate(
            title = "梦境森林",
            content = """
                每当夜晚来临，小熊猫乐乐闭上眼睛后，就会来到一个神奇的地方——梦境森林。
                
                在这里，树木会唱歌，花朵会跳舞，云朵是棉花糖做的。
                
                乐乐最喜欢骑着彩虹滑梯，从天空滑到地面。
                
                "欢迎来到梦境森林！"一只会飞的兔子向他打招呼。
                
                在梦境森林里，乐乐可以飞翔，可以变大变小，还可以和星星聊天。
                
                有一次，月亮姐姐告诉他："梦境森林的魔力来自每个孩子的想象力。"
                
                "只要保持好奇心和想象力，梦境森林就会一直存在。"
                
                乐乐在梦境森林里度过了美妙的一夜，学会了用画笔画出会动的画，用歌声召唤流星雨。
                
                当清晨的阳光照进窗户，乐乐醒来了。虽然离开了梦境森林，但他知道，今晚闭上眼睛，又能回到那个奇妙的世界。
                
                因为想象力，永远不会消失。
            """.trimIndent(),
            imageResourceId = "drawable/story_dream_forest"
        )
    )
}
/**
 * 故事模板
 */
data class StoryTemplate(
    val title: String,
    val content: String,
    val imageResourceId: String
)
