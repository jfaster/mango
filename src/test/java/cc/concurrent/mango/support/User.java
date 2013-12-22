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
    private Long money;
    private Date updateTime;

    public User() {
    }

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
        Long thisUpdateTime = this.updateTime != null ? this.updateTime.getTime() : null;
        Long otherUpdateTime = other.updateTime != null ? other.updateTime.getTime() : null;
        return Objects.equal(this.id, other.id)
                && Objects.equal(this.name, other.name)
                && Objects.equal(this.age, other.age)
                && Objects.equal(this.gender, other.gender)
                && Objects.equal(this.money, other.money)
                && Objects.equal(thisUpdateTime, otherUpdateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, age, gender, money, updateTime != null ? updateTime.getTime() : null);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("name", name).add("age", age).
                add("gender", gender).add("money", money).add("updateTime", updateTime).toString();
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

    public Long getMoney() {
        return money;
    }

    public void setMoney(Long money) {
        this.money = money;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
