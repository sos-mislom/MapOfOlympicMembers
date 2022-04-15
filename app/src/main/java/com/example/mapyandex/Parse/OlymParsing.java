package com.example.mapyandex.Parse;

import static com.example.mapyandex.MainActivity.checkConnection;

import android.util.Log;

import com.example.mapyandex.Requests.Requests;
import com.example.mapyandex.Requests.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OlymParsing {
    private Requests p;
    private String url;

    public OlymParsing() {
        this.url = "https://olympteka.ru/olymp/";
        this.p = new Requests();
        }

    public Map<String, ArrayList> getAllMedals(String season){
        if (checkConnection()){
            Response html = p.get(this.url + String.format("different/medals/%s.html", season));
            Document document = Jsoup.parseBodyFragment(html.toString());
            Elements table = document.select("table[class=main-tb tb-eo tb-medals]");
            Map<String, ArrayList> dict = new LinkedHashMap<>();
            String country = "";
            Integer counter = 0;

            for (Element layout : table.select("tr")) {
                ArrayList<String> content = new ArrayList<>();
                for (Element rows : layout.select("td")) {
                    String row_less = String.valueOf(rows).replace("<td>", "").replace("</td>", "");
                    if (row_less.contains("sup")){
                        List<String> wordList = Arrays.asList(row_less.split(" "));
                        row_less = wordList.get(0);
                    }
                    if (counter == 1){
                        if (row_less.contains("fl")){
                            List<String> wordList1 = Arrays.asList(row_less.split("fl-"));
                            List<String> wordList2 = Arrays.asList(wordList1.get(1).split("\""));
                            content.add(wordList2.get(0));
                        }
                        country = row_less.replaceAll("[^А-я]", "");
                    } else {
                        content.add(row_less);
                    }
                    counter++;
                }
                if (content != null && content.size() > 0){
                    dict.put(country, content);
                    counter = 0;
                }

            }
            return dict;
            }
        return null;
    }

    public Map<String, ArrayList> getMedalsOfOneCountry(String country){
        if (checkConnection()){
            Response html = p.get(this.url + String.format("country/profile/%s.html", country));
            Document document = Jsoup.parseBodyFragment(html.toString());
            ArrayList<String> urls = new ArrayList<>();
            for (Element row : document.select("div[class=item-border noc-simbols]")){
                String row_str = row.toString();
                List<String> wordList1 = Arrays.asList(row_str.split("c=\""));
                List<String> wordList2 = Arrays.asList(wordList1.get(wordList1.size()-1).split("\""));
                urls.add("https://olympteka.ru/" + wordList2.get(0));
            }
            Map<String, ArrayList> dict = new LinkedHashMap<>();
            country = "";
            Integer counter = 0;
            for (Element layout : document.select("tr[class=medals-places]")) {
                ArrayList<String> content = new ArrayList<>();
                for (Element rows : layout.select("td")) {
                    String row_less;
                    row_less = String.valueOf(rows).replace("<sup>", "").replace("</sup>", "").replace("<td>", "").replace("</td>", "");
                    if (counter == 1){
                        List<String> wordList1 = Arrays.asList(row_less.split(" "));
                        List<String> wordList2 = Arrays.asList(wordList1.get(2).split(">"));
                        content.add(wordList2.get(wordList2.size()-1));
                        country = row_less.replaceAll("[^А-я]", "");
                    } else if (counter ==11) {
                        Log.e("НСПЗ", counter.toString());
                    }else {
                        content.add(row_less);
                    }
                    counter++;
                }
                if (content != null && content.size() > 0){
                    dict.put(country, content);
                    dict.put("src", urls);
                    counter = 0;
                }

            }
            return dict;
        }
        return null;
    }
}
