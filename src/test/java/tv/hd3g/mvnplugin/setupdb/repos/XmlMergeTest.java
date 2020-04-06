package tv.hd3g.mvnplugin.setupdb.repos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;

import tv.hd3g.mvnplugin.setupdb.TestLog;

class XmlMergeTest {

	@Test
	void testXmlMerge() throws Exception {
		final var xmlMerge = new XmlMerge(new TestLog());

		final var basePath = new File("").getAbsolutePath();

		xmlMerge.addChangelog(Path.of(basePath, "src/test/resources/database-changelog-v1v2.xml").toFile());
		xmlMerge.addChangelog(Path.of(basePath, "src/test/resources/database-changelog-v3.xml").toFile());
		final File resolvedChangelogFile = Path.of(basePath, "target/database-resolved.xml").toFile();
		xmlMerge.save(resolvedChangelogFile);

		/**
		 * Open after save
		 */
		final var df = DocumentBuilderFactory.newInstance();
		df.setFeature("http://xml.org/sax/features/external-general-entities", false);
		df.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		final var xmlDocumentBuilder = df.newDocumentBuilder();
		xmlDocumentBuilder.setErrorHandler(null);
		final var resolvedChangelogDocument = xmlDocumentBuilder.parse(resolvedChangelogFile);

		final var childs = resolvedChangelogDocument.getDocumentElement().getChildNodes();

		var itemCount = 0;
		for (int pos = 0; pos < childs.getLength(); pos++) {
			final var child = childs.item(pos);
			if (child instanceof Element == false) {
				continue;
			}
			itemCount++;

			final var element = (Element) child;
			assertEquals("include", element.getNodeName());

			final var attrFileValue = element.getAttribute("file");
			assertNotNull(attrFileValue);
			final var attrFile = new File(attrFileValue);
			assertTrue(attrFile.exists());

			if (pos == 0) {
				assertEquals("11.xml", attrFile.getName());
			} else if (pos == 1) {
				assertEquals("12.xml", attrFile.getName());
			} else if (pos == 2) {
				assertEquals("21.xml", attrFile.getName());
			} else if (pos == 3) {
				assertEquals("31.xml", attrFile.getName());
			}
		}
		assertEquals(4, itemCount);
	}

}
