package org.example;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import static java.lang.Thread.sleep;

public class DataRetrival {
    public static void main(String[] args) throws Exception {
        int pageCount = 1;
        int maxPageCount = 2000;
        try {
            for (; pageCount <= maxPageCount; pageCount++) {
                String savePath = "data_retrival" + File.separator + "Data" + File.separator + "originFile"+pageCount+".json";
                String url = "https://api.stackexchange.com/2.3/questions?page=" + pageCount + "&key=U4DMV*8nvpm3EOpvf69Rxw((&site=stackoverflow&pagesize=100&order=desc&sort=activity&tagged=java&filter=!1rhU1q-.EI1))GM3Tt4cayLj*lGTq-oL8W97HUNG8Q9TXnhExPD6*-nK)dusdg-rvUL";
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                // optional default is GET
                con.setRequestMethod("GET");
                int responseCode = con.getResponseCode();
                System.out.println("Sending request for page " + pageCount);
                System.out.println("Response Code : " + responseCode);
                if (responseCode == 400) {
                    System.out.println("Bad request");
                    pageCount--;
                    sleep(5000);
                    continue;
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(con.getInputStream())));
                BufferedWriter out = new BufferedWriter(new FileWriter(savePath));
                String inputLine = in.readLine();
                JSONObject dataJson = JSONUtil.parseObj(inputLine);
                System.out.println("quota_max: " + dataJson.get("quota_max"));
                System.out.println("quota_remaining: " + dataJson.get("quota_remaining"));
                System.out.println("page: " + dataJson.get("page"));
                System.out.println("page_size: " + dataJson.get("page_size"));
                out.write(inputLine);
                out.newLine();
                in.close();
                out.close();
                Long backoff = dataJson.get("backoff", Long.class);
                System.out.println("backoff: " + backoff);
                System.out.println();
                sleep(Objects.requireNonNullElse(backoff, 1L) * 1000);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}