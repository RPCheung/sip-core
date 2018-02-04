package com.rp.sip.utils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by RP on 2017/4/26.
 */
public class XmlUtils {

    private XmlUtils() {
    }

    private static class Instance {
        public static XmlUtils instance = new XmlUtils();
    }

    public static XmlUtils getXmlUtils() {
        return Instance.instance;
    }

    public Map<String, String> readXml(String content) throws DocumentException {

        StringReader reader = new StringReader(content);
        SAXReader saxReader = new SAXReader();
        Document document =  saxReader.read(reader);
        Element element = document.getRootElement();
        return readXml(element);
    }

    public Map<String, String> readXml(Element fElement) {

        Map<String, String> map = new HashMap<String, String>();
        for (Iterator<Element> i = fElement.elementIterator(); i.hasNext(); ) {
            Element element = i.next();

            if (!(element.isTextOnly())) {
                readXml(element);
            } else {
                map.put(element.getName(), element.getTextTrim());
            }
        }
        return map;
    }

    public Document setXml(Document document, String elementName, String elementText) {
        Element rootElement = document.getRootElement();
        setXml(rootElement, elementName, elementText);
        return document;
    }

    private void setXml(Element firstElement, String elementName, String elementText) {

        for (Iterator<Element> i = firstElement.elementIterator(); i.hasNext(); ) {
            Element element = i.next();

            System.out.println("子节点名称：" + element.getName() + ":" + element.getTextTrim());
            if (element.getName().equals(elementName)) {
                element.setText(elementText);
            }

            if (!(element.isTextOnly())) {
                setXml(element, elementName, elementText);
            }

            List<Attribute> attributes = element.attributes();
            for (Attribute a : attributes) {
                System.out.println("子节点属性：" + a.getName() + ":" + a.getStringValue());
            }
        }
    }
}
