import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Test;

public class TestSetIconGenerator extends XMLTestCase {

  static File MAVEN_TARGET_FOLDER = new File(
      IconSetGenerator.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();

  static File TEST_ICONS_FOLDER = new File(MAVEN_TARGET_FOLDER, "test-classes/example-icons/");

  @Test
  public void test_toBase64() throws Exception {
    final String base64 = IconSetGenerator.toBase64(new ByteArrayInputStream("foobar1234567890".getBytes("UTF-8")));
    assertEquals("Base64 encoding mismatch", "Zm9vYmFyMTIzNDU2Nzg5MA==", base64);
  }

  @Test
  public void test_isValid() throws Exception {
    assumeTrue("Could not locate folder with test icons",
        TEST_ICONS_FOLDER.exists() && TEST_ICONS_FOLDER.isDirectory());
    assertTrue("Demo icon 5C shall be valid", IconSetGenerator.isValidIcon(new File(TEST_ICONS_FOLDER, "5C.png")));
  }

  @Test
  public void test_assembleXML() throws Exception {
    assumeTrue("Could not locate folder with test icons",
        TEST_ICONS_FOLDER.exists() && TEST_ICONS_FOLDER.isDirectory());

    final IconSetGenerator generator = new IconSetGenerator(TEST_ICONS_FOLDER);
    final ByteArrayOutputStream xmlResultStream = new ByteArrayOutputStream();
    generator.assembleXML(xmlResultStream);
    final String xmlResult = new String(xmlResultStream.toByteArray(), StandardCharsets.UTF_8);

    String expectedXml = readFile(new File(TEST_ICONS_FOLDER, "example.xml"), StandardCharsets.UTF_8);
    assertXMLEqual("XML mismatches", expectedXml, xmlResult);
  }

  // from http://stackoverflow.com/a/326440
  private static String readFile(File file, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(file.toURI()));
    return new String(encoded, encoding);
  }
}

