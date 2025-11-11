package com.footer.docx;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.w3c.dom.Document;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.FileInputStream;

public class Main {
    public static void main(String[] args) throws Exception {

        String path = "D:/Projects/Java/docx-footer-demo/src/main/java/com/footer/docx/";

        String inputDocx = path + "invoice.docx";
        String inputData = path + "invoice-data.xml";
        String outputDocx = path + "generated_document.docx";

        // Explicitly configure Xalan as the TransformerFactory implementation
        System.setProperty(
                "javax.xml.transform.TransformerFactory",
                "org.docx4j.org.apache.xalan.processor.TransformerFactoryImpl"
        );

        // Load the DOCX template
        WordprocessingMLPackage pkg = Docx4J.load(new File(inputDocx));

        // debug purpose
        // org.docx4j.wml.Document jaxbEl = pkg.getMainDocumentPart().getJaxbElement();
        // String pretty = XmlUtils.marshaltoString(
        //         jaxbEl,
        //         true  // pretty print
        // );
        // System.out.println(pretty);

        // Load the XML data document
        FileInputStream fis = new FileInputStream(inputData);
        Document xmlDocument = XmlUtils.getNewDocumentBuilder().parse(fis);

        // Configure how hyperlinks inside content controls are generated (optional)
        BindingHandler.getHyperlinkResolver().setHyperlinkStyle("Hyperlink");

        // Perform OpenDoPE XML binding:
        // FLAG_BIND_INSERT_XML = inject the XML
        // FLAG_BIND_BIND_XML   = populate content controls
        System.out.println("Applying XML data binding...");
        Docx4J.bind(
                pkg,
                xmlDocument,
                Docx4J.FLAG_BIND_INSERT_XML | Docx4J.FLAG_BIND_BIND_XML
        );

        // Extract the main document part (document.xml)
        MainDocumentPart mdp = pkg.getMainDocumentPart();

        // Convert it into a W3C DOM Document so XSLT can be applied
        org.w3c.dom.Document wordXmlAsDom =
                XmlUtils.marshaltoW3CDomDocument(mdp.getJaxbElement());

        // Load and compile the XSLT transformation
        Source xsltSource = new StreamSource(new File(path + "XsltFinisherInvoice.xslt"));
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer(xsltSource);

        // Transform the document XML
        DOMResult transformedResult = new DOMResult();
        transformer.transform(new DOMSource(wordXmlAsDom), transformedResult);

        // Replace the document contents with the transformed version
        org.w3c.dom.Document transformedDom =
                (org.w3c.dom.Document) transformedResult.getNode();
        mdp.setContents(
                mdp.unmarshal(transformedDom.getDocumentElement())
        );

        // Save to a new DOCX file
        Docx4J.save(pkg, new File(outputDocx), Docx4J.FLAG_NONE);

        System.out.println("Document successfully transformed using XSLT and saved to: " + outputDocx);
    }
}
