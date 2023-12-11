package org.java2.data_retrieval;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JsonToMysql {
    public static String url = "jdbc:mysql://47.107.113.54:3306/stackoverflow";
    public static String user = "root";
    public static String password = "soawjd47628";
    public static void main(String[] args) {
        int pageCount = 1;
        int maxPageCount = 100;
        for (;pageCount<=maxPageCount;pageCount++) {
            String jsonFilePath = "data_retrival" + File.separator + "Data" + File.separator + "originFile" + pageCount + ".json";
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                JSONArray items = JSON.parseObject(sb.toString()).getJSONArray("items");
                for (int i = 0; i < 100; i++) {
                    insertQuestion(items.getJSONObject(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void insertQuestion(JSONObject jsonObject) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String questionSql = "INSERT INTO question (, ) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(questionSql);

            for (int i = 0; i < jsonObject.size(); i++) {
                JSONObject jsonObject = jsonObject.getJSONObject(i);
                ps.setString(1, jsonObject.getString("key1"));
                ps.setString(2, jsonObject.getString("key2"));
                ps.executeUpdate();
            }
        }
    }

    public static void insertAnswer(JSONObject jsonObject) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String answerSql = "INSERT INTO answer (, ) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(answerSql);

            for (int i = 0; i < jsonObject.size(); i++) {
                JSONObject jsonObject = jsonObject.getJSONObject(i);
                ps.setString(1, jsonObject.getString("key1"));
                ps.setString(2, jsonObject.getString("key2"));
                ps.executeUpdate();
            }
        }
    }

    public static void insertComment(JSONObject jsonObject) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String commentSql = "INSERT INTO comment (, ) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(commentSql);

            for (int i = 0; i < jsonObject.size(); i++) {
                JSONObject jsonObject = jsonObject.getJSONObject(i);
                ps.setString(1, jsonObject.getString("key1"));
                ps.setString(2, jsonObject.getString("key2"));
                ps.executeUpdate();
            }
        }
    }
}