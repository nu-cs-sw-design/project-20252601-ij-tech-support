package example;

public abstract class TestBadClass implements Runnable, Comparable<TestBadClass> {

	public int BadFieldName = 10; // bad: starts uppercase
	public String another_bad_field = "oops"; // bad: underscores in non-constant

	private int goodField = 5; // normal looking field

	public void BadMethod() { // bad: starts uppercase
		System.out.println("Bad method");
	}

	public void do_Weird() { // bad: uses underscore
		System.out.println("Weird method");
	}

	public void normalMethod() {
		System.out.println("Normal method");
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof TestBadClass && ((TestBadClass) o).goodField == this.goodField;
	}
	// intentionally missing hashCode()
}
