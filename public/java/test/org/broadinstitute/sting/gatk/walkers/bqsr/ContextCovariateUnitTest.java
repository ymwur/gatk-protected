package org.broadinstitute.sting.gatk.walkers.bqsr;

import org.broadinstitute.sting.gatk.GenomeAnalysisEngine;
import org.broadinstitute.sting.utils.sam.ArtificialSAMUtils;
import org.broadinstitute.sting.utils.sam.GATKSAMRecord;
import org.broadinstitute.sting.utils.sam.ReadUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.BitSet;
import java.util.Random;

/**
 * @author Mauricio Carneiro
 * @since 3/1/12
 */
public class ContextCovariateUnitTest {
    ContextCovariate covariate;
    RecalibrationArgumentCollection RAC;
    Random random;

    @BeforeClass
    public void init() {
        RAC = new RecalibrationArgumentCollection();
        covariate = new ContextCovariate();
        random = GenomeAnalysisEngine.getRandomGenerator();
        covariate.initialize(RAC);

    }

    @Test(enabled = true)
    public void testSimpleContexts() {
        byte[] quals = ReadUtils.createRandomReadQuals(10000);
        byte[] bbases = ReadUtils.createRandomReadBases(10000, true);
        String bases = stringFrom(bbases);
        //        System.out.println("Read: " + bases);
        GATKSAMRecord read = ArtificialSAMUtils.createArtificialRead(bbases, quals, bbases.length + "M");
        CovariateValues values = covariate.getValues(read);
        verifyCovariateArray(values.getMismatches(), RAC.MISMATCHES_CONTEXT_SIZE, bases);
        verifyCovariateArray(values.getInsertions(), RAC.INSERTIONS_CONTEXT_SIZE, bases);
        verifyCovariateArray(values.getDeletions(), RAC.DELETIONS_CONTEXT_SIZE, bases);
    }

    private void verifyCovariateArray(BitSet[] values, int contextSize, String bases) {
        for (int i = 0; i < values.length; i++) {
            String expectedContext = covariate.NO_CONTEXT_VALUE;
            if (i >= contextSize) {
                String context = bases.substring(i - contextSize, i);
                if (!context.contains("N"))
                    expectedContext = context;
            }
            //            System.out.println(String.format("Context [%d]:\n%s\n%s\n", i, covariate.keyFromBitSet(values[i]), expectedContext));
            Assert.assertEquals(covariate.keyFromBitSet(values[i]), expectedContext);
        }
    }

    private String stringFrom(byte[] array) {
        String s = "";
        for (byte value : array)
            s += (char) value;
        return s;
    }

}
