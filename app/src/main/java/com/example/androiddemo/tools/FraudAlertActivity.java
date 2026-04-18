package com.example.androiddemo.tools;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FraudAlertActivity extends AppCompatActivity implements FraudAppAdapter.OnUninstallClickListener {

    private Button btnDetect;
    private LinearLayout layoutResult;
    private ImageView ivStatus;
    private TextView tvStatusTitle;
    private TextView tvRiskCount;
    private RecyclerView rvRiskApps;
    private ProgressBar progressBar;
    private View layoutSafe;
    private View layoutDanger;

    private FraudAppAdapter adapter;
    private final List<FraudApp> fraudApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fraud_alert);

        initViews();
        initFraudAppDatabase();
    }

    private void initViews() {
        btnDetect = findViewById(R.id.btn_detect);
        layoutResult = findViewById(R.id.layout_result);
        ivStatus = findViewById(R.id.iv_status);
        tvStatusTitle = findViewById(R.id.tv_status_title);
        tvRiskCount = findViewById(R.id.tv_risk_count);
        rvRiskApps = findViewById(R.id.rv_risk_apps);
        progressBar = findViewById(R.id.progress_bar);
        layoutSafe = findViewById(R.id.layout_safe);
        layoutDanger = findViewById(R.id.layout_danger);

        adapter = new FraudAppAdapter();
        adapter.setOnUninstallClickListener(this);
        rvRiskApps.setLayoutManager(new LinearLayoutManager(this));
        rvRiskApps.setAdapter(adapter);

        btnDetect.setOnClickListener(v -> startDetection());
    }

    private void initFraudAppDatabase() {
        // 电诈APP清单
        fraudApps.add(new FraudApp("com.fake.loan.cash", "速贷钱包", "电诈APP", "假冒贷款APP，诱导充值后无法提现"));
        fraudApps.add(new FraudApp("com.fake.loan.quick", "快捷贷款", "电诈APP", "假冒贷款APP，要求先交工本费"));
        fraudApps.add(new FraudApp("com.fake.loan.easy", "易贷宝", "电诈APP", "假冒贷款APP，诱导刷流水"));
        fraudApps.add(new FraudApp("com.fake.loan.master", "贷款大师", "电诈APP", "假冒贷款APP，收取服务费后消失"));
        fraudApps.add(new FraudApp("com.fake.loan.plus", "贷款Plus", "电诈APP", "假冒贷款APP，诱导预存保证金"));
        fraudApps.add(new FraudApp("com.fake.loan.pro", "贷款专家", "电诈APP", "假冒贷款APP，要求提供验证码"));
        fraudApps.add(new FraudApp("com.fake.loan.vip", "VIP贷款", "电诈APP", "假冒贷款APP，会员费诈骗"));
        fraudApps.add(new FraudApp("com.fake.loan.card", "信用卡贷款", "电诈APP", "假冒贷款APP，诱导先付款"));
        fraudApps.add(new FraudApp("com.fake.loan.sec", "安全贷款", "电诈APP", "假冒贷款APP，收取审核费"));
        fraudApps.add(new FraudApp("com.fake.loan.credit", "信用贷款王", "电诈APP", "假冒贷款APP，诱导刷银行流水"));

        // 假冒电商类
        fraudApps.add(new FraudApp("com.fake.shop.taobao", "淘宝优惠券", "电诈APP", "假冒电商APP，诱导刷单"));
        fraudApps.add(new FraudApp("com.fake.shop.jd", "京东特惠", "电诈APP", "假冒电商APP，虚假购物"));
        fraudApps.add(new FraudApp("com.fake.shop.pinduoduo", "拼多多砍价", "电诈APP", "假冒电商APP，诱导分享诈骗"));
        fraudApps.add(new FraudApp("com.fake.shop.tmall", "天猫优惠", "电诈APP", "假冒电商APP，钓鱼链接"));
        fraudApps.add(new FraudApp("com.fake.shop.cheap", "超低价购物", "电诈APP", "假冒电商APP，收款不发货"));
        fraudApps.add(new FraudApp("com.fake.shop.flash", "限时秒杀", "电诈APP", "假冒电商APP，诱导充值"));
        fraudApps.add(new FraudApp("com.fake.shop.free", "免费领取", "电诈APP", "假冒电商APP，骗取个人信息"));
        fraudApps.add(new FraudApp("com.fake.shop.coupon", "优惠券发放", "电诈APP", "假冒电商APP，诱导注册"));
        fraudApps.add(new FraudApp("com.fake.shop.group", "团购助手", "电诈APP", "假冒电商APP，团购诈骗"));
        fraudApps.add(new FraudApp("com.fake.shop.rebate", "返利购物", "电诈APP", "假冒电商APP，诱导充值"));

        // 假冒公安类
        fraudApps.add(new FraudApp("com.gov.fake.police", "假冒公安APP", "假冒公务", "冒充公安机关诈骗"));
        fraudApps.add(new FraudApp("com.gov.fake.court", "法院通知", "假冒公务", "冒充法院诈骗"));
        fraudApps.add(new FraudApp("com.gov.fake.prosecutor", "检察院通缉", "假冒公务", "冒充检察院诈骗"));
        fraudApps.add(new FraudApp("com.gov.fake.ems", "安全账户", "假冒公务", "冒充公安机关诱导转账"));
        fraudApps.add(new FraudApp("com.gov.fake.tax", "税务核查", "假冒公务", "冒充税务机关诈骗"));
        fraudApps.add(new FraudApp("com.gov.fake.customs", "海关查税", "假冒公务", "冒充海关诈骗"));
        fraudApps.add(new FraudApp("com.gov.fake.bank", "银行监管", "假冒公务", "冒充银行监管诈骗"));
        fraudApps.add(new FraudApp("com.gov.fake.safety", "安全账户", "假冒公务", "冒充公检法诈骗"));
        fraudApps.add(new FraudApp("com.gov.fake.legal", "法律文书", "假冒公务", "冒充司法机关诈骗"));
        fraudApps.add(new FraudApp("com.gov.fake.arrest", "通缉令", "假冒公务", "冒充公安机关恐吓诈骗"));

        // 假冒银行类
        fraudApps.add(new FraudApp("com.fakebank.icbc", "工行手机银行", "盗版金融", "假冒银行APP，钓鱼诈骗"));
        fraudApps.add(new FraudApp("com.fakebank.ccb", "建设银行", "盗版金融", "假冒银行APP，盗取账号"));
        fraudApps.add(new FraudApp("com.fakebank.boc", "中国银行", "盗版金融", "假冒银行APP，诈骗转账"));
        fraudApps.add(new FraudApp("com.fakebank.abc", "农业银行", "盗版金融", "假冒银行APP，钓鱼网站"));
        fraudApps.add(new FraudApp("com.fakebank.comm", "交通银行", "盗版金融", "假冒银行APP，盗刷银行卡"));
        fraudApps.add(new FraudApp("com.fakebank.cmb", "招商银行", "盗版金融", "假冒银行APP，诈骗钱财"));
        fraudApps.add(new FraudApp("com.fakebank.psbc", "邮储银行", "盗版金融", "假冒银行APP，钓鱼欺诈"));
        fraudApps.add(new FraudApp("com.fakebank.cib", "兴业银行", "盗版金融", "假冒银行APP，盗取信息"));
        fraudApps.add(new FraudApp("com.fakebank.hxb", "华夏银行", "盗版金融", "假冒银行APP，诈骗转账"));
        fraudApps.add(new FraudApp("com.fakebank.citic", "中信银行", "盗版金融", "假冒银行APP，钓鱼诈骗"));

        // 杀猪盘类
        fraudApps.add(new FraudApp("com.dating.fake.love", "甜蜜约会", "电诈APP", "杀猪盘诈骗APP，诱导投资"));
        fraudApps.add(new FraudApp("com.dating.fake.match", "精准匹配", "电诈APP", "杀猪盘APP，婚恋诈骗"));
        fraudApps.add(new FraudApp("com.dating.fake.heart", "真心交友", "电诈APP", "杀猪盘APP，诱导博彩"));
        fraudApps.add(new FraudApp("com.dating.fake.sweet", "甜心恋爱", "电诈APP", "杀猪盘APP，情感诈骗"));
        fraudApps.add(new FraudApp("com.dating.fake.chat", "附近聊天", "电诈APP", "杀猪盘APP，诱导赌博"));
        fraudApps.add(new FraudApp("com.love.scam.date", "同城约会", "电诈APP", "杀猪盘APP，骗取充值"));
        fraudApps.add(new FraudApp("com.love.scam.chat", "聊天约会", "电诈APP", "杀猪盘APP，诱导转账"));
        fraudApps.add(new FraudApp("com.love.scam.match", "速配交友", "电诈APP", "杀猪盘APP，诈骗钱财"));
        fraudApps.add(new FraudApp("com.love.scam.heart", "心动对象", "电诈APP", "杀猪盘APP，诱导投资"));
        fraudApps.add(new FraudApp("com.love.scam.friend", "附近的人", "电诈APP", "杀猪盘APP，骗取信任"));

        // 刷单诈骗类
        fraudApps.add(new FraudApp("com.task.fake.shopping", "刷单赚钱", "电诈APP", "刷单诈骗APP，高额返利"));
        fraudApps.add(new FraudApp("com.task.fake.amazon", "亚马逊刷单", "电诈APP", "刷单诈骗APP，诱导充值"));
        fraudApps.add(new FraudApp("com.task.fake.taobao", "淘宝刷单", "电诈APP", "刷单诈骗APP，骗取本金"));
        fraudApps.add(new FraudApp("com.task.fake.jd", "京东刷单", "电诈APP", "刷单诈骗APP，高佣金诱惑"));
        fraudApps.add(new FraudApp("com.task.fake.shop", "电商刷单", "电诈APP", "刷单诈骗APP，连续任务"));
        fraudApps.add(new FraudApp("com.reward.scam.cash", "现金奖励", "电诈APP", "刷单诈骗APP，先充值后返"));
        fraudApps.add(new FraudApp("com.reward.scam.red", "红包任务", "电诈APP", "刷单诈骗APP，诱导转账"));
        fraudApps.add(new FraudApp("com.reward.scam.point", "积分任务", "电诈APP", "刷单诈骗APP，骗取保证金"));
        fraudApps.add(new FraudApp("com.reward.scam.gold", "黄金任务", "电诈APP", "刷单诈骗APP，高回报诱惑"));
        fraudApps.add(new FraudApp("com.reward.scam.vip", "VIP任务", "电诈APP", "刷单诈骗APP，会员费诈骗"));

        // 投资诈骗类
        fraudApps.add(new FraudApp("com.invest.fake.stock", "股票推荐", "电诈APP", "投资诈骗APP，诱导充值"));
        fraudApps.add(new FraudApp("com.invest.fake.bitcoin", "比特币投资", "电诈APP", "投资诈骗APP，虚假平台"));
        fraudApps.add(new FraudApp("com.invest.fake.fund", "基金理财", "电诈APP", "投资诈骗APP，高收益诱惑"));
        fraudApps.add(new FraudApp("com.invest.fake.trust", "信托投资", "电诈APP", "投资诈骗APP，庞氏骗局"));
        fraudApps.add(new FraudApp("com.invest.fake.realty", "房产投资", "电诈APP", "投资诈骗APP，虚假项目"));
        fraudApps.add(new FraudApp("com.stock.scam.predict", "股票预测", "电诈APP", "投资诈骗APP，收取服务费"));
        fraudApps.add(new FraudApp("com.stock.scam.insider", "内幕消息", "电诈APP", "投资诈骗APP，诱导跟单"));
        fraudApps.add(new FraudApp("com.stock.scam.guide", "炒股指导", "电诈APP", "投资诈骗APP，骗取学费"));
        fraudApps.add(new FraudApp("com.stock.scam.robot", "智能炒股", "电诈APP", "投资诈骗APP，虚假软件"));
        fraudApps.add(new FraudApp("com.stock.scam.margin", "杠杆配资", "电诈APP", "投资诈骗APP，虚拟盘"));

        // 假冒金融类
        fraudApps.add(new FraudApp("com.fake.finance.wallet", "钱包金融", "盗版金融", "假冒金融APP，非法集资"));
        fraudApps.add(new FraudApp("com.fake.finance.wealth", "财富管理", "盗版金融", "假冒金融APP，高息诱惑"));
        fraudApps.add(new FraudApp("com.fake.finance.trust", "信托理财", "盗版金融", "假冒金融APP，诈骗本金"));
        fraudApps.add(new FraudApp("com.fake.finance.p2p", "P2P理财", "盗版金融", "假冒金融APP，非法融资"));
        fraudApps.add(new FraudApp("com.fake.finance.insure", "保险理财", "盗版金融", "假冒金融APP，误导投保"));
        fraudApps.add(new FraudApp("com.fake.finance.asset", "资产管理", "盗版金融", "假冒金融APP，虚假项目"));
        fraudApps.add(new FraudApp("com.fake.finance.equity", "股权投资", "盗版金融", "假冒金融APP，原始股诈骗"));
        fraudApps.add(new FraudApp("com.fake.finance.crowd", "众筹投资", "盗版金融", "假冒金融APP，非法众筹"));

        // 假冒贷款APP
        fraudApps.add(new FraudApp("com.loan.cash.fast", "秒下款", "电诈APP", "假冒贷款APP，砍头息"));
        fraudApps.add(new FraudApp("com.loan.cash.small", "小额贷款", "电诈APP", "假冒贷款APP，高利贷"));
        fraudApps.add(new FraudApp("com.loan.cash.online", "线上贷款", "电诈APP", "假冒贷款APP，套路贷"));
        fraudApps.add(new FraudApp("com.loan.cash.instant", "即时贷款", "电诈APP", "假冒贷款APP，裸贷诈骗"));
        fraudApps.add(new FraudApp("com.loan.cash.credit", "信用贷款", "电诈APP", "假冒贷款APP，骗取资料"));
        fraudApps.add(new FraudApp("com.loan.cash.cash", "现金贷", "电诈APP", "假冒贷款APP，714高炮"));
        fraudApps.add(new FraudApp("com.loan.cash.micro", "微粒贷", "电诈APP", "假冒贷款APP，诱导充值"));
        fraudApps.add(new FraudApp("com.loan.cash.white", "白条贷款", "电诈APP", "假冒贷款APP，骗取手续费"));
        fraudApps.add(new FraudApp("com.loan.cash.jump", "跳级贷款", "电诈APP", "假冒贷款APP，连环套路"));
        fraudApps.add(new FraudApp("com.loan.cash.super", "超级贷款", "电诈APP", "假冒贷款APP，暴力催收"));

        // 其他电诈类
        fraudApps.add(new FraudApp("com.scam.game.free", "免费游戏", "电诈APP", "游戏诈骗APP，充值返利"));
        fraudApps.add(new FraudApp("com.scam.game.gift", "游戏礼包", "电诈APP", "游戏诈骗APP，盗取账号"));
        fraudApps.add(new FraudApp("com.scam.medication", "特效药", "电诈APP", "医药诈骗APP，假药"));
        fraudApps.add(new FraudApp("com.scam.inherit", "遗产继承", "电诈APP", "继承诈骗APP，中奖骗局"));
        fraudApps.add(new FraudApp("com.scam.urgent", "紧急转账", "电诈APP", "转账诈骗APP，冒充熟人"));
        fraudApps.add(new FraudApp("com.scam.taxrefund", "退税补贴", "电诈APP", "退税诈骗APP，钓鱼链接"));
        fraudApps.add(new FraudApp("com.scam.subsidy", "政府补贴", "电诈APP", "补贴诈骗APP，诱导转账"));
        fraudApps.add(new FraudApp("com.scam.award", "中奖兑奖", "电诈APP", "中奖诈骗APP，保证金"));
        fraudApps.add(new FraudApp("com.scam.refund", "退款理赔", "电诈APP", "退款诈骗APP，验证码"));
        fraudApps.add(new FraudApp("com.scam.help", "爱心捐助", "电诈APP", "捐助诈骗APP，虚假众筹"));

        // 测试应用
        fraudApps.add(new FraudApp("com.adbc.embank", "爱心捐助", "电诈APP", "捐助诈骗APP，虚假众筹"));

    }

    private void startDetection() {
        btnDetect.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);

        new Thread(() -> {
            List<FraudApp> detectedApps = detectFraudApps();

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnDetect.setEnabled(true);
                showResult(detectedApps);
            });
        }).start();
    }

    private List<FraudApp> detectFraudApps() {
        List<FraudApp> detectedApps = new ArrayList<>();
        Set<String> fraudPackageNames = new HashSet<>();
        for (FraudApp app : fraudApps) {
            fraudPackageNames.add(app.getPackageName());
        }

        PackageManager pm = getPackageManager();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {
            if (fraudPackageNames.contains(packageInfo.packageName)) {
                // 找到对应的FraudApp
                for (FraudApp fraudApp : fraudApps) {
                    if (fraudApp.getPackageName().equals(packageInfo.packageName)) {
                        FraudApp detected = new FraudApp(
                                fraudApp.getPackageName(),
                                fraudApp.getAppName(),
                                fraudApp.getRiskType(),
                                fraudApp.getDescription()
                        );
                        try {
                            ApplicationInfo appInfo = packageInfo.applicationInfo;
                            detected.setIcon(appInfo.loadIcon(pm));
                            detected.setAppName(appInfo.loadLabel(pm).toString());
                        } catch (Exception e) {
                            // ignore
                        }
                        detectedApps.add(detected);
                        break;
                    }
                }
            }
        }

        return detectedApps;
    }

    private void showResult(List<FraudApp> detectedApps) {
        layoutResult.setVisibility(View.VISIBLE);

        if (detectedApps.isEmpty()) {
            layoutSafe.setVisibility(View.VISIBLE);
            layoutDanger.setVisibility(View.GONE);
            adapter.setFraudAppList(new ArrayList<>());
        } else {
            layoutSafe.setVisibility(View.GONE);
            layoutDanger.setVisibility(View.VISIBLE);
            ivStatus.setImageResource(R.drawable.ic_warning);
            tvStatusTitle.setText("发现风险");
            tvRiskCount.setText("共发现 " + detectedApps.size() + " 个风险APP");
            adapter.setFraudAppList(detectedApps);
        }
    }

    @Override
    public void onUninstallClick(FraudApp fraudApp) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + fraudApp.getPackageName()));
        startActivity(intent);
    }
}