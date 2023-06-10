package com.hante.zhongliang.hantepaydemo_android;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //支付宝
    private Button aipayActionBtn;
    //微信
    private Button wechatpayActionBtn;
    //随机订单号
    private Button generateReferenceActionBtn;
    //金额
    private EditText amountTF;
    //订单号
    private EditText referenceTF;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        aipayActionBtn = (Button) findViewById(R.id.alipayActionBtn);
        wechatpayActionBtn = (Button) findViewById(R.id.wechatpayActionBtn);
        generateReferenceActionBtn = (Button) findViewById(R.id.generateReferencBtn);
        aipayActionBtn.setOnClickListener(this);
        wechatpayActionBtn.setOnClickListener(this);
        generateReferenceActionBtn.setOnClickListener(this);

        amountTF = (EditText) findViewById(R.id.amountTF);
        //格式化交易金额
        amountTF.addTextChangedListener(new MoneyTextWatcher(amountTF));

        referenceTF = (EditText) findViewById(R.id.referenceTF);
        referenceTF.setFocusable(false);
        referenceTF.setText(generateReference());

    }

    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
//            @SuppressWarnings("unchecked")
            /**
             * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
             */
            Log.d("支付宝支付结果", msg.obj.toString());
        }

        ;
    };

    @Override
    public void onClick(final View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {
            case R.id.generateReferencBtn: //随机订单号
                referenceTF.setText(generateReference());
            break;
            case R.id.wechatpayActionBtn://微信
            case R.id.alipayActionBtn: //支付宝
                //组装参数
                String amountSting = amountTF.getText().toString();
                Float amount = Float.valueOf(amountSting) * 100;
                if (amount <= 0) {
                    Log.d("错误！", "onClick: 金额必须大于0");
                    return;
                }
                DecimalFormat df = new DecimalFormat("###.00");
                int reultAmount = amount.intValue();
                HantePayOrder order = HantePayOrder.getInstance();
                order.setAmount(String.valueOf(reultAmount));
                order.setReference(referenceTF.getText().toString());
                //币种代码：模式 USD 美元
                order.setCurrency("USD");
                //区分收款方式
                if (v.getId() == R.id.alipayActionBtn) {
                    order.setVendor("alipay");
                } else {
                    order.setVendor("wechatpay");
                }

                order.setDesc("Product Description");
                order.setNote("note for merchant");

                order.setIpnUrl("www.hante.com");

                order.setHanteApiUrl("https://api.hantepay.cn/v1.3/transactions/app/pay");
                order.setHanteApiToken("123456");
                //创建交易记录
                order.doPost(new HantePayOrder.RequestCallBack() {
                    @Override
                    public void successCallBack(final Response response) throws Exception {
                        //创建交易记录成功 启动收款
                        Runnable payRunnable = new Runnable() {
                            
                            String content = response.body().string();

                            @Override
                            public void run() {
                                JSONObject resp = JSONObject.parseObject(content);
                                String orderInfo = resp.get("orderInfo").toString();

                                if (v.getId() == R.id.alipayActionBtn) {//支付宝
                                    //调用支付宝
                                    PayTask alipay = new PayTask(MainActivity.this);
                                    String result = alipay.pay(orderInfo, true);

                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = result;
                                    mHandler.sendMessage(msg);
                                } else if (v.getId() == R.id.wechatpayActionBtn) {//微信
                                    //调用微信
                                    IWXAPI iwxapi = WXAPIFactory.createWXAPI(MainActivity.this, "wx1f8a7ce87240cdfd");
                                    JSONObject orderInfoJson = JSONObject.parseObject(orderInfo);
                                    PayReq req = new PayReq();
                                    req.appId = orderInfoJson.getString("appid");
                                    req.partnerId = orderInfoJson.getString("partnerid");
                                    req.prepayId = orderInfoJson.getString("prepayid");
                                    req.packageValue = orderInfoJson.getString("package");
                                    req.nonceStr = orderInfoJson.getString("noncestr");
                                    req.timeStamp = orderInfoJson.getString("timestamp");
                                    req.sign = orderInfoJson.getString("sign");
                                    req.extData = "app data"; // optional
                                    iwxapi.sendReq(req);
                                }

                            }
                        };
                        // 必须异步调用
                        Thread payThread = new Thread(payRunnable);
                        payThread.start();
                    }

                    @Override
                    public void errorCallBack(String errorString) {
                        Log.d("请求失败", "errorCallBack: " + errorString);

                    }
                });

            break;
        }

    }

    /**
     * 随机订单号
     * 规则：
     *      时间戳+ 随机6位字符
     * @return
     */
    private String generateReference() {
        //时间戳
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String reference = df.format(new Date());

        String chars = "abcdefghijklmnopqrstuvwxyz";
        //随机6位字符
        String signChar = "";
        for (int i = 0; i < 6; i++) {
            int index = (int) (1 + Math.random() * (chars.length() - 1 - 1 + 1));
            signChar = signChar + chars.substring(index, index + 1);
        }
        //时间戳+ 随机6位字符
        return reference + signChar;
    }


    private static void showAlert(Context ctx, String info) {
        showAlert(ctx, info, null);
    }

    private static void showAlert(Context ctx, String info, DialogInterface.OnDismissListener onDismiss) {
        new AlertDialog.Builder(ctx)
                .setMessage(info)
                .setPositiveButton("confirm", null)
                .setOnDismissListener(onDismiss)
                .show();
    }


    /**
     * 格式化交易金额
     */
    public class MoneyTextWatcher implements TextWatcher {
        private EditText editText;
        private int digits = 2;

        public MoneyTextWatcher(EditText et) {
            editText = et;
        }

        public MoneyTextWatcher setDigits(int d) {
            digits = d;
            return this;
        }


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //删除“.”后面超过2位后的数据
            if (s.toString().contains(".")) {
                if (s.length() - 1 - s.toString().indexOf(".") > digits) {
                    s = s.toString().subSequence(0,
                            s.toString().indexOf(".") + digits + 1);
                    editText.setText(s);
                    editText.setSelection(s.length()); //光标移到最后
                }
            }
            //如果"."在起始位置,则起始位置自动补0
            if (s.toString().trim().substring(0).equals(".")) {
                s = "0" + s;
                editText.setText(s);
                editText.setSelection(2);
            }

            //如果起始位置为0,且第二位跟的不是".",则无法后续输入
            if (s.toString().startsWith("0")
                    && s.toString().trim().length() > 1) {
                if (!s.toString().substring(1, 2).equals(".")) {
                    editText.setText(s.subSequence(0, 1));
                    editText.setSelection(1);
                    return;
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


}
