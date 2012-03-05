package phenoscape.queries;

import java.sql.SQLException;

import phenoscape.queries.lib.Utils;
import junit.framework.Assert;

public abstract class PropTreeTest {

	
	protected static final String UNITTESTROOT = "TTO:0000015";	
	protected static final String UNITTESTKB = "unitTestconnection.properties"; 
	protected static final String TAXON1STR = "TTO:0000001";
	protected static final String TAXON2STR = "TTO:0000002";
	protected static final String TAXON3STR = "TTO:0000003";
	protected static final String TAXON4STR = "TTO:0000004";
	protected static final String TAXON5STR = "TTO:0000005";
	protected static final String TAXON6STR = "TTO:0000006";
	protected static final String TAXON7STR = "TTO:0000007";
	protected static final String TAXON8STR = "TTO:0000008";
	protected static final String TAXON9STR = "TTO:0000009";
	protected static final String TAXON10STR = "TTO:0000010";
	protected static final String TAXON11STR = "TTO:0000011";
	protected static final String TAXON12STR = "TTO:0000012";
	protected static final String TAXON13STR = "TTO:0000013";
	protected static final String TAXON14STR = "TTO:0000014";
	
	protected static final String GENEPHENOTYPECOUNTQUERY =
		"SELECT count(*) FROM distinct_gene_annotation  WHERE distinct_gene_annotation.phenotype_node_id IN " +
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link phenotype_inheres_in_part_of ON (phenotype_inheres_in_part_of.node_id = phenotype.node_id AND phenotype_inheres_in_part_of.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:inheres_in_part_of')) " +
		"JOIN link quality_is_a ON (quality_is_a.node_id = phenotype.node_id AND quality_is_a.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a')) " +
		"WHERE (phenotype_inheres_in_part_of.object_id =  ?  AND quality_is_a.object_id = ?))";

	protected static final String GENEQUALITYCOUNTQUERY =
		"SELECT count(*) FROM distinct_gene_annotation  WHERE distinct_gene_annotation.phenotype_node_id IN " +
		"(SELECT phenotype.node_id from phenotype " +
		"JOIN link quality_is_a ON (quality_is_a.node_id = phenotype.node_id AND quality_is_a.predicate_id = (SELECT node.node_id FROM node WHERE node.uid='OBO_REL:is_a')) " +
		"WHERE (quality_is_a.object_id = ?))";

	
	
	

	protected final int genePhenotypeAnnotationCount = 23;   // True independent of the taxon data loaded
	
	protected final double IC1 = -1*(Math.log(1.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	protected final double IC2 = -1*(Math.log(2.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	protected final double IC3 = -1*(Math.log(3.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	protected final double IC4 = -1*(Math.log(4.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	protected final double IC12 = -1*(Math.log(12.0/(double)genePhenotypeAnnotationCount)/Math.log(2));
	protected final double IC13 = -1*(Math.log(13.0/(double)genePhenotypeAnnotationCount)/Math.log(2));

	
	int order1ID;
	int family1ID;
	int genus1ID;
	int genus2ID;
	int genus3ID;
	int alfID;
	int apaID;
	int apcID;
	int brpf1ID;
	int cyp26b1ID;
	int edn1ID;
	int fgf8aID;
	int fgf24ID;
	int furinaID;
	int henID;
	int jag1bID;
	int lama5ID;
	int lofID;
	int macf1ID;
	int rndID;
	int sec23aID;
	int sec24dID;
	int shhaID;
	int ugdhID;
	
	int opercleID;
	int dorsalRegionOfCerebellumID;
	int eyeID;
	int pectoralFinID;
	int ventralRegionOfCerebellumID;
	
	int countID;
	int positionID;
	int shapeID;
	int sizeID;
	int textureID;

	
	protected void initNames(Utils u) throws SQLException{

		
		order1ID = u.getIDFromName("Order 1");
		Assert.assertFalse("failed to lookup taxon Order 1",order1ID==-1);

		family1ID = u.getIDFromName("Family 1");
		Assert.assertFalse("failed to lookup taxon Family 1",family1ID==-1);
		
		genus1ID = u.getIDFromName("Genus 1");
		Assert.assertFalse("failed to lookup taxon Genus 1",genus1ID==-1);

		genus2ID = u.getIDFromName("Genus 2");
		Assert.assertFalse("failed to lookup taxon Genus 2",genus2ID==-1);

		genus3ID = u.getIDFromName("Genus 3");
		Assert.assertFalse("failed to lookup taxon Genus 3",genus3ID==-1);
		
		alfID = u.getIDFromName("alf");
		Assert.assertFalse("failed to lookup gene alf",alfID==-1);

		apaID = u.getIDFromName("apa");
		Assert.assertFalse("failed to lookup gene apa",apaID==-1);
		
		apcID = u.getIDFromName("apc");
		Assert.assertFalse("failed to lookup gene apc",apcID==-1);
		
		brpf1ID = u.getIDFromName("brpf1");
		Assert.assertFalse("failed to lookup gene apc",apcID==-1);
		
		cyp26b1ID = u.getIDFromName("cyp26b1");
		Assert.assertFalse("failed to lookup gene cyp26b1",cyp26b1ID==-1);
		
		edn1ID = u.getIDFromName("edn1");
		Assert.assertFalse("failed to lookup gene edn1",edn1ID==-1);
		
		fgf8aID = u.getIDFromName("fgf8a");
		Assert.assertFalse("failed to lookup gene fgf8a",fgf8aID==-1);

		fgf24ID = u.getIDFromName("fgf24");
		Assert.assertFalse("failed to lookup gene fgf24",fgf24ID==-1);
		
		furinaID = u.getIDFromName("furina");
		Assert.assertFalse("failed to lookup gene furina",furinaID==-1);
		
		henID = u.getIDFromName("hen");
		Assert.assertFalse("failed to lookup gene hen",henID==-1);
		
		jag1bID = u.getIDFromName("jag1b");
		Assert.assertFalse("failed to lookup gene jag1b",jag1bID==-1);
		
		lama5ID = u.getIDFromName("lama5");
		Assert.assertFalse("failed to lookup gene lama5",lama5ID==-1);
		
		lofID = u.getIDFromName("lof");
		Assert.assertFalse("failed to lookup gene lof",lofID==-1);
		
		macf1ID = u.getIDFromName("macf1");
		Assert.assertFalse("failed to lookup gene macf1",macf1ID==-1);
		
		rndID = u.getIDFromName("rnd");
		Assert.assertFalse("failed to lookup gene rnd",rndID==-1);
		
		sec23aID = u.getIDFromName("sec23a");
		Assert.assertFalse("failed to lookup gene sec23a",sec23aID==-1);
		
		sec24dID = u.getIDFromName("sec24d");
		Assert.assertFalse("failed to lookup gene sec24d",sec24dID==-1);
		
		shhaID = u.getIDFromName("shha");
		Assert.assertFalse("failed to lookup gene shha",shhaID==-1);
		
		ugdhID = u.getIDFromName("ugdh");
		Assert.assertFalse("failed to lookup gene ugdh",ugdhID==-1);


		opercleID = u.getIDFromName("opercle");
		if (opercleID == -1){
			u.cacheOneNode(u.getOneName("opercle"));
			opercleID = u.getIDFromName("opercle");
		}

		//BSPO:0000079^OBO_REL:part_of(TAO:0000100)
		dorsalRegionOfCerebellumID = u.getOneUID("BSPO:0000079^OBO_REL:part_of(TAO:0000100)");

		eyeID = u.getIDFromName("eye");
		if (eyeID == -1){
			u.cacheOneNode(u.getOneName("eye"));
			eyeID = u.getIDFromName("eye");
		}

		pectoralFinID = u.getOneUID("TAO:0001161");
		
		//BSPO:0000084^OBO_REL:part_of(TAO:0000100)  
		ventralRegionOfCerebellumID = u.getOneUID("BSPO:0000084^OBO_REL:part_of(TAO:0000100)");
		
		countID = u.getIDFromName("count");
		positionID = u.getIDFromName("position");
		shapeID = u.getIDFromName("shape");
		sizeID = u.getIDFromName("size");
		textureID = u.getIDFromName("texture");

	}
	
	
	/**
	 * Compares double values to within a range of the expected value (avoiding exact comparison of doubles)
	 * @param value
	 * @param expected
	 * @return
	 */
	boolean softCompare(double value, double expected){
		if ((value <= 1.0001*expected) && (0.9999*expected <= value))
			return true;
		return false;
	}


	
}