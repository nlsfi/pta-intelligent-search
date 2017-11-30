package fi.maanmittauslaitos.pta.search.xpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class HashmapNamespaceContextImpl implements NamespaceContext {
	private Map<String, String> prefixToNamespace = new HashMap<>();
	private Map<String, List<String>> namespaceToPrefixes = new HashMap<>();
	
	
	public void registerNamespace(String prefix, String namespace) {
		prefixToNamespace.put(prefix, namespace);
		
		List<String> tmp = namespaceToPrefixes.get(namespace);
		if (tmp == null) {
			tmp = new ArrayList<>();
		}
		
		tmp.add(prefix);
		
		namespaceToPrefixes.put(namespace, tmp);
	}
	
	@Override
	public String getNamespaceURI(String prefix) {
		return prefixToNamespace.get(prefix);
	}

	@Override
	public String getPrefix(String namespaceURI) {
		List<String> tmp = namespaceToPrefixes.get(namespaceURI);
		if (tmp != null) {
			return tmp.get(0);
		}
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getPrefixes(String namespaceURI) {
		List<String> tmp = namespaceToPrefixes.get(namespaceURI);
		if (tmp == null) {
			tmp = new ArrayList<>();
		} else {
			tmp = new ArrayList<>(tmp); // Copy
		}
		
		return tmp.iterator();
	}

}
