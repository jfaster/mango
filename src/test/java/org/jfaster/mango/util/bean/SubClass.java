/**
 * @author ash
 */

package org.jfaster.mango.util.bean;

import javax.annotation.Resource;

/**
 * @author ash
 */
public class SubClass extends SuperClass {

  private int price;

  @Resource
  private String tree;

  private int age;

  @Override
  public String getTree() {
    return tree;
  }

  @Override
  public void setTree(String tree) {
    this.tree = tree;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }
}
