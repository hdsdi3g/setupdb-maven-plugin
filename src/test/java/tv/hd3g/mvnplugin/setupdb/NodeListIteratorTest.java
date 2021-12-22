package tv.hd3g.mvnplugin.setupdb;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class NodeListIteratorTest {

	NodeListIterator nodeListIterator;
	int listLen;

	@Mock
	NodeList nodeList;
	@Mock
	Node node;

	@BeforeEach
	void init() throws Exception {
		MockitoAnnotations.openMocks(this).close();
		nodeListIterator = new NodeListIterator(nodeList);
		listLen = 1 + new Random().nextInt(20);
		Mockito.when(nodeList.getLength()).thenReturn(listLen);
		Mockito.when(nodeList.item(ArgumentMatchers.anyInt())).thenReturn(node);
	}

	@Test
	void testNodeListIteratorElement() {
		final var nodeListFromElement = Mockito.mock(Element.class);
		Mockito.when(nodeListFromElement.getChildNodes()).thenReturn(nodeList);

		final var nodeListIterator = new NodeListIterator(nodeListFromElement);
		assertTrue(nodeListIterator.hasNext());
		assertEquals(node, nodeListIterator.next());
	}

	@Test
	void testHasNext() {
		assertTrue(nodeListIterator.hasNext());
	}

	@Test
	void testNext() {
		assertEquals(node, nodeListIterator.next());
	}

	@Test
	void testSpliterator() {
		final var spliterator = nodeListIterator.spliterator();
		assertNotNull(spliterator);
		assertTrue(spliterator.hasCharacteristics(IMMUTABLE));
		assertTrue(spliterator.hasCharacteristics(ORDERED));
		assertTrue(spliterator.hasCharacteristics(SIZED));
		assertEquals(listLen, spliterator.estimateSize());
		spliterator.forEachRemaining(item -> assertEquals(node, item));
	}

	@Test
	void testStream() {
		final var stream = nodeListIterator.stream();
		assertNotNull(stream);
		assertEquals(listLen, nodeListIterator.stream().count());
		nodeListIterator.stream().forEach(item -> assertEquals(node, item));
	}

}
