package fi.maanmittauslaitos.pta.search.xpath;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class HashmapNamespaceContextTest {
	private HashmapNamespaceContextImpl ctx;
	
	@Before
	public void setUp() throws Exception {
		ctx = new HashmapNamespaceContextImpl();
	}

	@Test
	public void testNoPrefix() {
		String ns = ctx.getNamespaceURI("foo");
		assertNull(ns);
	}

	@Test
	public void testPrefixRegistered() {
		ctx.registerNamespace("foo", "http://bar.com");
		
		String ns = ctx.getNamespaceURI("foo");
		assertEquals("http://bar.com", ns);
	}


	@Test
	public void testPrefixRegisteredGetPrefix() {
		ctx.registerNamespace("foo", "http://bar.com");
		
		String prefix = ctx.getPrefix("http://bar.com");
		assertEquals("foo", prefix);
	}


	@Test
	public void testPrefixRegisteredSingleItemIterator() {
		ctx.registerNamespace("foo", "http://bar.com");
		
		@SuppressWarnings("unchecked")
		Iterator<String> i = ctx.getPrefixes("http://bar.com");
		assertTrue(i.hasNext());
		
		assertEquals("foo", i.next());
		
		assertFalse(i.hasNext());
	}



	@Test
	public void testPrefixRegisteredTwoItemIterator() {
		ctx.registerNamespace("foo", "http://bar.com");
		ctx.registerNamespace("bar", "http://bar.com");
		
		@SuppressWarnings("unchecked")
		Iterator<String> i = ctx.getPrefixes("http://bar.com");
		assertTrue(i.hasNext());
		
		Set<String> prefixes = new HashSet<>();
		prefixes.add(i.next());
		
		assertTrue(i.hasNext());
		prefixes.add(i.next());
		
		assertFalse(i.hasNext());
		
		assertEquals(2, prefixes.size());
		assertTrue(prefixes.contains("foo"));
		assertTrue(prefixes.contains("bar"));
	}


	
}
