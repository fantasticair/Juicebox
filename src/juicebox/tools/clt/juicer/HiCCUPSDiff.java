/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2016 Broad Institute, Aiden Lab
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

import juicebox.tools.clt.CommandLineParserForJuicer;
import juicebox.tools.clt.JuicerCLT;
import java.io.File;
import juicebox.track.feature.Feature2D;

/**
 * HiCCUPS Diff
 * <p/>
 * Developed by Suhas Rao, ported by Suhas Rao + Neva Durand
 * <p/>
 * -------
 * Takes as input two Hi-C maps and their associated loop calls <br>
 * Outputs the differential loop list      <br>
 * Other parameters are used for the two HiCCUPS calls on the alternate loop lists
 * @see juicebox.tools.clt.juicer.HiCCUPS
 * hiccupsdiff [-m matrixSize] [-k normalization (NONE/VC/VC_SQRT/KR)] [-c chromosome(s)] [-r resolution(s)]
 * [-f fdr] [-p peak width] [-i window] [-t thresholds] [-d centroid distances]
 *  <firstHicFile> <secondHicFile> <firstLoopList> <secondLoopList> <outputDirectory>
 *  firstLoopList is the loop list generated by running HiCCUPS on firstHicFile
 *  secondLoopList is the loop list generated by running HiCCUPS on secondHicFile
 */
public class HiCCUPSDiff extends JuicerCLT {

    private HiCCUPS hiccups1;
    private HiCCUPS hiccups2;

    public HiCCUPSDiff() {
        super("hiccupsdiff [-m matrixSize] [-k normalization (NONE/VC/VC_SQRT/KR)] [-c chromosome(s)] [-r resolution(s)] " +
                "[-f fdr] [-p peak width] [-i window] [-t thresholds] [-d centroid distances] " +
                "<firstHicFile> <secondHicFile> <firstLoopList> <secondLoopList> <outputDirectory>");

    }

    @Override
    protected void readJuicerArguments(String[] args, CommandLineParserForJuicer juicerParser) {
        if (args.length != 6) {
            printUsage();
            System.exit(1);
        }

        String outputDirectory = args[5];

        File dir = new File(outputDirectory);

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.err.println("Couldn't create output directory " + outputDirectory);
                System.exit(1);
            }
        }

        String[] args1 = new String[4];
        args1[0] = "hiccups";
        args1[1] = args[1];
        args1[2] = outputDirectory + File.separator + "file1";
        args1[3] = args[4];
        hiccups1 = new HiCCUPS();
        hiccups1.readJuicerArguments(args1, juicerParser);

        String[] args2 = new String[4];
        args2[0] = "hiccups";
        args2[1] = args[2];
        args2[2] = outputDirectory + File.separator + "file2";
        args2[3] = args[3];
        hiccups2 = new HiCCUPS();
        hiccups2.readJuicerArguments(args2, juicerParser);

    }

    @Override
    public void run() {
        hiccups1.run();
        hiccups2.run();
                       /*


# identify conserved loops, parameters from RH2014
/aidenlab/work/suhas/scripts/loop_analysis/centroid_reproducibility_matrix2.py $looplist1 $looplist2 50000 0.2 > $output_dir"/conserved_loops_list2.txt"
/aidenlab/work/suhas/scripts/loop_analysis/centroid_reproducibility_matrix2.py $looplist2 $looplist1 50000 0.2 > $output_dir"/conserved_loops_list1.txt"

# identify differential loops

maxenrich=1.3 #this can be made an input parameter

# print candidate diff loops
awk -v cllist=$output_dir"/conserved_loops_list1.txt" 'BEGIN{while(getline<cllist>0){peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]++}}{if (NR==1) {print $0} else if (peak[$1 " " $2 " " $3 " "$4 " " $5 " " $6]=="") {print $0}}' $looplist1 > $output_dir"/tmp1"
awk -v cllist=$output_dir"/conserved_loops_list2.txt" 'BEGIN{while(getline<cllist>0){peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]++}}{if (NR==1) {print $0} else if (peak[$1 " " $2 " " $3 " "$4 " " $5 " " $6]=="") {print $0}}' $looplist2 > $output_dir"/tmp2"

# print diff loops under max enrich parameter for all filters
awk -v list5kb=$output_dir"/file1/requested_list_5000" -v list10kb=$output_dir"/file1/requested_list_10000" -v list25kb=$output_dir"/file1/requested_list_25000" -v me=$maxenrich 'BEGIN{while(getline<list5kb>0) {if ($8<me*$9&&$8<me*$10&&$8<me*$11&&$8<me*$12) {peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]++}} while(getline<list10kb>0){if ($8<me*$9&&$8<me*$10&&$8<me*$11&&$8<me*$12) {peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]++}} while(getline<list25kb>0){if ($8<me*$9&&$8<me*$10&&$8<me*$11&&$8<me*$12) {peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]++}}}{if (NR==1) {print $0} else if {peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]>0) {print $0}}' $output_dir"/tmp2" > $output_dir"/differential_loops_list2.txt"
awk -v list5kb=$output_dir"/file2/requested_list_5000" -v list10kb=$output_dir"/file2/requested_list_10000" -v list25kb=$output_dir"/file2/requested_list_25000" -v me=$maxenrich 'BEGIN{while(getline<list5kb>0) {if ($8<me*$9&&$8<me*$10&&$8<me*$11&&$8<me*$12) {peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]++}} while(getline<list10kb>0){if ($8<me*$9&&$8<me*$10&&$8<me*$11&&$8<me*$12) {peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]++}} while(getline<list25kb>0){if ($8<me*$9&&$8<me*$10&&$8<me*$11&&$8<me*$12) {peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]++}}}{if (NR==1) {print $0} else if {peak[$1 " " $2 " " $3 " " $4 " " $5 " " $6]>0) {print $0}}' $output_dir"/tmp1" > $output_dir"/differential_loops_list1.txt"
rm $output_dir"/tmp1"
rm $output_dir"/tmp2"

                        */
    }
}
