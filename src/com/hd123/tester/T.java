package com.hd123.tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author zhangxinkun 2017年04月08日
 */
public class T {
  public static void main(String[] args) {
    File file = new File(System.getProperty("user.home"), "/hd.install");
    Properties pro = new Properties();
    try {
      pro.load(new FileInputStream(file));
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("hd.install文件读取失败");
    }
    String hdHome = pro.getProperty("HD_HOME");
    File jposhome = new File(hdHome, "/jpos");
    System.setProperty("HD_HOME", new File(hdHome).getAbsolutePath());
    System.setProperty("JPOS_HOME", jposhome.getAbsolutePath());
    try {
      TestUtil.addUser4("0", "0");
      System.out.println("添加用户0/0");
      TestUtil.addRightsAll4("0");
      System.out.println("用户0权限添加完毕");
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("添加员工和权限失败");
    }
  }
}
