package example;

import java.util.Objects;

public class GoodClass implements Runnable {

	private int count;
	private static final String DEFAULT_NAME = "Good";

	public GoodClass() {}

	public void doWork() {
		count++;
	}

	public int getCount() {
		return count;
	}

	@Override
	public void run() {
		doWork();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GoodClass)) {
			return false;
		}
		GoodClass other = (GoodClass) o;
		return count == other.count && Objects.equals(DEFAULT_NAME, other.DEFAULT_NAME);
	}

	@Override
	public int hashCode() {
		return Objects.hash(count, DEFAULT_NAME);
	}
}
