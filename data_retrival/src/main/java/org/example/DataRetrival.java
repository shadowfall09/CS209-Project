package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import static java.lang.Thread.sleep;

public class DataRetrival {
    public static void main(String[] args) throws Exception {
        int pageCount = 92;
        int maxPageCount = 100;
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
                    break;
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(con.getInputStream())));
                BufferedWriter out = new BufferedWriter(new FileWriter(savePath));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    out.write(inputLine);
                    out.newLine();
                }
                in.close();
                out.close();
                sleep(1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}