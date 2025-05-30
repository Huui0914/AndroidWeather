package com.example.myapplicationweather.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplicationweather.R;
import com.example.myapplicationweather.common.bing.BingImage;
import com.example.myapplicationweather.common.bing.BingResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Hashtable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecommendedFragment extends Fragment {

    private EditText urlEditText;
    private Button generateQRCodeButton;
    private ImageView qrcodeImageView;
    private Button dailyImageButton;
    private ImageView dailyImageView;
    private TextView dailyImageDescription;

    private OkHttpClient client;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommended, container, false);

        urlEditText = view.findViewById(R.id.urlEditText);
        generateQRCodeButton = view.findViewById(R.id.generateQRCodeButton);
        qrcodeImageView = view.findViewById(R.id.qrcodeImageView);
        dailyImageButton = view.findViewById(R.id.dailyImageButton);
        dailyImageView = view.findViewById(R.id.dailyImageView);
        dailyImageDescription = view.findViewById(R.id.dailyImageDescription);

        // 初始化 OkHttpClient
        client = new OkHttpClient();

        // 设置默认网址
        urlEditText.setText("www.hznu.edu.cn");

        // 生成二维码按钮点击事件
        generateQRCodeButton.setOnClickListener(v -> {
            String url = urlEditText.getText().toString();
            Bitmap qrCodeBitmap = generateQRCode(url);
            if (qrCodeBitmap != null) {
                qrcodeImageView.setImageBitmap(qrCodeBitmap);
            }
        });

        // 每日一图按钮点击事件
        dailyImageButton.setOnClickListener(v -> fetchBingImage());

        return view;
    }

    // 生成二维码的方法
    private Bitmap generateQRCode(String url) {
        try {
            int width = 300;
            int height = 300;
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, width, height, hints);

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            //设置像素信息
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取Bing每日一图
    private void fetchBingImage() {
        String url = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=5&mkt=zh-CN";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();

                    // 解析 JSON 数据
                    Gson gson = new Gson();
                    BingResponse bingResponse = gson.fromJson(jsonData, BingResponse.class);

                    if (bingResponse != null && bingResponse.images != null && !bingResponse.images.isEmpty()) {
                        BingImage image = bingResponse.images.get(0);
                        String fullImageUrl = image.getFullUrl();
                        String copyright = image.copyright;

                        // 更新 UI
                        getActivity().runOnUiThread(() -> updateBackground(fullImageUrl, copyright));
                    }
                }
            }
        });
    }

    private void updateBackground(String imageUrl, String copyright) {
        // 使用 Glide 加载图片并设置为背景
        Glide.with(this)
                .load(imageUrl)
                .into(dailyImageView);

        // 设置版权说明
        dailyImageDescription.setText(copyright);
    }
}
