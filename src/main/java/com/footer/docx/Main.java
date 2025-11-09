package com.footer.docx;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;

import java.io.File;
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;

public class Main {
    public static void main(String[] args) throws Exception {
        WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        ObjectFactory factory = Context.getWmlObjectFactory();

        FooterPart footerPart = new FooterPart();
        footerPart.setPackage(pkg);

        Ftr ftr = createFooter(factory);
        footerPart.setJaxbElement(ftr);

        Relationship rel = pkg.getMainDocumentPart().addTargetPart(footerPart);

        SectPr sectPr = factory.createSectPr();
        FooterReference footerRef = factory.createFooterReference();
        footerRef.setType(HdrFtrRef.DEFAULT);
        footerRef.setId(rel.getId());
        sectPr.getEGHdrFtrReferences().add(footerRef);

        pkg.getMainDocumentPart().addObject(sectPr);

        // generating 3 pages document
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j < 15 + 1; j++) {
                pkg.getMainDocumentPart().addParagraphOfText(
                      "Line " + j + " of page " + i + " - test content for page break."
                );
            }

            if (i < 3) {
                P p = factory.createP();
                Br breakPage = factory.createBr();
                breakPage.setType(STBrType.PAGE);
                p.getContent().add(breakPage);
                pkg.getMainDocumentPart().addObject(p);
            }
        }

        File out = new File("document.docx");
        pkg.save(out);
        System.out.println("Document saved: " + out.getAbsolutePath());
    }

    private static Ftr createFooter(ObjectFactory factory) {
        Ftr ftr = factory.createFtr();
        Tbl tbl = factory.createTbl();

        TblPr tblPr = factory.createTblPr();
        TblWidth tblW = factory.createTblWidth();
        tblW.setType("pct");
        tblW.setW(BigInteger.valueOf(5000));
        tblPr.setTblW(tblW);
        tbl.setTblPr(tblPr);

        Tr tr = factory.createTr();

        Tc tcLeft = factory.createTc();
        tcLeft.setTcPr(makeCellWidth(factory, 1650));
        tcLeft.getContent().add(factory.createP());
        tr.getContent().add(tcLeft);

        Tc tcCenter = factory.createTc();
        tcCenter.setTcPr(makeCellWidth(factory, 1700));

        P pCenter = factory.createP();
        PPr pPrCenter = factory.createPPr();
        Jc jcCenter = factory.createJc();
        jcCenter.setVal(JcEnumeration.CENTER);
        pPrCenter.setJc(jcCenter);
        pCenter.setPPr(pPrCenter);

        pCenter.getContent().add(runText(factory, "My Custom Footer"));
        tcCenter.getContent().add(pCenter);
        tr.getContent().add(tcCenter);

        Tc tcRight = factory.createTc();
        tcRight.setTcPr(makeCellWidth(factory, 1650));

        P pRight = factory.createP();
        PPr pprRight = factory.createPPr();
        Jc jcRight = factory.createJc();
        jcRight.setVal(JcEnumeration.RIGHT);
        pprRight.setJc(jcRight);
        pRight.setPPr(pprRight);

        pRight.getContent().add(runText(factory, "Page\u00A0"));
        addPageField(factory, pRight);

        tcRight.getContent().add(pRight);
        tr.getContent().add(tcRight);

        tbl.getContent().add(tr);
        ftr.getContent().add(tbl);

        return ftr;
    }

    private static TcPr makeCellWidth(ObjectFactory factory, int pct50ths) {
        TcPr tcPr = factory.createTcPr();
        TblWidth w = factory.createTblWidth();
        w.setType("pct");
        w.setW(BigInteger.valueOf(pct50ths));
        tcPr.setTcW(w);
        return tcPr;
    }

    private static R runText(ObjectFactory factory, String value) {
        R r = factory.createR();
        Text t = factory.createText();
        t.setSpace("preserve");
        t.setValue(value);
        r.getContent().add(t);
        return r;
    }

    private static void addPageField(ObjectFactory factory, P p) {
        R rBegin = factory.createR();
        FldChar begin = factory.createFldChar();
        begin.setFldCharType(STFldCharType.BEGIN);
        rBegin.getContent().add(begin);

        R rInstr = factory.createR();
        Text instrText = factory.createText();
        instrText.setSpace("preserve");
        instrText.setValue(" PAGE ");
        JAXBElement<Text> wrapped = factory.createRInstrText(instrText);
        rInstr.getContent().add(wrapped);

        R rSep = factory.createR();
        FldChar sep = factory.createFldChar();
        sep.setFldCharType(STFldCharType.SEPARATE);
        rSep.getContent().add(sep);

        R rVal = factory.createR();
        Text t = factory.createText();
        t.setValue("1");
        rVal.getContent().add(t);

        R rEnd = factory.createR();
        FldChar end = factory.createFldChar();
        end.setFldCharType(STFldCharType.END);
        rEnd.getContent().add(end);

        p.getContent().add(rBegin);
        p.getContent().add(rInstr);
        p.getContent().add(rSep);
        p.getContent().add(rVal);
        p.getContent().add(rEnd);
    }
}
