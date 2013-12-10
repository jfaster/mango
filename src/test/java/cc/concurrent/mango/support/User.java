package cc.concurrent.mango.support;

import com.google.common.base.Objects;

import java.util.Date;

/**
 * @author ash
 */
public class User {

    private int id;
    private String name;
    private int age;
    private boolean gender;
    private long money;
    private Date updateTime;

    public User(String name, int age, boolean gender, long money, Date updateTime) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.money = money;
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final User other = (User) obj;
        return Objects.equal(this.id, other.id)
                && Objects.equal(this.name, other.name)
                && Objects.equal(this.age, other.age)
                && Objects.equal(this.gender, other.gender)
                && Objects.equal(this.money, other.money)
                && Objects.equal(this.updateTime, other.updateTime);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
