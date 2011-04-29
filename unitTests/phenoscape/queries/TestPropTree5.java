package phenoscape.queries;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import phenoscape.queries.lib.CountTable;
import phenoscape.queries.lib.DistinctGeneAnnotationRecord;
import phenoscape.queries.lib.PhenotypeExpression;
import phenoscape.queries.lib.PhenotypeScoreTable;
import phenoscape.queries.lib.Profile;
import phenoscape.queries.lib.TaxonPhenotypeLink;
import phenoscape.queries.lib.Utils;
import phenoscape.queries.lib.VariationTable;

public class TestPropTree5 {
	private static final String UNITTESTROOT = "TTO:0000015";	
	private static final String UNITTESTKB = "unitTestconnection.properties"; 
	private static final String TAXON1STR = "TTO:0000001";
	private static final String TAXON2STR = "TTO:0000002";
	private static final String TAXON3STR = "TTO:0000003";
	private static final String TAXON4STR = "TTO:0000004";
	private static final String TAXON5STR = "TTO:0000005";
	private static final String TAXON6STR = "TTO:0000006";
	private static final String TAXON7STR = "TTO:0000007";
	private static final String TAXON8STR = "TTO:0000008";
	private static final String TAXON9STR = "TTO:0000009";
	private static final String TAXON10STR = "TTO:0000010";
	private static final String TAXON11STR = "TTO:0000011";
	private static final String TAXON12STR = "TTO:0000012";
	private static final String TAXON13STR = "TTO:0000013";
	private static final String TAXON14STR = "TTO:0000014";

	private final int genePhenotypeAnnotationCount = 23;   // True independent of the taxon data loaded
	private final double IC3 = -1*(Math.log(3.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	private final double IC4 = -1*(Math.log(4.0/(double)genePhenotypeAnnotationCount)/Math.log(2));

	
	PhenotypeProfileAnalysis testAnalysis;
	Utils u = new Utils();
	StringWriter testWriter1;
	StringWriter testWriter2;
	StringWriter testWriter3;
	StringWriter testWriter4;
	Map<Integer,Integer> attMap;
	TaxonomyTree t1;
	int nodeIDofQuality;
	Map<Integer,Integer> badQualities;
	Map<String,Integer>countTableCheck;

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();   //prevent complaints by log4j
		u.openKBFromConnections(UNITTESTKB);
		testAnalysis = new PhenotypeProfileAnalysis(u);
		attMap = u.setupAttributes();
		nodeIDofQuality = u.getQualityNodeID();
		testAnalysis.attributeMap = u.setupAttributes();   // this is icky
		
		PhenotypeExpression.getEQTop(u);   //just to initialize early.

		testAnalysis.attributeSet.addAll(testAnalysis.attributeMap.values());		
		testAnalysis.attributeSet.add(nodeIDofQuality);

		badQualities = new HashMap<Integer,Integer>();
		String taxonomyRoot = UNITTESTROOT; 
		t1 = new TaxonomyTree(taxonomyRoot,u);
		t1.traverseOntologyTree(u);
	}
	
	
	private void setupCountTableCheck(){
		countTableCheck = new HashMap<String,Integer>();
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(anatomical cluster)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(anatomical group)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(anatomical structure)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(anatomical system)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(body)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(bone)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(cranium)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(dermal bone)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(dermatocranium)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(dorsal hyoid arch)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(head)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(hyoid arch)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(intramembranous bone)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(material anatomical entity)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(multi-tissue structure)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(opercle)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(opercular flap)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(opercular series)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(organism subdivision)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(pharyngeal arch 2)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(pharyngeal arch)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(portion of connective tissue)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(portion of tissue)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(skeletal system)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(splanchnocranium)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(surface structure)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(suspensorium)", 2);
		countTableCheck.put("count^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 2);
		countTableCheck.put("count", 2);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(anatomical cluster)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(anatomical group)", 8);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(anatomical region)", 3);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(anatomical structure)", 20);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(anatomical system)", 8);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(body)", 20);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(bone)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(brain)", 3);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(cavitated compound organ)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(central nervous system)", 3);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(cerebellum)", 3);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(compound organ)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(cranium)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(dermal bone)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(dermatocranium)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(dorsal hyoid arch)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(dorsal region of cerebellum)", 2);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(dorsal region)", 2);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(eye)", 1);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(fin)", 12);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(head)", 5);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(hindbrain)", 3);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(hyoid arch)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(intramembranous bone)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(material anatomical entity)", 20);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(multi-tissue structure)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(nervous system)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(opercle)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(opercular flap)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(opercular series)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(organism subdivision)", 17);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(paired fin)", 12);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(pectoral fin)", 12);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(pharyngeal arch 2)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(pharyngeal arch)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(portion of connective tissue)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(portion of tissue)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(sensory system)", 1);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(skeletal system)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(splanchnocranium)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(surface structure)", 17);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(suspensorium)", 4);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 20);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(ventral region of cerebellum)", 1);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(ventral region)", 1);
		countTableCheck.put("morphology^OBO_REL:inheres_in_part_of(visual system)", 1);
		countTableCheck.put("morphology", 20);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(anatomical cluster)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(anatomical group)", 9);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(anatomical region)", 3);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(anatomical structure)", 21);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(anatomical system)", 9);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(body)", 21);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(bone)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(brain)", 3);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(cavitated compound organ)", 4);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(central nervous system)", 3);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(cerebellum)", 3);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(compound organ)", 4);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(cranium)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(dermal bone)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(dermatocranium)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(dorsal hyoid arch)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(dorsal region of cerebellum)", 2);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(dorsal region)", 2);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(eye)", 1);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(fin)", 12);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(head)", 6);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(hindbrain)", 3);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(hyoid arch)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(intramembranous bone)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(material anatomical entity)", 21);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(multi-tissue structure)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(nervous system)", 4);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(opercle)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(opercular flap)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(opercular series)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(organism subdivision)", 18);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(paired fin)", 12);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(pectoral fin)", 12);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(pharyngeal arch 2)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(pharyngeal arch)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(portion of connective tissue)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(portion of tissue)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(sensory system)", 1);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(skeletal system)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(splanchnocranium)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(surface structure)", 18);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(suspensorium)", 5);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 21);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(ventral region of cerebellum)", 1);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(ventral region)", 1);
		countTableCheck.put("physical object quality^OBO_REL:inheres_in_part_of(visual system)", 1);
		countTableCheck.put("physical object quality", 21);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(anatomical cluster)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(anatomical group)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(anatomical structure)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(anatomical system)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(body)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(bone)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(cranium)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(dermal bone)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(dermatocranium)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(dorsal hyoid arch)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(head)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(hyoid arch)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(intramembranous bone)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(material anatomical entity)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(multi-tissue structure)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(opercle)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(opercular flap)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(opercular series)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(organism subdivision)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(pharyngeal arch 2)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(pharyngeal arch)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(portion of connective tissue)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(portion of tissue)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(skeletal system)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(splanchnocranium)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(surface structure)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(suspensorium)", 1);
		countTableCheck.put("physical quality^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 1);
		countTableCheck.put("physical quality", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(anatomical cluster)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(anatomical group)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(anatomical structure)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(anatomical system)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(body)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(bone)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(cranium)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(dermal bone)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(dermatocranium)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(dorsal hyoid arch)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(head)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(hyoid arch)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(intramembranous bone)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(material anatomical entity)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(multi-tissue structure)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(opercle)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(opercular flap)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(opercular series)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(organism subdivision)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(pharyngeal arch 2)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(pharyngeal arch)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(portion of connective tissue)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(portion of tissue)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(skeletal system)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(splanchnocranium)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(surface structure)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(suspensorium)", 1);
		countTableCheck.put("position^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 1);
		countTableCheck.put("position", 1);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(anatomical cluster)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(anatomical group)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(anatomical structure)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(anatomical system)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(body)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(bone)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(cranium)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(dermal bone)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(dermatocranium)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(dorsal hyoid arch)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(head)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(hyoid arch)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(intramembranous bone)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(material anatomical entity)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(multi-tissue structure)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(opercle)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(opercular flap)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(opercular series)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(organism subdivision)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(pharyngeal arch 2)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(pharyngeal arch)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(portion of connective tissue)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(portion of tissue)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(skeletal system)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(splanchnocranium)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(surface structure)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(suspensorium)", 2);
		countTableCheck.put("qualitative^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 2);
		countTableCheck.put("qualitative", 2);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(anatomical cluster)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(anatomical group)", 11);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(anatomical region)", 3);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(anatomical structure)", 23);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(anatomical system)", 11);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(body)", 23);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(bone)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(brain)", 3);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(cavitated compound organ)", 4);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(central nervous system)", 3);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(cerebellum)", 3);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(compound organ)", 4);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(cranium)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(dermal bone)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(dermatocranium)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(dorsal hyoid arch)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(dorsal region of cerebellum)", 2);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(dorsal region)", 2);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(eye)", 1);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(fin)", 12);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(head)", 8);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(hindbrain)", 3);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(hyoid arch)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(intramembranous bone)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(material anatomical entity)", 23);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(multi-tissue structure)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(nervous system)", 4);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(opercle)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(opercular flap)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(opercular series)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(organism subdivision)", 20);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(paired fin)", 12);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(pectoral fin)", 12);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(pharyngeal arch 2)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(pharyngeal arch)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(portion of connective tissue)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(portion of tissue)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(sensory system)", 1);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(skeletal system)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(splanchnocranium)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(surface structure)", 20);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(suspensorium)", 7);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 23);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(ventral region of cerebellum)", 1);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(ventral region)", 1);
		countTableCheck.put("quality^OBO_REL:inheres_in_part_of(visual system)", 1);
		countTableCheck.put("quality", 23);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(anatomical cluster)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(anatomical group)", 4);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(anatomical region)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(anatomical structure)", 4);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(anatomical system)", 4);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(body)", 4);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(bone)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(brain)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(cavitated compound organ)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(central nervous system)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(cerebellum)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(compound organ)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(cranium)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(dermal bone)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(dermatocranium)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(dorsal hyoid arch)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(dorsal region of cerebellum)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(dorsal region)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(eye)", 0);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(head)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(hindbrain)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(hyoid arch)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(intramembranous bone)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(material anatomical entity)", 4);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(multi-tissue structure)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(nervous system)", 1);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(opercle)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(opercular flap)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(opercular series)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(organism subdivision)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(pharyngeal arch 2)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(pharyngeal arch)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(portion of connective tissue)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(portion of tissue)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(sensory system)", 0);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(skeletal system)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(splanchnocranium)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(surface structure)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(suspensorium)", 3);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 4);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(ventral region of cerebellum)", 0);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(ventral region)", 0);
		countTableCheck.put("shape^OBO_REL:inheres_in_part_of(visual system)", 0);
		countTableCheck.put("shape", 4);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(anatomical group)", 3);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(anatomical region)", 2);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(anatomical structure)", 15);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(anatomical system)", 3);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(body)", 15);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(brain)", 2);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(cavitated compound organ)", 3);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(central nervous system)", 2);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(cerebellum)", 2);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(compound organ)", 3);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(dorsal region of cerebellum)", 1);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(dorsal region)", 1);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(eye)", 1);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(fin)", 12);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(head)", 1);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(hindbrain)", 2);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(material anatomical entity)", 15);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(nervous system)", 3);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(organism subdivision)", 13);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(paired fin)", 12);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(pectoral fin)", 12);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(sensory system)", 1);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(surface structure)", 13);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 15);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(ventral region of cerebellum)", 1);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(ventral region)", 1);
		countTableCheck.put("size^OBO_REL:inheres_in_part_of(visual system)", 1);
		countTableCheck.put("size", 15);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(anatomical cluster)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(anatomical group)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(anatomical structure)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(anatomical system)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(body)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(bone)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(cranium)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(dermal bone)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(dermatocranium)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(dorsal hyoid arch)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(head)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(hyoid arch)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(intramembranous bone)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(material anatomical entity)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(multi-tissue structure)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(opercle)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(opercular flap)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(opercular series)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(organism subdivision)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(pharyngeal arch 2)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(pharyngeal arch)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(portion of connective tissue)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(portion of tissue)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(skeletal system)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(splanchnocranium)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(surface structure)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(suspensorium)", 1);
		countTableCheck.put("texture^OBO_REL:inheres_in_part_of(teleost anatomical entity)", 1);
		countTableCheck.put("texture", 1);

	}


	private static final String NODEQUERY = "SELECT n.node_id FROM node AS n WHERE n.uid = ?";

	
	@Test
	public void TestGetTaxonPhenotypeLinksFromKB() throws Exception{
		int taxonid = -1;
		ResultSet r;
		Collection<TaxonPhenotypeLink> c;
		PreparedStatement p = u.getPreparedStatement(NODEQUERY);
		
		p.setString(1,TAXON1STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON1STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON2STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON2STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON3STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON3STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON4STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON4STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());

		p.setString(1,TAXON5STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON5STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(2,c.size());

		p.setString(1,TAXON6STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON6STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(2,c.size());

		p.setString(1,TAXON7STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON7STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON8STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON8STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON9STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON9STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertTrue(c.isEmpty());

		p.setString(1,TAXON10STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON10STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());

		p.setString(1,TAXON11STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON11STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());

		p.setString(1,TAXON12STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON12STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());

		p.setString(1,TAXON13STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON13STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());
		
		p.setString(1,TAXON14STR);
		r = p.executeQuery();
		if (r.next()){
			taxonid = r.getInt(1);
		}
		else{
			fail("Couldn't find node for " + TAXON14STR);
		}
		c = testAnalysis.getTaxonPhenotypeLinksFromKB(u, taxonid);
		assertNotNull(c);
		assertFalse(c.isEmpty());
		Assert.assertEquals(1,c.size());
		
	}
	
	
	@Test
	public void TestGetAllTaxonPhenotypeLinksFromKB() throws Exception{
		Map<Integer,Set<TaxonPhenotypeLink>> links = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1, u);
		assertNotNull(links);
		Assert.assertEquals(15,links.size());   //this is just the number of taxa in the KB
		for(Integer taxonID : t1.getAllTaxa()){
			assertNotNull(links.get(taxonID));
		}
	}
	
	@Test
	public void testLoadTaxonProfiles() throws SQLException{
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);		
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15, taxonProfiles.size());  //again, should be equal to the number of taxa
	}
	
	final List<String>entityNames= Arrays.asList("body","opercle","pectoral fin");
	final List<String>attNames= Arrays.asList("optical quality","shape","size");
	@Test
	public void testTraverseTaxonomy() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		Assert.assertEquals("Count of entities used",3,taxonVariation.getUsedEntities().size()); 
		for (Integer entity : taxonVariation.getUsedEntities()){
			Assert.assertTrue("Found unexpected entity " + u.getNodeName(entity.intValue()),entityNames.contains(u.getNodeName(entity.intValue())));
		}
		Assert.assertEquals("Count of qualities used",3,taxonVariation.getUsedAttributes().size());  //This should be {'shape'}
		for (Integer att : taxonVariation.getUsedAttributes()){
			Assert.assertTrue("Found unexpected attribute " + u.getNodeName(att.intValue()),attNames.contains(u.getNodeName(att.intValue())));
		}
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals("Taxon profiles at end of traverse taxonomy",15,taxonProfiles.size()); //The taxonVariation table 'knows' where the variation is, but profiles not updated yet
	}

	
	@Test
	public void testFlushUnvaryingPhenotypes() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals("Count of taxa before flush",15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals("Count of taxa with variation",3,taxonProfiles.size()); //profiles has now been trimmed to only those taxa with variation
	}

	@Test
	public void testGetAllGeneAnnotationsFromKB() throws SQLException {
		Collection<DistinctGeneAnnotationRecord> annotations = testAnalysis.getAllGeneAnnotationsFromKB(u);
		Assert.assertEquals(23,annotations.size());
	}

	@Test
	public void testProcessGeneExpression() throws SQLException {
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		testAnalysis.processGeneExpression(geneVariation, u, null);
		Set<Integer> genes = new HashSet<Integer>();
		for(Integer att : geneVariation.getUsedAttributes()){
			for (Integer ent : geneVariation.getUsedEntities()){
				if (geneVariation.hasExhibitorSet(ent,att)){					
					genes.addAll(geneVariation.getExhibitorSet(ent,att));
				}
			}
		}
		System.out.println("Genes");
		for (Integer gene : genes){
			System.out.println(u.getNodeName(gene));
		}
	}

	@Test
	public void testBuildEQParents() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
//		for(PhenotypeExpression pe : phenotypeParentCache.keySet()){
//			pe.fillNames(u);
//			System.out.println("Expression is " + pe);
//			for (PhenotypeExpression peParent : phenotypeParentCache.get(pe)){
//				peParent.fillNames(u);
//				System.out.println("  Parent is " + peParent);
//			}
//		}
	}

	@Test
	public void testFillCountTable() throws SQLException {
		setupCountTableCheck();
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		CountTable counts = new CountTable();
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		for(PhenotypeExpression p : counts.getPhenotypes()){
			p.fillNames(u);
			System.out.println("Phenotype: " + p.getFullName(u) + " count: " + counts.getRawCount(p));
		}
	}


	@Test
	public void testBuildPhenotypeMatchCache() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles= geneProfiles;
		CountTable counts = new CountTable();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
	}

	@Test
	public void testWritePhenotypeMatchSummary() {
		
		fail("Not yet implemented");
	}



	@Test
	public void testCalcMaxIC() throws SQLException {
		t1.traverseOntologyTree(u);
		Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
		HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
		testAnalysis.taxonProfiles= taxonProfiles;
		final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
		testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
		assertFalse(taxonProfiles.isEmpty());
		Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
		testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
		VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
		HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
		testAnalysis.geneProfiles= geneProfiles;
		CountTable counts = new CountTable();
		Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
		Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
		PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
		testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
		testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
		testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
		int order1ID = u.getIDFromName("Order 1");
		int genus1ID = u.getIDFromName("Genus 1");
		int genus2ID = u.getIDFromName("Genus 2");
		int jag1bID = u.getIDFromName("jag1b");
		int apcID = u.getIDFromName("apc");
		
		double maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
				  				                   geneProfiles.get(jag1bID).getAllEAPhenotypes(),
				  				                   phenotypeScores);
		System.out.println("maxICScore = " + maxICScore);
		Assert.assertTrue("Expected " + IC3 + "; found " + maxICScore, softCompare(maxICScore,IC3));
		
		maxICScore = testAnalysis.calcMaxIC(taxonProfiles.get(order1ID).getAllEAPhenotypes(),
				                            geneProfiles.get(apcID).getAllEAPhenotypes(),
				                            phenotypeScores);
		System.out.println("maxICScore = " + maxICScore);
		Assert.assertTrue("Expected " + IC4 + "; found " + maxICScore,softCompare(maxICScore,IC4));
	
	}

	@Test
	public void testCalcICCS() {
		fail("Not yet implemented");
	}

	@Test
	public void testProfileMatchReport() throws SQLException{
	t1.traverseOntologyTree(u);
	Map<Integer,Set<TaxonPhenotypeLink>> allLinks = testAnalysis.getAllTaxonPhenotypeLinksFromKB(t1,u);
	HashMap<Integer,Profile>taxonProfiles = testAnalysis.loadTaxonProfiles(allLinks,u, attMap, nodeIDofQuality, badQualities);
	testAnalysis.taxonProfiles= taxonProfiles;
	final VariationTable taxonVariation = new VariationTable(VariationTable.VariationType.TAXON);
	testAnalysis.traverseTaxonomy(t1, t1.getRootNodeID(), taxonProfiles, taxonVariation, u);
	assertFalse(taxonProfiles.isEmpty());
	Assert.assertEquals(15,taxonProfiles.size()); //profiles before the flush includes all taxa
	testAnalysis.flushUnvaryingPhenotypes(taxonProfiles,taxonVariation,u);
	VariationTable geneVariation = new VariationTable(VariationTable.VariationType.GENE);
	HashMap<Integer,Profile>geneProfiles = testAnalysis.processGeneExpression(geneVariation, u, null);
	testAnalysis.geneProfiles= geneProfiles;
	CountTable counts = new CountTable();
	Map <PhenotypeExpression,Set<PhenotypeExpression>> phenotypeParentCache = new HashMap<PhenotypeExpression,Set<PhenotypeExpression>>();
	Map <Integer,Set<Integer>> entityParentCache = u.setupEntityParents();
	PhenotypeScoreTable phenotypeScores = new PhenotypeScoreTable();
	testAnalysis.buildEQParents(phenotypeParentCache,entityParentCache,u);
	testAnalysis.fillCountTable(geneProfiles, counts, phenotypeParentCache, u, PhenotypeProfileAnalysis.GENEPHENOTYPECOUNTQUERY, PhenotypeProfileAnalysis.GENEQUALITYCOUNTQUERY, u.countDistinctGenePhenotypeAnnotations());
	testAnalysis.buildPhenotypeMatchCache(phenotypeParentCache, phenotypeScores, counts, u);
	testAnalysis.profileMatchReport(phenotypeScores, null, null, u);
	}
	
	/**
	 * Compares double values to within a range of the expected value (avoiding exact comparison of doubles)
	 * @param value
	 * @param expected
	 * @return
	 */
	private boolean softCompare(double value, double expected){
		if ((value < 1.0001*expected) && (0.9999*expected < value))
			return true;
		return false;
	}
	


	
	@After
	public void tearDown() throws Exception {
		u.closeKB();
	}

}
