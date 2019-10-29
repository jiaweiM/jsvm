package jsvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static java.util.Objects.requireNonNull;

/**
 * This class store dataset.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 10:47 AM
 */
public class SVMProblem implements java.io.Serializable
{
    public int l;
    public double[] y;
    public SVMNode[][] x;
    private int maxIndex;

    public SVMProblem() { }

    /**
     * Construct with a file
     *
     * @param path {@link Path} of the file.
     */
    public SVMProblem(Path path)
    {
        requireNonNull(path);
        if (Files.notExists(path)) {
            throw new IllegalArgumentException(path + " is not exist!");
        }

        List<Double> vy = new ArrayList<>();
        List<SVMNode[]> vx = new ArrayList<>();
        maxIndex = 0;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {

//                System.out.println(line);
                StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
                vy.add(Double.parseDouble(st.nextToken()));

                int m = st.countTokens() / 2;
                SVMNode[] x = new SVMNode[m];
                for (int j = 0; j < m; j++) {
                    x[j] = new SVMNode(Integer.parseInt(st.nextToken()), Double.parseDouble(st.nextToken()));
                }
                if (m > 0) {
                    maxIndex = Math.max(maxIndex, x[m - 1].index);
                }
                vx.add(x);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.l = vy.size();

        this.x = new SVMNode[l][];
        for (int i = 0; i < l; i++) {
            x[i] = vx.get(i);
        }
        y = new double[l];
        for (int i = 0; i < l; i++) {
            y[i] = vy.get(i);
        }
    }

    public int getMaxIndex()
    {
        return maxIndex;
    }
}
