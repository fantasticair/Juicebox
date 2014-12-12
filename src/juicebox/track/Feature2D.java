/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2014 Broad Institute, Aiden Lab
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

package juicebox.track;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Map;
import java.text.*;

/**
 * chr1	x1	x2	chr2	y1	y2	color	observed	bl expected	donut expected	bl fdr	donut fdr
 *
 * @author jrobinso
 *         Date: 5/22/13
 *         Time: 8:51 AM
 *         <p/>
 *         Chr Chr pos pos observed expected1 expected2 fdr
 */
public class Feature2D {

    private final NumberFormat formatter = NumberFormat.getInstance();

    private final String chr1;
    private final int start1;
    private final int end1;
    private final String chr2;
    private final int start2;
    private final int end2;
    private final Color color;
    private final Map<String, String> attributes;
    private final String featureName;


    public Feature2D(String featureName, String chr1, int start1, int end1, String chr2, int start2, int end2, Color c,
                     Map<String, String> attributes) {
        this.featureName = featureName;
        this.chr1 = chr1;
        this.start1 = start1;
        this.end1 = end1;
        this.chr2 = chr2;
        this.start2 = start2;
        this.end2 = end2;
        this.color = (c == null ? Color.black : c);
        this.attributes = attributes;
    }


    public String getChr1() {
        return chr1;
    }

    public String getChr2() {
        return chr2;
    }

    public int getStart1() {
        return start1;
    }

    public int getStart2() {
        return start2;
    }

    public int getEnd1() {
        return end1;
    }

    public int getEnd2() {
        return end2;
    }

    public Color getColor() {
        return color;
    }

    public String tooltipText() {

        StringBuilder txt = new StringBuilder();



        txt.append("<span style='color:red; font-family: arial; font-size: 12pt;'>");
        txt.append(featureName);
        txt.append("</span><br>");

        txt.append("<span style='font-family: arial; font-size: 12pt;'>");
        txt.append(chr1).append(":").append(formatter.format(start1 + 1));
        if ((end1 - start1) > 1) {
            txt.append("-").append(formatter.format(end1));
        }

        txt.append("</span><br>");

        txt.append("<span style='font-family: arial; font-size: 12pt;'>");
        txt.append(chr2).append(":").append(formatter.format(start2 + 1));
        if ((end2 - start2) > 1) {
            txt.append("-").append(formatter.format(end2));
        }
        txt.append("</span>");
        DecimalFormat df = new DecimalFormat("#.##");

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String tmpKey = entry.getKey();
            if(!(tmpKey.equals("f1") || tmpKey.equals("f2") || tmpKey.equals("f3")||tmpKey.equals("f4")||tmpKey.equals("f5")))
            {
                txt.append("<br>");
                txt.append("<span style='font-family: arial; font-size: 12pt;'>");
                txt.append(tmpKey);
                txt.append(" = <b>");
                txt.append(df.format(Double.valueOf(entry.getValue())));
                txt.append("</b>");
                txt.append("</span>");
            }
        }
        return txt.toString();
    }

}
