package com.example.eaten;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.eaten.DTO.Account;
import com.example.eaten.DTO.Shared;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private TextView txtRegister;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Hide ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        //check đã đăng nhập trước đó chưa
        Shared shared = new Shared(getApplicationContext());
        if(shared.firsttime()){
            Intent intent = new Intent(MainActivity.this, SlideShowActivity.class);
            startActivity(intent);
            finish();
        }

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtRegister = (TextView) findViewById(R.id.txtRegister);

        //Xử lý đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = String.format("{\n" +
                        "  \"accountId\": 0,\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"%s\",\n" +
                        "  \"displayName\": \"string\",\n" +
                        "  \"avatar\": \"\",\n" +
                        "  \"gender\": \"string\",\n" +
                        "  \"yearOfBirth\": 0\n" +
                        "}", edtEmail.getText().toString(), edtPassword.getText().toString());
                Submit(data);
            }
        });

        //Xử lý đăng ký
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentRegister = new Intent(v.getContext(), RegisterActivity.class);
                startActivity(intentRegister);
            }
        });
    }

    private void Submit(String data) {
        final String savedata = data;
        String URL = "https://thym.azurewebsites.net/api/Accounts/get-account-login/";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String emailValue = edtEmail.getText().toString();
                String passwordValue = edtPassword.getText().toString();
                try {
                    JSONObject object = new JSONObject(response);

                    JSONObject objResult = object.getJSONObject("data");
                    Account account = new Account(Integer.parseInt(objResult.getString("accountId")), Integer.parseInt(objResult.getString("yearOfBirth")), objResult.getString("email"), objResult.getString("password").trim(), objResult.getString("displayName"), objResult.getString("avatarURL"), objResult.getString("gender"));
                    if (account.getEmail().equals(emailValue) && account.getPassword().equals(passwordValue)){
                        //Toast.makeText(getApplicationContext(), "Đăng nhập thành công!", Toast.LENGTH_LONG).show();

                        //lưu ID của account
                        SharedPreferences sp = getSharedPreferences("Save_ID_Acc", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        //Lưu dữ liệu
                        editor.putInt("accID", account.getAccountId());
                        //Hoàn thành
                        editor.commit();

                        //Intent intentHome = new Intent(MainActivity.this, HomeActivity.class);
                        Intent intentHome = new Intent(MainActivity.this, SlideShowActivity.class);
                        intentHome.putExtra("accID", account.getAccountId()); //chuyển accountId sang HomeActivity
                        startActivity(intentHome);
                        //kết thúc activity, ko back được về trang này nếu ko logout
                        finish();
                        //lần vào ứng dụng sau ko cần đăng nhập lại
                        Shared shared = new Shared(getApplicationContext());
                        shared.secondtime();
                    }
                    else
                        Toast.makeText(getApplicationContext(), R.string.text_signinfalse, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), R.string.text_signinfalse, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.text_signinfalse, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return savedata == null ? null : savedata.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    //Log.v("Unsupported Encoding while trying to get the bytes", data);
                    return null;
                }
            }
        };
        requestQueue.add(stringRequest);
    }
}


