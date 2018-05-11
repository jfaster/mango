/**
 * @author ash
 */

package org.jfaster.mango.util.bean;

import org.jfaster.mango.annotation.ID;

/**
 * @author ash
 */
public class SuperClass {

  @ID
  private String id;

  private int uid;

  private String tree;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public String getTree() {
    return tree;
  }

  public void setTree(String tree) {
    this.tree = tree;
  }

  public String getKey() {
    return "key";
  }

  public void setKey(String key) {
  }
}
