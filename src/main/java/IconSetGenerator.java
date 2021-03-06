import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IconSetGenerator {

  private static final String NAMESPACE = "http://xml.levigo.com/ns/jadice/alf-icons/1.0";

  private static final String SCHEMA_LOCATION = NAMESPACE + "/schema.xsd";

  private static final String PREFIX = "alf";
  
  private static final String[] IMAGE_FORMATS = {"png", "gif" };

  private final File sourceFolder;

  public IconSetGenerator(File sourceFolder) {
    Objects.requireNonNull(sourceFolder, "source folder must not be null");
    if (!sourceFolder.exists() || !sourceFolder.exists()) {
      throw new IllegalArgumentException(sourceFolder + " does not point to a folder");
    }
    this.sourceFolder = sourceFolder;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      printUsage();
      System.exit(-1);
      return;
    }


    File sourceFolder = new File(args[0]);
    File target = new File(args[1]);
    if (target.exists()) {
      System.err.println(target + " already exists. Abort now.");
      System.exit(-2);
    }

    final IconSetGenerator iconGenerator = new IconSetGenerator(sourceFolder);
    iconGenerator.assembleXML(new FileOutputStream(target));
  }

  void assembleXML(OutputStream outputStream) throws Exception {
    List<File> icons = scanSourceFolder();
    List<File> validIcons = validate(icons);
    Document dom = createDOM(validIcons);
    writeDOM(dom, outputStream);
    System.out.println("Finished processing with " + validIcons.size() + " ALF icons");
    
  }

  private List<File> scanSourceFolder() {
    System.out.println("Scanning source folder for icon files");
    final File[] icons = sourceFolder.listFiles((FilenameFilter) new SuffixFileFilter(IMAGE_FORMATS, IOCase.INSENSITIVE));
    return Arrays.asList(icons);
  }

  private List<File> validate(List<File> icons) {
    return icons.stream().filter(IconSetGenerator::isValidIcon).collect(Collectors.toList());
  }


  static boolean isValidIcon(File icon) {
    try {
      final BufferedImage image = ImageIO.read(icon);
      if (image.getWidth() <= 0 || image.getHeight() <= 0) {
        throw new IllegalArgumentException("image has no dimension");
      }
      System.out.println("  " + icon.getName() + " - valid icon");
      return true;
    } catch (Throwable th) {
      System.out.println("  " + icon.getName() + " - no valid icon: " + th.getMessage());
      return false;
    }
  }

  private Document createDOM(List<File> validIcons) throws Exception {
    System.out.println("Create DOM");
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    Document doc = dbf.newDocumentBuilder().newDocument();
    final Element root = doc.createElementNS(NAMESPACE, "icons");
    root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", //
        "xs:schemaLocation", NAMESPACE + " " + SCHEMA_LOCATION);
    root.setPrefix(PREFIX);
    doc.appendChild(root);
    for (File iconFile : validIcons) {
      final Element icon = doc.createElementNS(NAMESPACE, "icon");
      icon.setPrefix(PREFIX);
      icon.setAttribute("id", FilenameUtils.getBaseName(iconFile.getName()));
      icon.setAttribute("format", FilenameUtils.getExtension(iconFile.getName()).toUpperCase());
      final CDATASection cdata = doc.createCDATASection(toBase64(new FileInputStream(iconFile)));
      icon.appendChild(cdata);
      root.appendChild(icon);
    }

    return doc;
  }

  static String toBase64(InputStream is) throws IOException {
    final byte[] buffer = IOUtils.toByteArray(is);
    return new String(Base64.getEncoder().encode(buffer), Charsets.US_ASCII);
  }

  private void writeDOM(Document dom, OutputStream os) throws Exception {
    Writer writer = new OutputStreamWriter(os, "UTF-8");
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(2));
    Result result = new StreamResult(writer);
    Source source = new DOMSource(dom);
    transformer.transform(source, result);
  }

  private static void printUsage() {
    final URL loc = IconSetGenerator.class.getProtectionDomain().getCodeSource().getLocation();
    final String jarFileName;
    final String jarLocation = loc.toString();
    if (jarLocation.endsWith(".jar")) {
      final int idx = jarLocation.lastIndexOf("/");
      jarFileName = jarLocation.substring(idx + 1);
    } else {
      jarFileName = "alf-iconset-generator.jar";
    }
    System.out.println("Usage: java -jar " + jarFileName + " <icon-source-folder> <target-file>");
  }
}
