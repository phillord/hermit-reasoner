package rationals.transformations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import rationals.Automaton;
import rationals.converters.ConverterException;
import rationals.converters.Expression;

public class InverseMorphismTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void test01SimpleMap() throws ConverterException {
		Automaton a = new Expression().fromString("(abc)*");
		Map m = new HashMap();
		m.put("a", "b");
		m.put("b", "c");
		m.put("c", "a");
		InverseMorphism im = new InverseMorphism(m);
		Automaton res = im.transform(a);
		List exp = Arrays.asList(new String[] { "c", "a", "b", "c", "a", "b" });
		assertTrue("Does not accept 'cabcab'", res.accept(exp));
		exp = Arrays.asList(new String[] { "a", "b", "c" });
		assertTrue("Does accept 'abc'", !res.accept(exp));
	}

	public void test02EpsilonMapping() throws ConverterException {
		Automaton a = new Expression().fromString("(abc)*a");
		Map m = new HashMap();
		m.put("a", "b");
		m.put("d", null);
		m.put("b", "c");
		m.put("c", "a");
		InverseMorphism im = new InverseMorphism(m);
		Automaton res = im.transform(a);
		System.err.println(res);
		List exp = Arrays.asList(new String[] { "c", "a", "b", "d", "d", "c",
				"a", "b", "c" });
		assertTrue("Does not accept 'cabddcabc'", res.accept(exp));
		exp = Arrays.asList(new String[] { "d", "d", "c" });
		assertTrue("Does not accept 'ddc'", res.accept(exp));
	}

	public void test03MultipleMap() throws ConverterException {
		Automaton a = new Expression().fromString("(abc)*a");
		Map m = new HashMap();
		m.put("a", "b");
		m.put("b", "c");
		m.put("c", "b");
		InverseMorphism im = new InverseMorphism(m);
		Automaton res = im.transform(a);
		System.err.println(res);
		List exp = Arrays.asList(new String[] { "a", "a", "b", "a", "c", "b","a" });
		assertTrue("Does not accept 'aabacba'", res.accept(exp));
	}

	public void test04AlphMorph() throws ConverterException {
		Automaton a = new Expression().fromString("(abc)*");
		Map m = new HashMap();
		m.put("a", "b");
		m.put("b", "c");
		m.put(null, null );
		m.put("c", "a");
		InverseMorphism im = new InverseMorphism(m);
		Automaton res = im.transform(a);
		List exp = Arrays.asList(new String[] { "c", "a", "b", "c", "a", "b" });
		assertTrue("Does not accept 'cabcab'", res.accept(exp));
		exp = Arrays.asList(new String[] { "a", "b", "c" });
		assertTrue("Does accept 'abc'", !res.accept(exp));
	}
}
