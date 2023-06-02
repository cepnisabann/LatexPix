package com.whitejuke.latexpix;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;


public class MathMLConverter {
    public static String fixMathML(String mathML) {
        Document doc = Jsoup.parse(mathML, "", org.jsoup.parser.Parser.xmlParser());
        Element mathElement = doc.select("math").first();
        Element semanticsElement = mathElement.selectFirst("semantics");
        Element annotationElement = semanticsElement.selectFirst("annotation");
        String result = annotationElement.text();

        System.out.println(result);

        return result;
    }
}
