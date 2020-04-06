package tv.hd3g.mvnplugin.setupdb;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListIterator implements Iterator<Node> {

	private final NodeList nodeList;
	private int pos;

	public NodeListIterator(final NodeList nodeList) {
		Objects.requireNonNull(nodeList, "nodeList can't to be null");
		this.nodeList = nodeList;
		pos = 0;
	}

	public NodeListIterator(final Element nodeListFromElement) {
		this(nodeListFromElement.getChildNodes());
	}

	@Override
	public boolean hasNext() {
		return pos < nodeList.getLength();
	}

	@Override
	public Node next() {
		if (hasNext() == false) {
			throw new NoSuchElementException();
		}
		return nodeList.item(pos++);
	}

	public Spliterator<Node> spliterator() {
		return Spliterators.spliterator(this, nodeList.getLength(), IMMUTABLE + ORDERED + SIZED);
	}

	public Stream<Node> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

}
