
public class MatrixProd {

  private static final long MILLIS_PER_SECOND = 1000;

  private static double simpleCycle(double[] op1Matrix, double[] op2Matrix, double[] resMatrix, int matrixSize) {

    double temp;
    int i,j,k;
    long time1 = System.currentTimeMillis();
    for (i = 0; i < matrixSize; i++) {
      for (j = 0; j < matrixSize; j++) {
        temp = 0;
        for (k = 0; k < matrixSize; k++) {
          temp += op1Matrix[i * matrixSize + k] * op2Matrix[k * matrixSize + j];
        }
        resMatrix[i * matrixSize + j] = temp;
      }
    }

    long time2 = System.currentTimeMillis();

    return (double) (time2 - time1) / MILLIS_PER_SECOND;
  }

  private static double optimCycle(double[] op1Matrix, double[] op2Matrix, double[] resMatrix, int matrixSize) {

    int i, j, k;
    long time1 = System.currentTimeMillis();
    for (i = 0; i < matrixSize; i++) {
      for (k = 0; k < matrixSize; k++) {
        for (j = 0; j < matrixSize; j++) {
          resMatrix[i * matrixSize + j] += op1Matrix[i * matrixSize + k] * op2Matrix[k * matrixSize + j];
        }
      }
    }

    long time2 = System.currentTimeMillis();

    return (double) (time2 - time1) / MILLIS_PER_SECOND;
  }

  private static double blockSimpleCycle(double[] op1Matrix, double[] op2Matrix, double[] resMatrix, int matrixSize,
      int blockSize) {

    double temp;
    int jj, kk, i, j, k;
    long time1 = System.currentTimeMillis();

    for (jj = 0; jj < matrixSize; jj = jj + blockSize)
      for (kk = 0; kk < matrixSize; kk = kk + blockSize)
        for (i = 0; i < matrixSize; i = i + 1)
          for (j = jj; j < jj + blockSize; j = j + 1) {
            temp = 0;
            for (k = kk; k < kk + blockSize; k = k + 1)
              temp = temp + op1Matrix[i * matrixSize + k] * op2Matrix[k * matrixSize + j];

            resMatrix[i * matrixSize + j] = resMatrix[i * matrixSize + j] + temp;
          }

    long time2 = System.currentTimeMillis();

    return (double) (time2 - time1) / MILLIS_PER_SECOND;
  }

  private static double blockOptimCycle(double[] op1Matrix, double[] op2Matrix, double[] resMatrix, int matrixSize,
      int blockSize) {

    int jj, kk, i, k, j;
    long time1 = System.currentTimeMillis();

    for (jj = 0; jj < matrixSize; jj = jj + blockSize)
      for (kk = 0; kk < matrixSize; kk = kk + blockSize)
        for (i = 0; i < matrixSize; i = i + 1)
          for (k = kk; k < kk + blockSize; k = k + 1) {
            for (j = jj; j < jj + blockSize; j = j + 1)
              resMatrix[i * matrixSize + j] += op1Matrix[i * matrixSize + k] * op2Matrix[k * matrixSize + j];

          }

    long time2 = System.currentTimeMillis();

    return (double) (time2 - time1) / MILLIS_PER_SECOND;
  }

  public static void main(String[] args) {
    if (args.length < 3) {
      System.err.println("ERROR: insufficient number of arguments (square matrix size, runs, operation)");
      System.exit(-1);
    }

    // Get arguments

    int matrixSize = Integer.parseInt(args[0]);
    int runs = Integer.parseInt(args[1]);
    int op = Integer.parseInt(args[2]);
    int blockSize = args.length == 4 ? Integer.parseInt(args[3]) : matrixSize*matrixSize;

    // init matrices
    double [] op1Matrix = new double[matrixSize*matrixSize];
    double [] op2Matrix = new double[matrixSize*matrixSize];
    double [] resMatrix = new double[matrixSize*matrixSize];

    for (int i = 0; i < matrixSize; i++){
      for (int j = 0; j < matrixSize; j++){
        op1Matrix[i * matrixSize + j] = 1.0;
        op2Matrix[i * matrixSize + j] = (double)(i + 1);
        resMatrix[i * matrixSize + j] = (double)0;
      }
    }

    for(int i = 0; i < runs; i++) {

      // Start counting
      double algorithmTime = 0;

      switch (op) {
        case 1:
          algorithmTime = simpleCycle(op1Matrix, op2Matrix, resMatrix, matrixSize);
          break;
        case 2:
          algorithmTime = optimCycle(op1Matrix, op2Matrix, resMatrix, matrixSize);
          break;
        case 3:
          algorithmTime = blockSimpleCycle(op1Matrix, op2Matrix, resMatrix, matrixSize, blockSize);
          break;
        case 4:
          algorithmTime = blockOptimCycle(op1Matrix, op2Matrix, resMatrix, matrixSize, blockSize);
          break;
        default:
          break;
      }

      System.out.println(algorithmTime);

    }

  }
}