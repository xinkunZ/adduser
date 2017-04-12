package com.hd123.tester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultAttribute;

public class ContextXmlHelper {
  public static Document createContextXmlDocument() {
    Document doc = DocumentFactory.getInstance().createDocument();
    Element e = doc.addElement("beans", "http://www.springframework.org/schema/beans");
    e.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    e.addAttribute("xmlns:context", "http://www.springframework.org/schema/context");
    e.addAttribute("xsi:schemaLocation", "http://www.springframework.org/schema/beans "
        + "http://www.springframework.org/schema/beans/spring-beans-2.5.xsd "
        + "http://www.springframework.org/schema/context "
        + "http://www.springframework.org/schema/context/spring-context-2.5.xsd");
    doc.setRootElement(e);
    return doc;
  }

  public static Document readDocument(String xmlString) {
    try {
      return readDocument(new ByteArrayInputStream(xmlString.getBytes("utf-8")));
    } catch (Exception e) {
      return createContextXmlDocument();
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Document readDocument(InputStream is) throws DocumentException {
    SAXReader saxReader = new SAXReader();
    Map namespaceURIs = new HashMap();
    namespaceURIs.put("beans", "http://www.springframework.org/schema/beans");
    namespaceURIs.put("jpos", "http://www.hd123.com/schema/jpos");
    saxReader.getDocumentFactory().setXPathNamespaceURIs(namespaceURIs);
    return saxReader.read(is);
  }

  /**
   * write document to file. overwrite if file exists.
   * 
   * @param doc
   * @param file
   * @throws IOException
   */
  public static void writeDocument(Document doc, File file) throws IOException {
    if (!file.getParentFile().exists())
      file.getParentFile().mkdirs();
    FileOutputStream fos = new FileOutputStream(file);
    Writer writer = new OutputStreamWriter(fos, "utf-8");
    writeDocument(doc, writer);
    writer.flush();
    fos.getFD().sync();
    writer.close();
  }

  public static void writeDocument(Document doc, Writer writer) throws IOException {
    OutputFormat format = OutputFormat.createPrettyPrint();
    format.setEncoding("utf-8");
    XMLWriter xmlwriter = new XMLWriter(writer, format);
    xmlwriter.write(doc);
  }

  public static String toXmlString(Document doc) throws IOException {
    if (doc == null)
      return null;
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Writer writer = new PrintWriter(os);
    OutputFormat format = OutputFormat.createPrettyPrint();
    format.setEncoding("utf-8");
    XMLWriter xmlwriter = new XMLWriter(writer, format);
    xmlwriter.write(doc);
    writer.close();
    return os.toString();
  }

  public static void setPropertyValue(Element bean, String propertyName, long propertyValue) {
    setPropertyValue(bean, propertyName, Long.toString(propertyValue));
  }

  public static void setPropertyValue(Element bean, String propertyName, String propertyValue) {
    Element prop = (Element) bean.selectSingleNode("beans:property[@name='" + propertyName + "']");
    if (prop == null) {
      prop = bean.addElement("property");
      prop.addAttribute("name", propertyName);
    }
    prop.addAttribute("value", propertyValue);
  }

  public static String getPropertyValue(Element bean, String propertyName) {
    if (bean == null)
      return null;
    DefaultAttribute prop = (DefaultAttribute) bean.selectSingleNode("beans:property[@name='" + propertyName
        + "']/@value");
    if (prop == null)
      return null;
    else
      return prop.getText();
  }

  public static Element copyOrCreateBean(Document srcDoc, Document destDoc, String beanId, String[][] properties) {
    Element srcBean = getBean(srcDoc, beanId);
    Element destBean = null;
    if (srcBean == null) {
      destBean = destDoc.getRootElement().addElement("bean");
      destBean.addAttribute("id", beanId);
      for (String[] prop : properties) {
        destBean.addAttribute(prop[0], prop[1]);
      }
    } else {
      destBean = (Element) srcBean.clone();
      destDoc.getRootElement().add(destBean);
    }
    return destBean;
  }

  public static Element getBean(Document doc, String beanId) {
    return (Element) doc.selectSingleNode("/beans/beans:bean[@id='" + beanId + "']");
  }

  @SuppressWarnings("unchecked")
  public static List<Element> getAllBeans(Document doc) {
    return doc.selectNodes("/beans/beans:bean");
  }

  public static void putBean(Document destDoc, Element beanElem) {
    if (beanElem != null) {
      Element e = getBean(destDoc, beanElem.attributeValue("id"));
      if (e != null)
        e.detach();
    }
    destDoc.getRootElement().add(beanElem);
  }

  public static Element getBeanElementOfBeanId(Document doc, String beanId) {
    return (Element) doc.selectSingleNode("/beans/beans:bean[@id='" + beanId + "']");
  }

  @SuppressWarnings("unchecked")
  public static void removeBeanElementOfBeanId(Document doc, String beanId) {
    List<Element> elems = doc.selectNodes("/beans/beans:bean[@id='" + beanId + "']");
    for (Element e : elems) {
      doc.getRootElement().remove(e);
    }
  }
}
