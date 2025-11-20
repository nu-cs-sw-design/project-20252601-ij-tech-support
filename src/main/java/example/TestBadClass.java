package example;

public class TestBadClass {

    public int BadFieldName = 10;   // field should start lowercase

    public void DoSomething() {     // method should start lowercase
        System.out.println("Hello");
    }

    @Override
    public boolean equals(Object o) {
        return true;                // equals, but no hashCode()
    }
}
