package netease.zh.com.neteasemaven.netease.async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Arguments {
	private final ArrayList<Object> values = new ArrayList<Object>();

	public Arguments(Object... args) {
		add(args);
	}

	public Arguments add(Object... args) {
		values.addAll(Arrays.asList(args));
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(int index) {
		return (T) values.get(index);
	}

	public List<Object> slice(int from, int to) {
		return values.subList(from, to);
	}

	public List<Object> slice(int from) {
		return slice(from, values.size());
	}

	public int length() {
		return values.size();
	}

	@Override
	public String toString() {
		return "Arguments{" +
				"values=" + values +
				'}';
	}

	public static Arguments create(Object... args) {
		return new Arguments(args);
	}
}
