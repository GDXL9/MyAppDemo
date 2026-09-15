package com.example.myappdemo

data class WeatherCity(
    val id: String,
    val province: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class WeatherInfo(
    val cityName: String,
    val temperature: Int,
    val weatherCode: Int,
    val description: String,
    val icon: String,
    val updatedAtMillis: Long
) {
    fun displayText(): String = "$icon $temperature°C · $description"
}

data class DailyWeatherForecast(
    val epochDay: Long,
    val weatherCode: Int,
    val minTemperature: Int,
    val maxTemperature: Int
) {
    val icon: String get() = WeatherCodeMapper.icon(weatherCode, true)
    val description: String get() = WeatherCodeMapper.description(weatherCode)
    fun widgetText(): String = "$icon $description\n$maxTemperature°/$minTemperature°"
}

/**
 * 按省级地区组织的天气城市。坐标为城市中心附近坐标，用于 Open-Meteo 天气查询。
 * 城市 id 是持久化键，只能新增，不应随意修改已有值。
 */
object WeatherCities {
    val ALL = listOf(
        // 直辖市
        city("beijing", "北京市", "北京", 39.9042, 116.4074),
        city("shanghai", "上海市", "上海", 31.2304, 121.4737),
        city("tianjin", "天津市", "天津", 39.0842, 117.2009),
        city("chongqing", "重庆市", "重庆", 29.5630, 106.5516),

        // 华北
        city("shijiazhuang", "河北省", "石家庄", 38.0428, 114.5149),
        city("tangshan", "河北省", "唐山", 39.6305, 118.1802),
        city("qinhuangdao", "河北省", "秦皇岛", 39.9354, 119.5996),
        city("handan", "河北省", "邯郸", 36.6256, 114.5391),
        city("baoding", "河北省", "保定", 38.8740, 115.4646),
        city("zhangjiakou", "河北省", "张家口", 40.7675, 114.8863),
        city("taiyuan", "山西省", "太原", 37.8706, 112.5489),
        city("datong", "山西省", "大同", 40.0768, 113.3001),
        city("linfen", "山西省", "临汾", 36.0880, 111.5190),
        city("yuncheng", "山西省", "运城", 35.0264, 111.0075),
        city("changzhi", "山西省", "长治", 36.1954, 113.1163),
        city("hohhot", "内蒙古自治区", "呼和浩特", 40.8426, 111.7492),
        city("baotou", "内蒙古自治区", "包头", 40.6574, 109.8403),
        city("ordos", "内蒙古自治区", "鄂尔多斯", 39.6086, 109.7813),
        city("chifeng", "内蒙古自治区", "赤峰", 42.2578, 118.8869),
        city("hulunbuir", "内蒙古自治区", "呼伦贝尔", 49.2116, 119.7657),

        // 东北
        city("shenyang", "辽宁省", "沈阳", 41.8057, 123.4315),
        city("dalian", "辽宁省", "大连", 38.9140, 121.6147),
        city("anshan", "辽宁省", "鞍山", 41.1086, 122.9956),
        city("jinzhou", "辽宁省", "锦州", 41.0951, 121.1270),
        city("dandong", "辽宁省", "丹东", 40.0008, 124.3547),
        city("changchun", "吉林省", "长春", 43.8171, 125.3235),
        city("jilin_city", "吉林省", "吉林", 43.8378, 126.5494),
        city("yanji", "吉林省", "延吉", 42.8913, 129.5091),
        city("siping", "吉林省", "四平", 43.1664, 124.3504),
        city("harbin", "黑龙江省", "哈尔滨", 45.8038, 126.5349),
        city("qiqihar", "黑龙江省", "齐齐哈尔", 47.3543, 123.9182),
        city("daqing", "黑龙江省", "大庆", 46.5893, 125.1038),
        city("mudanjiang", "黑龙江省", "牡丹江", 44.5527, 129.6324),
        city("jiamusi", "黑龙江省", "佳木斯", 46.7998, 130.3189),

        // 华东
        city("nanjing", "江苏省", "南京", 32.0603, 118.7969),
        city("suzhou_js", "江苏省", "苏州", 31.2989, 120.5853),
        city("wuxi", "江苏省", "无锡", 31.4912, 120.3119),
        city("changzhou", "江苏省", "常州", 31.8107, 119.9741),
        city("nantong", "江苏省", "南通", 31.9802, 120.8943),
        city("xuzhou", "江苏省", "徐州", 34.2044, 117.2858),
        city("yangzhou", "江苏省", "扬州", 32.3942, 119.4129),
        city("hangzhou", "浙江省", "杭州", 30.2741, 120.1551),
        city("ningbo", "浙江省", "宁波", 29.8683, 121.5440),
        city("wenzhou", "浙江省", "温州", 27.9939, 120.6994),
        city("jiaxing", "浙江省", "嘉兴", 30.7461, 120.7555),
        city("jinhua", "浙江省", "金华", 29.0792, 119.6474),
        city("taizhou_zj", "浙江省", "台州", 28.6564, 121.4208),
        city("hefei", "安徽省", "合肥", 31.8206, 117.2272),
        city("wuhu", "安徽省", "芜湖", 31.3525, 118.4331),
        city("bengbu", "安徽省", "蚌埠", 32.9163, 117.3897),
        city("huangshan", "安徽省", "黄山", 29.7147, 118.3376),
        city("fuyang", "安徽省", "阜阳", 32.8901, 115.8142),
        city("fuzhou_fj", "福建省", "福州", 26.0745, 119.2965),
        city("xiamen", "福建省", "厦门", 24.4798, 118.0894),
        city("quanzhou", "福建省", "泉州", 24.8741, 118.6757),
        city("zhangzhou", "福建省", "漳州", 24.5130, 117.6471),
        city("putian", "福建省", "莆田", 25.4541, 119.0077),
        city("nanchang", "江西省", "南昌", 28.6820, 115.8579),
        city("ganzhou", "江西省", "赣州", 25.8311, 114.9350),
        city("jiujiang", "江西省", "九江", 29.7051, 116.0019),
        city("jingdezhen", "江西省", "景德镇", 29.2687, 117.1784),
        city("shangrao", "江西省", "上饶", 28.4549, 117.9434),
        city("jinan", "山东省", "济南", 36.6512, 117.1201),
        city("qingdao", "山东省", "青岛", 36.0671, 120.3826),
        city("yantai", "山东省", "烟台", 37.4638, 121.4479),
        city("weihai", "山东省", "威海", 37.5131, 122.1204),
        city("weifang", "山东省", "潍坊", 36.7069, 119.1619),
        city("linyi", "山东省", "临沂", 35.1047, 118.3564),
        city("jining", "山东省", "济宁", 35.4149, 116.5871),

        // 华中
        city("zhengzhou", "河南省", "郑州", 34.7466, 113.6254),
        city("luoyang", "河南省", "洛阳", 34.6197, 112.4540),
        city("kaifeng", "河南省", "开封", 34.7973, 114.3076),
        city("nanyang", "河南省", "南阳", 32.9907, 112.5283),
        city("xinyang", "河南省", "信阳", 32.1470, 114.0913),
        city("xinxiang", "河南省", "新乡", 35.3030, 113.9268),
        city("wuhan", "湖北省", "武汉", 30.5928, 114.3055),
        city("yichang", "湖北省", "宜昌", 30.6919, 111.2865),
        city("xiangyang", "湖北省", "襄阳", 32.0090, 112.1224),
        city("jingzhou", "湖北省", "荆州", 30.3348, 112.2407),
        city("shiyan", "湖北省", "十堰", 32.6292, 110.7980),
        city("changsha", "湖南省", "长沙", 28.2282, 112.9388),
        city("zhuzhou", "湖南省", "株洲", 27.8274, 113.1339),
        city("xiangtan", "湖南省", "湘潭", 27.8297, 112.9441),
        city("hengyang", "湖南省", "衡阳", 26.8932, 112.5719),
        city("yueyang", "湖南省", "岳阳", 29.3571, 113.1292),
        city("zhangjiajie", "湖南省", "张家界", 29.1171, 110.4792),

        // 华南
        city("guangzhou", "广东省", "广州", 23.1291, 113.2644),
        city("shenzhen", "广东省", "深圳", 22.5431, 114.0579),
        city("foshan", "广东省", "佛山", 23.0218, 113.1219),
        city("dongguan", "广东省", "东莞", 23.0207, 113.7518),
        city("zhuhai", "广东省", "珠海", 22.2707, 113.5767),
        city("huizhou", "广东省", "惠州", 23.1115, 114.4152),
        city("shantou", "广东省", "汕头", 23.3541, 116.6819),
        city("zhanjiang", "广东省", "湛江", 21.2707, 110.3594),
        city("nanning", "广西壮族自治区", "南宁", 22.8170, 108.3669),
        city("guilin", "广西壮族自治区", "桂林", 25.2742, 110.2991),
        city("liuzhou", "广西壮族自治区", "柳州", 24.3264, 109.4281),
        city("beihai", "广西壮族自治区", "北海", 21.4811, 109.1202),
        city("wuzhou", "广西壮族自治区", "梧州", 23.4748, 111.2791),
        city("haikou", "海南省", "海口", 20.0440, 110.1999),
        city("sanya", "海南省", "三亚", 18.2528, 109.5119),
        city("danzhou", "海南省", "儋州", 19.5209, 109.5808),
        city("qionghai", "海南省", "琼海", 19.2584, 110.4746),

        // 西南
        city("chengdu", "四川省", "成都", 30.5728, 104.0668),
        city("mianyang", "四川省", "绵阳", 31.4675, 104.6796),
        city("deyang", "四川省", "德阳", 31.1269, 104.3979),
        city("yibin", "四川省", "宜宾", 28.7518, 104.6432),
        city("luzhou", "四川省", "泸州", 28.8718, 105.4423),
        city("nanchong", "四川省", "南充", 30.8373, 106.1107),
        city("guiyang", "贵州省", "贵阳", 26.6470, 106.6302),
        city("zunyi", "贵州省", "遵义", 27.7257, 106.9272),
        city("anshun", "贵州省", "安顺", 26.2531, 105.9476),
        city("bijie", "贵州省", "毕节", 27.2985, 105.3050),
        city("kaili", "贵州省", "凯里", 26.5676, 107.9804),
        city("kunming", "云南省", "昆明", 25.0389, 102.7183),
        city("dali", "云南省", "大理", 25.6065, 100.2676),
        city("lijiang", "云南省", "丽江", 26.8721, 100.2299),
        city("qujing", "云南省", "曲靖", 25.4900, 103.7962),
        city("yuxi", "云南省", "玉溪", 24.3505, 102.5439),
        city("jinghong", "云南省", "景洪", 22.0001, 100.7717),
        city("lhasa", "西藏自治区", "拉萨", 29.6520, 91.1721),
        city("shigatse", "西藏自治区", "日喀则", 29.2675, 88.8812),
        city("nyingchi", "西藏自治区", "林芝", 29.6489, 94.3615),
        city("qamdo", "西藏自治区", "昌都", 31.1406, 97.1720),

        // 西北
        city("xian", "陕西省", "西安", 34.3416, 108.9398),
        city("baoji", "陕西省", "宝鸡", 34.3619, 107.2373),
        city("xianyang", "陕西省", "咸阳", 34.3296, 108.7093),
        city("yanan", "陕西省", "延安", 36.5853, 109.4898),
        city("hanzhong", "陕西省", "汉中", 33.0676, 107.0238),
        city("lanzhou", "甘肃省", "兰州", 36.0611, 103.8343),
        city("tianshui", "甘肃省", "天水", 34.5809, 105.7249),
        city("jiayuguan", "甘肃省", "嘉峪关", 39.7731, 98.2892),
        city("jiuquan", "甘肃省", "酒泉", 39.7326, 98.4943),
        city("zhangye", "甘肃省", "张掖", 38.9259, 100.4498),
        city("xining", "青海省", "西宁", 36.6171, 101.7782),
        city("delingha", "青海省", "德令哈", 37.3760, 97.3701),
        city("yushu_city", "青海省", "玉树市", 33.0053, 97.0065),
        city("yinchuan", "宁夏回族自治区", "银川", 38.4872, 106.2309),
        city("shizuishan", "宁夏回族自治区", "石嘴山", 38.9846, 106.3842),
        city("wuzhong", "宁夏回族自治区", "吴忠", 37.9976, 106.1988),
        city("guyuan", "宁夏回族自治区", "固原", 36.0158, 106.2426),
        city("urumqi", "新疆维吾尔自治区", "乌鲁木齐", 43.8256, 87.6168),
        city("karamay", "新疆维吾尔自治区", "克拉玛依", 45.5799, 84.8892),
        city("turpan", "新疆维吾尔自治区", "吐鲁番", 42.9513, 89.1895),
        city("korla", "新疆维吾尔自治区", "库尔勒", 41.7259, 86.1746),
        city("aksu", "新疆维吾尔自治区", "阿克苏", 41.1675, 80.2634),
        city("kashgar", "新疆维吾尔自治区", "喀什", 39.4704, 75.9898),
        city("yining", "新疆维吾尔自治区", "伊宁", 43.9095, 81.2779),

        // 港澳台
        city("hong_kong", "香港特别行政区", "香港", 22.3193, 114.1694),
        city("macau", "澳门特别行政区", "澳门", 22.1987, 113.5439),
        city("taipei", "台湾省", "台北", 25.0330, 121.5654),
        city("new_taipei", "台湾省", "新北", 25.0169, 121.4628),
        city("taichung", "台湾省", "台中", 24.1477, 120.6736),
        city("tainan", "台湾省", "台南", 22.9999, 120.2269),
        city("kaohsiung", "台湾省", "高雄", 22.6273, 120.3014),
        city("hualien", "台湾省", "花莲", 23.9911, 121.6112)
    )

    val PROVINCES: List<String> = ALL.map { it.province }.distinct()
    val DEFAULT: WeatherCity = ALL.first()

    fun citiesForProvince(province: String): List<WeatherCity> =
        ALL.filter { it.province == province }

    fun findById(id: String): WeatherCity = ALL.firstOrNull { it.id == id } ?: DEFAULT

    private fun city(id: String, province: String, name: String, latitude: Double, longitude: Double) =
        WeatherCity(id, province, name, latitude, longitude)
}
