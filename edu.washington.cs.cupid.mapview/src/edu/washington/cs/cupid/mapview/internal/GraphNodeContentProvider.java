package edu.washington.cs.cupid.mapview.internal;

import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Graph content provider for maps. If all values are {@link Set}s, the elements of the sets are
 * displayed as individual elements.
 * @author Todd Schiller
 */
public class GraphNodeContentProvider extends ArrayContentProvider implements IGraphEntityContentProvider {

	private final Map<?, ?> map;
	private final boolean mappedToSets;
	
	/**
	 * Construct a graph content provider for <code>map</code>.
	 * @param map the underlying map
	 */
	public GraphNodeContentProvider(final Map<?, ?> map) {
		this.map = map;
		
		this.mappedToSets = Iterables.all(map.values(), new Predicate<Object>() {
			@Override
			public boolean apply(final Object value) {
				return value instanceof Set;
			}
		});
	}

	@Override
	public final Object[] getConnectedTo(final Object entity) {
		if (map.get(entity) != null) {
			if (mappedToSets) {
				return ((Set<?>) map.get(entity)).toArray();
			} else {
				return new Object[]{map.get(entity)};
			}
		} else {
			return new Object[]{};
		}
	}
	
	/**
	 * Returns union of map keys and values; if all values are {@link Set}s, the individual set elements are included.
	 * @return Union of map keys and values
	 */
	public final Set<Object> getNodes() {
		if (mappedToSets) {
			Set<Object> result = Sets.newHashSet(map.keySet());
			for (Object value : map.values()) {
				result = Sets.union(result, (Set<?>) value);
			}
			return result;
		} else {
			return Sets.union(map.keySet(), Sets.newHashSet(map.values()));			
		}
	}
}
