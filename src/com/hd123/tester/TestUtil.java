package com.hd123.tester;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

public class TestUtil {

  private static Connection connectDerby() throws Exception {
    String home = System.getProperty("JPOS_HOME");
    return DriverManager.getConnection("jdbc:derby:" + home + "\\data\\sample");
  }

  private static String getUserGid4(Connection conn, String userCode) throws Exception {
    PreparedStatement pstm = conn.prepareStatement(String.format("select gid from %s where code=?", "Jpos4Employee"));
    pstm.setString(1, userCode);
    ResultSet rs = pstm.executeQuery();
    String gid = null;
    if (rs.next()) {
      gid = rs.getString(1);
    } else {
      throw new Exception("用户不存在:" + userCode);
    }
    rs.close();
    pstm.close();
    return gid;
  }

  static void addUser4(String userCode, String passwd) throws Exception {
    Connection conn = connectDerby();
    conn.setAutoCommit(false);
    String empGid = null;
    try {
      empGid = getUserGid4(conn, userCode);
    } catch (Exception e) {
      // nothing
    }
    if (empGid == null) {
      PreparedStatement pstm = conn.prepareStatement(String.format(
          "insert into %s(gid,code,name,passwd, lwprc) values(?,?,?,?,?)", "Jpos4Employee"));
      pstm.setString(1, userCode);
      pstm.setString(2, userCode);
      pstm.setString(3, userCode);
      pstm.setString(4, passwd);
      pstm.setString(5, "RTLPRC");
      pstm.executeUpdate();
    } else {
      PreparedStatement pstm = conn.prepareStatement(String.format("update %s set passwd=? where code=?",
          "Jpos4Employee"));
      pstm.setString(1, passwd);
      pstm.setString(2, userCode);
      pstm.executeUpdate();
    }
    conn.commit();
  }

  public static void addRightsAll4(String userCode) throws Exception {
    ArrayList<String> list = new ArrayList<String>();
    Object[][] perms = getPermissions();
    for (Object[] perm : perms) {
      list.add((String) perm[0]);
    }
    addRights4(userCode, list.toArray(new String[] {}), perms);
  }

  private static Object[][] getPermissions() throws Exception {
    File jposJar = new File(System.getProperty("HD_HOME"), "\\program\\jnlp\\lib\\jpos.jar");
    File jposUserJar = new File(System.getProperty("HD_HOME"), "\\program\\jnlp\\lib\\jposuser.jar");
    ClassLoader cl = new URLClassLoader(new URL[] {
        new URL("file:" + jposJar.getAbsolutePath()),
        new URL("file:" + jposUserJar.getAbsolutePath()) }, TestUtil.class.getClassLoader());
    Document doc = ContextXmlHelper.readDocument(cl.getResourceAsStream("jpos-permission.xml"));
    List list = doc.getRootElement().selectNodes("jpos:permission/jpos:hdpos4");
    List<Object[]> array = new ArrayList<Object[]>();
    for (Object item : list) {
      Element elem = (Element) item;
      Object[] perm = new Object[2];
      for (String code : elem.attributeValue("code").split(",")) {
        perm[0] = elem.getParent().attributeValue("id");
        perm[1] = Integer.valueOf(code.trim());
        array.add(perm);
      }
    }

    Document doc4 = ContextXmlHelper.readDocument(cl.getResourceAsStream("jpos-permission4.xml"));
    List list4 = doc4.getRootElement().selectNodes("jpos:permission/jpos:hdpos4");
    for (Object item : list4) {
      Element elem = (Element) item;
      Object[] perm = new Object[2];
      for (String code : elem.attributeValue("code").split(",")) {
        perm[0] = elem.getParent().attributeValue("id");
        perm[1] = Integer.valueOf(code.trim());
        array.add(perm);
      }
    }

    Document docUser = ContextXmlHelper.readDocument(cl.getResourceAsStream("jpos-permission-user.xml"));
    List listUser = docUser.getRootElement().selectNodes("jpos:permission/jpos:hdpos4");
    for (Object item : listUser) {
      Element elem = (Element) item;
      Object[] perm = new Object[2];
      for (String code : elem.attributeValue("code").split(",")) {
        perm[0] = elem.getParent().attributeValue("id");
        perm[1] = Integer.valueOf(code.trim());
        array.add(perm);
      }
    }
    return array.toArray(new Object[][] {});
  }

  private static void addRights4(String userCode, String[] rights, Object[][] perms) throws Exception {
    Connection conn = connectDerby();
    String empGid = getUserGid4(conn, userCode);
    PreparedStatement pstm = conn.prepareStatement(String.format("insert into %s (empgid, rightgid) values (?, ?)",
        "Jpos4EmpRight"));
    conn.setAutoCommit(false);
    for (String right : rights) {
      String rightGid = getRightGid(right, perms);
      pstm.setString(1, empGid);
      pstm.setString(2, rightGid);
      try {
        pstm.executeUpdate();
        System.out.println(String.format("%s(%s) added", right, rightGid));
      } catch (Exception e) {
        // duplicate
      }
    }
    conn.commit();
  }

  private static String getRightGid(String right, Object[][] perms) throws Exception {
    for (Object[] perm : perms) {
      String id = (String) perm[0];
      Integer idx = (Integer) perm[1];
      if (id.equals(right)) {
        return idx.toString();
      }
    }
    throw new Exception("没有定义权限" + right);
  }
}
