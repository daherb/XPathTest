/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package de.ids_mannheim.lza.xpathtester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.AttributeFilter;
import org.jdom2.filter.ContentFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathFactory;

/**
 *
 * @author Herbert Lange <lange@ids-mannheim.de>
 */
public class XPathTester {

    public static void main(String[] args) {
        Options options = new Options()
                .addOption(Option.builder()
                        .argName("NAMESPACE")
                        .option("n")
                        .longOpt("namespace")
                        .desc("Additional namespaces of the form PREFIX=URL, can be repeated")
                        .hasArg()
                        .build())
                .addOption(Option.builder()
                        .argName("INFILE")
                        .option("i")
                        .longOpt("input-file")
                        .desc("Input file")
                        .hasArg()
                        .required()
                        .build())
                .addOption(Option.builder()
                        .argName("XPATH_EXPRESSION")
                        .option("x")
                        .longOpt("xpath-expression")
                        .desc("XPath expression to test, can be repeated")
                        .hasArg()
                        .required()
                        .build())
                .addOption(Option.builder()
                        .argName("ALL_TEXT")
                        .option("t")
                        .longOpt("all-text")
                        .desc("Lists all the text nodes of the element and all its children")
                        .required(false)
                        .hasArg(false)
                        .build())
                .addOption(Option.builder()
                        .argName("HELP")
                        .option("h")
                        .longOpt("help")
                        .hasArg(false)
                        .build());
        CommandLineParser clp = new DefaultParser();
        try {
            CommandLine cl = clp.parse(options, args);
            if (cl.hasOption("h"))
                throw new ParseException("");
            List<Namespace> namespaces = new ArrayList<>();
            if (cl.hasOption("n")) {
                for (String ns : cl.getOptionValues("n")) {
                    if (ns.contains("=")) {
                        String[] splits = ns.split("=");
                        namespaces.add(Namespace.getNamespace(splits[0],splits[1]));
                    }
                    else {
                        throw new ParseException("Expected format PREFIX=URI, got "+ ns);
                    }
                }
            }
            Document doc = new SAXBuilder().build(new File(cl.getOptionValue("i")));
            for (String xpath : cl.getOptionValues("x")) {
                LOG.log(Level.INFO, "XPath {0}", xpath); {
                    List<Content> result1 = XPathFactory.instance()
                        .compile(xpath, new ContentFilter(), new HashMap<>(), namespaces)
                            .evaluate(doc);
                    for (Content c : result1) {
                        if (c instanceof Element) {
                            if (cl.hasOption("t")) {
                                System.out.println(getAllText((Element) c));
                            }
                            else {
                                System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString((Element) c));
                            }
                        }
                        else if (c != null) {
                            System.out.println(c.getCType() + ":" + c.getValue());
                        }
                        else {
                            LOG.severe("Content is null");
                        }
                    }
                    List<Attribute> result2 = XPathFactory.instance()
                        .compile(xpath, new AttributeFilter(), new HashMap<>(), namespaces)
                            .evaluate(doc);
                    for (Attribute a : result2) {
                        System.out.println(a.getName()+ "=" + a.getValue());
                    }
                }
            }
        }
        catch (ParseException pe) {
            String header = pe.getLocalizedMessage();
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("XPathTester", header, options, "", true);
        }
        catch (JDOMException | IOException e) {
            LOG.log(Level.SEVERE, "Exception when reading document {0}", e.getLocalizedMessage());
        }
    }
    
    // Gets all text from an element and its children
    public static List<String> getAllText(Element e) {
        List<String> result = new ArrayList<>(List.of(e.getTextNormalize()));
        result.addAll(e.getChildren().stream().flatMap(c -> getAllText(c).stream()).filter(s -> !s.isBlank()).collect(Collectors.toList()));
        return result.stream().filter(Predicate.not(String::isEmpty)).toList();
    }
    private static final Logger LOG = Logger.getLogger(XPathTester.class.getName());
}
