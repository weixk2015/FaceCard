package com.example.xk.facecard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import static com.example.xk.facecard.ImageHelper.getBitmapFromURL;

public class PersonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        Intent intent = getIntent();
        String personId = intent.getStringExtra("id");
        HashMap<String, String> userDataInfo = RemoteHelper.cacheHashMap.get(personId);


        String title = userDataInfo.get("职称");
        if(title == null)
            title = "当前信息无法加载";
        String phone = userDataInfo.get("电话");
        if(phone == null){
            phone = "当前学者无电话";
        }
        if(phone == null)
            phone = "当前信息无法加载";
        String mail = userDataInfo.get("邮件");
        if(mail == null)
            mail = "当前信息无法加载";
        String addr = userDataInfo.get("地址");
        if(addr == null)
            addr = "当前信息无法加载";
        String school = userDataInfo.get("学位");
        if(school == null)
            school = "当前信息无法加载";
        String area = userDataInfo.get("研究领域");
        if(area == null){
            area = userDataInfo.get("工作内容");
        }
        if(area == null)
            area = "当前信息无法加载";
        String name = userDataInfo.get("姓名");
        if(name == null)
            name = "当前信息无法加载";


        TextView textView0 = (TextView) findViewById(R.id.textView0);
        textView0.append(title);
        TextView textView1 = (TextView) findViewById(R.id.textView1);
        textView1.append(phone);
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        textView2.append(mail);
        TextView textView3 = (TextView) findViewById(R.id.textView3);
        textView3.append(addr);
        TextView textView4 = (TextView) findViewById(R.id.textView4);
        textView4.append(school);
        TextView textView5 = (TextView) findViewById(R.id.textView5);
        textView5.append(area);
        TextView textView6 = (TextView) findViewById(R.id.textView6);
        textView6.append(name);


        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(getBitmapFromURL(RemoteHelper.getImage(personId, "1", null)));

//        ImageView imageView = (ImageView) findViewById(R.id.imageView);
//        imageView.setImageBitmap(getBitmapFromURL(RemoteHelper.getImage(personId, mpersongroupId, null)));
    }
}
