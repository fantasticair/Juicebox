/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2015 Broad Institute, Aiden Lab
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package juicebox.tools.clt.juicer;

import jargs.gnu.CmdLineParser;
import juicebox.HiC;
import juicebox.HiCGlobals;
import juicebox.data.Dataset;
import juicebox.data.HiCFileTools;
import juicebox.data.Matrix;
import juicebox.data.MatrixZoomData;
import juicebox.tools.clt.CommandLineParserForJuicer;
import juicebox.tools.clt.JuicerCLT;
import juicebox.tools.utils.juicer.arrowhead.ArrowheadScoreList;
import juicebox.tools.utils.juicer.arrowhead.BlockBuster;
import juicebox.track.feature.Feature2DList;
import juicebox.windowui.HiCZoom;
import juicebox.windowui.NormalizationType;
import org.broad.igv.Globals;
import org.broad.igv.feature.Chromosome;

import java.util.*;

/**
 * Created by nchernia on 1/9/15.
 */
public class Arrowhead extends JuicerCLT {

    private static int matrixSize = 2000;
    private String file, outputPath;
    private int resolution = -100;
    private Set<String> givenChromosomes = null;
    private NormalizationType norm = NormalizationType.KR;

    public Arrowhead() {
        super("arrowhead [-c chromosome(s)] [-m matrix size] [NONE/VC/VC_SQRT/KR] <input_HiC_file(s)> <output_file> <resolution>");// [list] [control]
        HiCGlobals.useCache = false;
    }

    @Override
    public void readArguments(String[] args, CmdLineParser parser) {

        CommandLineParserForJuicer juicerParser = (CommandLineParserForJuicer) parser;
        //setUsage("juicebox arrowhead hicFile resolution");
        if (args.length < 4 || args.length > 5) {
            printUsage();
        }

        int i = 1;
        if (args.length == 5) {
            norm = retrieveNormalization(args[i++]);
        }

        file = args[i++];
        outputPath = args[i++];

        try {
            resolution = Integer.valueOf(args[i++]);
        } catch (NumberFormatException error) {
            printUsage();
        }
        if (juicerParser.getChromosomeOption() != null)
            givenChromosomes = new HashSet<String>(juicerParser.getChromosomeOption());
        int specifiedMatrixSize = juicerParser.getMatrixSizeOption();
        if (specifiedMatrixSize % 2 == 1)
            specifiedMatrixSize += 1;
        if (specifiedMatrixSize > 50)
            matrixSize = specifiedMatrixSize;

    }

    @Override
    public void run() {

        // might need to catch OutofMemory errors.  10Kb => 8GB, 5Kb => 12GB in original script
        Dataset ds = HiCFileTools.extractDatasetForCLT(Arrays.asList(file.split("\\+")), true);

        List<Chromosome> chromosomes = ds.getChromosomes();
        if (givenChromosomes != null)
            chromosomes = new ArrayList<Chromosome>(HiCFileTools.stringToChromosomes(givenChromosomes,
                    chromosomes));

        // Note: could make this more general if we wanted, to arrowhead calculation at any BP or FRAG resolution
        HiCZoom zoom = new HiCZoom(HiC.Unit.BP, resolution);

        Feature2DList contactDomainsGenomeWide = new Feature2DList();
        Feature2DList contactDomainListScoresGenomeWide = new Feature2DList();
        Feature2DList contactDomainControlScoresGenomeWide = new Feature2DList();

        for (Chromosome chr : chromosomes) {
            if (chr.getName().equals(Globals.CHR_ALL)) continue;

            Matrix matrix = ds.getMatrix(chr, chr);

            if (matrix == null) continue;
            System.out.println("\nProcessing " + chr.getName());
            MatrixZoomData zd = matrix.getZoomData(zoom);
            // todo use given lists
            ArrowheadScoreList list = new ArrowheadScoreList();
            ArrowheadScoreList control = new ArrowheadScoreList();

            BlockBuster.run(chr.getIndex(), chr.getName(), chr.getLength(), resolution, matrixSize,
                    zd, list, control, norm,
                    contactDomainsGenomeWide, contactDomainListScoresGenomeWide, contactDomainControlScoresGenomeWide);
        }

        contactDomainsGenomeWide.exportFeatureList(outputPath + "_" + resolution + "_blocks", false);
        contactDomainListScoresGenomeWide.exportFeatureList(outputPath + "_" + resolution + "_list_scores", false);
        contactDomainControlScoresGenomeWide.exportFeatureList(outputPath + "_" + resolution + "_control_scores", false);
    }
}