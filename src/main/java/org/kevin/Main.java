package org.kevin;

import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        List<String> symbols = new ArrayList<>();
        symbols.add("USD");
        symbols.add("EUR");
        StringBuilder sb = new StringBuilder("{\"op\":\"subscribe\",\"args\":[");
        for (int i = 0; i< symbols.size(); i++){
            sb.append("{\"channel\":\"tickets\",\"instId\":\"").append(symbols.get(i)).append("\"}");
        }
        String result = sb.append("]}").toString();
        System.out.println(result);
    }
}