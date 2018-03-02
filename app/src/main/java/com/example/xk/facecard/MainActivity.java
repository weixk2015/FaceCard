package com.example.xk.facecard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
        final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        Button button1 = (Button) findViewById(R.id.main_ok_button);

        List<String> dataList1, dataList2;
        //为dataList赋值，将下面这些数据添加到数据源中
        dataList1 = new ArrayList<String>();
        dataList1.add("复旦大学");
        dataList1.add("上海交通大学");
        dataList1.add("清华大学");
        dataList1.add("北京大学");
        dataList2 = new ArrayList<String>();
        dataList2.add("计算机科学技术学院");
        dataList2.add("经济学院");
        dataList2.add("管理学院");


        /*为spinner定义适配器，也就是将数据源存入adapter，这里需要三个参数
        1. 第一个是Context（当前上下文），这里就是this
        2. 第二个是spinner的布局样式，这里用android系统提供的一个样式
        3. 第三个就是spinner的数据源，这里就是dataList*/
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,dataList1);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, dataList2);



        //为适配器设置下拉列表下拉时的菜单样式。
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //为spinner绑定我们定义好的数据适配器
        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);
        spinner1.setSelection(0,true);
        spinner2.setSelection(0,true);



        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String schoolName = spinner1.getSelectedItem().toString();
                String departmentName = spinner2.getSelectedItem().toString();
                if(schoolName.equals("复旦大学") && departmentName.equals("计算机科学技术学院")) {
                    Intent intent = new Intent(MainActivity.this, IdentificationActivity.class);
                    //给Activity传值方式一：创建一个Bundle对象封装数据
                    Bundle data = new Bundle();
                    data.putString("person group id", "");
                    intent.putExtra("data", data);
                    startActivity(intent);
                }else {
                    Toast.makeText(MainActivity.this, "当前选项不可用", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}








