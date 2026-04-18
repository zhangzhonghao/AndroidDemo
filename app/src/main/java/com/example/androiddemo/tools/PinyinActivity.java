package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

import java.util.HashMap;
import java.util.Map;

public class PinyinActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvFullPinyin;
    private TextView tvInitials;

    private static final Map<Character, String> PINYIN_MAP = new HashMap<>();

    static {
        String[][] data = {
            {"啊","a"},{"阿","a"},{"爱","ai"},{"安","an"},{"暗","an"},
            {"八","ba"},{"把","ba"},{"爸","ba"},{"吧","ba"},{"白","bai"},
            {"百","bai"},{"班","ban"},{"半","ban"},{"办","ban"},{"帮","bang"},
            {"包","bao"},{"保","bao"},{"报","bao"},{"北","bei"},{"被","bei"},
            {"比","bi"},{"笔","bi"},{"边","bian"},{"变","bian"},{"别","bie"},
            {"病","bing"},{"不","bu"},{"步","bu"},{"才","cai"},{"参","can"},
            {"藏","cang"},{"草","cao"},{"测","ce"},{"层","ceng"},{"查","cha"},
            {"茶","cha"},{"产","chan"},{"常","chang"},{"长","chang"},{"场","chang"},
            {"唱","chang"},{"车","che"},{"城","cheng"},{"成","cheng"},{"吃","chi"},
            {"持","chi"},{"出","chu"},{"初","chu"},{"除","chu"},{"穿","chuan"},
            {"传","chuan"},{"窗","chuang"},{"床","chuang"},{"春","chun"},{"词","ci"},
            {"次","ci"},{"从","cong"},{"村","cun"},{"错","cuo"},{"打","da"},
            {"大","da"},{"带","dai"},{"代","dai"},{"单","dan"},{"但","dan"},
            {"蛋","dan"},{"当","dang"},{"道","dao"},{"到","dao"},{"导","dao"},
            {"得","de"},{"灯","deng"},{"等","deng"},{"低","di"},{"底","di"},
            {"地","di"},{"弟","di"},{"点","dian"},{"电","dian"},{"店","dian"},
            {"掉","diao"},{"调","diao"},{"顶","ding"},{"定","ding"},{"丢","diu"},
            {"东","dong"},{"冬","dong"},{"动","dong"},{"读","du"},{"短","duan"},
            {"段","duan"},{"断","duan"},{"对","dui"},{"队","dui"},{"多","duo"},
            {"夺","duo"},{"饿","e"},{"儿","er"},{"耳","er"},{"二","er"},
            {"发","fa"},{"法","fa"},{"反","fan"},{"饭","fan"},{"范","fan"},
            {"方","fang"},{"房","fang"},{"放","fang"},{"非","fei"},{"飞","fei"},
            {"费","fei"},{"分","fen"},{"纷","fen"},{"粉","fen"},{"风","feng"},
            {"封","feng"},{"服","fu"},{"福","fu"},{"父","fu"},{"附","fu"},
            {"复","fu"},{"该","gai"},{"改","gai"},{"干","gan"},{"感","gan"},
            {"刚","gang"},{"高","gao"},{"告","gao"},{"哥","ge"},{"歌","ge"},
            {"个","ge"},{"给","gei"},{"跟","gen"},{"根","gen"},{"工","gong"},
            {"共","gong"},{"狗","gou"},{"够","gou"},{"古","gu"},{"故","gu"},
            {"瓜","gua"},{"挂","gua"},{"关","guan"},{"管","guan"},{"光","guang"},
            {"广","guang"},{"贵","gui"},{"国","guo"},{"果","guo"},{"过","guo"},
            {"还","hai"},{"孩","hai"},{"海","hai"},{"害","hai"},{"汉","han"},
            {"号","hao"},{"好","hao"},{"喝","he"},{"河","he"},{"黑","hei"},
            {"很","hen"},{"红","hong"},{"后","hou"},{"厚","hou"},{"呼","hu"},
            {"虎","hu"},{"户","hu"},{"花","hua"},{"化","hua"},{"画","hua"},
            {"话","hua"},{"坏","huai"},{"欢","huan"},{"环","huan"},{"换","huan"},
            {"黄","huang"},{"回","hui"},{"汇","hui"},{"会","hui"},{"婚","hun"},
            {"活","huo"},{"火","huo"},{"或","huo"},{"货","huo"},{"机","ji"},
            {"基","ji"},{"鸡","ji"},{"级","ji"},{"极","ji"},{"几","ji"},
            {"己","ji"},{"记","ji"},{"季","ji"},{"继","ji"},{"济","ji"},
            {"技","ji"},{"际","ji"},{"加","jia"},{"家","jia"},{"价","jia"},
            {"架","jia"},{"假","jia"},{"嫁","jia"},{"件","jian"},{"建","jian"},
            {"键","jian"},{"江","jiang"},{"讲","jiang"},{"奖","jiang"},{"交","jiao"},
            {"郊","jiao"},{"教","jiao"},{"接","jie"},{"街","jie"},{"节","jie"},
            {"姐","jie"},{"今","jin"},{"金","jin"},{"仅","jin"},{"尽","jin"},
            {"紧","jin"},{"进","jin"},{"近","jin"},{"京","jing"},{"经","jing"},
            {"精","jing"},{"井","jing"},{"静","jing"},{"九","jiu"},{"久","jiu"},
            {"酒","jiu"},{"旧","jiu"},{"救","jiu"},{"就","jiu"},{"居","ju"},
            {"局","ju"},{"举","ju"},{"句","ju"},{"巨","ju"},{"具","ju"},
            {"据","ju"},{"聚","ju"},{"开","kai"},{"看","kan"},{"抗","kang"},
            {"考","kao"},{"靠","kao"},{"科","ke"},{"可","ke"},{"课","ke"},
            {"刻","ke"},{"客","ke"},{"空","kong"},{"口","kou"},{"哭","ku"},
            {"苦","ku"},{"库","ku"},{"快","kuai"},{"块","kuai"},{"宽","kuan"},
            {"况","kuang"},{"亏","kui"},{"困","kun"},{"扩","kuo"},{"拉","la"},
            {"来","lai"},{"蓝","lan"},{"兰","lan"},{"拦","lan"},{"懒","lan"},
            {"烂","lan"},{"狼","lang"},{"老","lao"},{"乐","le"},{"了","le"},
            {"雷","lei"},{"累","lei"},{"冷","leng"},{"离","li"},{"里","li"},
            {"理","li"},{"礼","li"},{"李","li"},{"力","li"},{"历","li"},
            {"立","li"},{"利","li"},{"连","lian"},{"脸","lian"},{"练","lian"},
            {"凉","liang"},{"两","liang"},{"亮","liang"},{"量","liang"},{"林","lin"},
            {"临","lin"},{"灵","ling"},{"铃","ling"},{"零","ling"},{"领","ling"},
            {"另","ling"},{"留","liu"},{"流","liu"},{"六","liu"},{"龙","long"},
            {"楼","lou"},{"路","lu"},{"旅","lu"},{"律","lv"},{"绿","lv"},
            {"妈","ma"},{"马","ma"},{"吗","ma"},{"买","mai"},{"卖","mai"},
            {"满","man"},{"慢","man"},{"忙","mang"},{"毛","mao"},{"没","mei"},
            {"每","mei"},{"美","mei"},{"妹","mei"},{"门","men"},{"们","men"},
            {"米","mi"},{"面","mian"},{"民","min"},{"明","ming"},{"名","ming"},
            {"命","ming"},{"摸","mo"},{"末","mo"},{"模","mo"},{"母","mu"},
            {"木","mu"},{"目","mu"},{"拿","na"},{"哪","na"},{"那","na"},
            {"奶","nai"},{"男","nan"},{"南","nan"},{"呢","ne"},{"内","nei"},
            {"能","neng"},{"你","ni"},{"年","nian"},{"念","nian"},{"娘","niang"},
            {"鸟","niao"},{"您","nin"},{"牛","niu"},{"农","nong"},{"弄","nong"},
            {"女","nv"},{"暖","nuan"},{"怕","pa"},{"拍","pai"},{"排","pai"},
            {"派","pai"},{"盘","pan"},{"判","pan"},{"跑","pao"},{"配","pei"},
            {"朋","peng"},{"皮","pi"},{"片","pian"},{"偏","pian"},{"骗","pian"},
            {"漂","piao"},{"票","piao"},{"品","pin"},{"平","ping"},{"苹","ping"},
            {"凭","ping"},{"普","pu"},{"七","qi"},{"期","qi"},{"其","qi"},
            {"奇","qi"},{"骑","qi"},{"起","qi"},{"气","qi"},{"汽","qi"},
            {"器","qi"},{"去","qu"},{"区","qu"},{"取","qu"},{"趣","qu"},
            {"全","quan"},{"泉","quan"},{"却","que"},{"群","qun"},{"然","ran"},
            {"让","rang"},{"绕","rao"},{"热","re"},{"人","ren"},{"认","ren"},
            {"日","ri"},{"容","rong"},{"肉","rou"},{"如","ru"},{"入","ru"},
            {"软","ruan"},{"若","ruo"},{"三","san"},{"散","san"},{"色","se"},
            {"杀","sha"},{"沙","sha"},{"山","shan"},{"上","shang"},{"少","shao"},
            {"社","she"},{"身","shen"},{"深","shen"},{"什","shen"},{"生","sheng"},
            {"声","sheng"},{"师","shi"},{"十","shi"},{"时","shi"},{"实","shi"},
            {"食","shi"},{"始","shi"},{"使","shi"},{"世","shi"},{"市","shi"},
            {"事","shi"},{"是","shi"},{"室","shi"},{"试","shi"},{"视","shi"},
            {"收","shou"},{"手","shou"},{"首","shou"},{"受","shou"},{"书","shu"},
            {"树","shu"},{"竖","shu"},{"数","shu"},{"双","shuang"},{"水","shui"},
            {"睡","shui"},{"顺","shun"},{"说","shuo"},{"思","si"},{"死","si"},
            {"四","si"},{"送","song"},{"诉","su"},{"速","su"},{"算","suan"},
            {"虽","sui"},{"岁","sui"},{"所","suo"},{"他","ta"},{"她","ta"},
            {"它","ta"},{"台","tai"},{"太","tai"},{"态","tai"},{"谈","tan"},
            {"汤","tang"},{"糖","tang"},{"特","te"},{"疼","teng"},{"提","ti"},
            {"题","ti"},{"体","ti"},{"天","tian"},{"田","tian"},{"条","tiao"},
            {"铁","tie"},{"听","ting"},{"停","ting"},{"通","tong"},{"同","tong"},
            {"头","tou"},{"图","tu"},{"土","tu"},{"团","tuan"},{"推","tui"},
            {"腿","tui"},{"外","wai"},{"玩","wan"},{"完","wan"},{"晚","wan"},
            {"万","wan"},{"王","wang"},{"往","wang"},{"网","wang"},{"望","wang"},
            {"忘","wang"},{"危","wei"},{"位","wei"},{"文","wen"},{"问","wen"},
            {"我","wo"},{"屋","wu"},{"五","wu"},{"午","wu"},{"物","wu"},
            {"务","wu"},{"西","xi"},{"吸","xi"},{"希","xi"},{"息","xi"},
            {"习","xi"},{"洗","xi"},{"系","xi"},{"戏","xi"},{"细","xi"},
            {"下","xia"},{"夏","xia"},{"先","xian"},{"现","xian"},{"线","xian"},
            {"想","xiang"},{"向","xiang"},{"象","xiang"},{"像","xiang"},{"小","xiao"},
            {"校","xiao"},{"笑","xiao"},{"些","xie"},{"写","xie"},{"谢","xie"},
            {"新","xin"},{"心","xin"},{"信","xin"},{"星","xing"},{"行","xing"},
            {"形","xing"},{"醒","xing"},{"姓","xing"},{"休","xiu"},{"修","xiu"},
            {"需","xu"},{"许","xu"},{"学","xue"},{"雪","xue"},{"血","xue"},
            {"压","ya"},{"牙","ya"},{"言","yan"},{"研","yan"},{"眼","yan"},
            {"演","yan"},{"阳","yang"},{"养","yang"},{"样","yang"},{"要","yao"},
            {"药","yao"},{"爷","ye"},{"也","ye"},{"夜","ye"},{"叶","ye"},
            {"业","ye"},{"一","yi"},{"医","yi"},{"衣","yi"},{"以","yi"},
            {"已","yi"},{"意","yi"},{"易","yi"},{"因","yin"},{"音","yin"},
            {"银","yin"},{"印","yin"},{"英","ying"},{"影","ying"},{"用","yong"},
            {"由","you"},{"油","you"},{"游","you"},{"友","you"},{"有","you"},
            {"又","you"},{"右","you"},{"园","yuan"},{"原","yuan"},{"远","yuan"},
            {"院","yuan"},{"愿","yuan"},{"月","yue"},{"越","yue"},{"云","yun"},
            {"运","yun"},{"在","zai"},{"再","zai"},{"早","zao"},{"怎","zen"},
            {"站","zhan"},{"张","zhang"},{"找","zhao"},{"照","zhao"},{"者","zhe"},
            {"这","zhe"},{"真","zhen"},{"正","zheng"},{"政","zheng"},{"知","zhi"},
            {"之","zhi"},{"只","zhi"},{"纸","zhi"},{"指","zhi"},{"至","zhi"},
            {"治","zhi"},{"中","zhong"},{"钟","zhong"},{"种","zhong"},{"重","zhong"},
            {"周","zhou"},{"州","zhou"},{"主","zhu"},{"住","zhu"},{"注","zhu"},
            {"祝","zhu"},{"准","zhun"},{"桌","zhuo"},{"字","zi"},{"自","zi"},
            {"资","zi"},{"子","zi"},{"走","zou"},{"租","zu"},{"足","zu"},
            {"组","zu"},{"最","zui"},{"昨","zuo"},{"左","zuo"},{"作","zuo"},
            {"做","zuo"},{"坐","zuo"},{"座","zuo"}
        };
        for (String[] pair : data) {
            PINYIN_MAP.put(pair[0].charAt(0), pair[1]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinyin);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("中文拼音转换");
        }

        initViews();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvFullPinyin = findViewById(R.id.tv_full_pinyin);
        tvInitials = findViewById(R.id.tv_initials);
        Button btnConvert = findViewById(R.id.btn_convert);
        Button btnClear = findViewById(R.id.btn_clear);

        btnConvert.setOnClickListener(v -> convertToPinyin());
        btnClear.setOnClickListener(v -> {
            etInput.setText("");
            tvFullPinyin.setText("");
            tvInitials.setText("");
        });
    }

    private void convertToPinyin() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            tvFullPinyin.setText("请输入中文文本");
            tvInitials.setText("");
            return;
        }

        StringBuilder fullPinyin = new StringBuilder();
        StringBuilder initials = new StringBuilder();

        for (char c : input.toCharArray()) {
            String pinyin = PINYIN_MAP.get(c);
            if (pinyin != null) {
                fullPinyin.append(pinyin);
                initials.append(pinyin.charAt(0));
            } else {
                fullPinyin.append(c);
                initials.append(c);
            }
        }

        tvFullPinyin.setText(fullPinyin.toString());
        tvInitials.setText(initials.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
