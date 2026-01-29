package com.example.projekt.reedsolomon;

public final class ReedSolomonDecoderGF {
    private final GenericGF field;

    public ReedSolomonDecoderGF(GenericGF field) { this.field = field; }

    public void decode(int[] received, int twoS) throws Exception {
        GenericGFPoly poly = new GenericGFPoly(field, received);
        int[] syndromeCoefficients = new int[twoS];
        boolean noError = true;
        for (int i = 0; i < twoS; i++) {
            int eval = poly.evaluateAt(field.exp(i + field.getGeneratorBase()));
            syndromeCoefficients[syndromeCoefficients.length - 1 - i] = eval;
            if (eval != 0) noError = false;
        }
        if (noError) return;
        GenericGFPoly syndrome = new GenericGFPoly(field, syndromeCoefficients);
        GenericGFPoly[] sigmaOmega = runEuclideanAlgorithm(field.buildMonomial(twoS, 1), syndrome, twoS);
        GenericGFPoly sigma = sigmaOmega[0];
        GenericGFPoly omega = sigmaOmega[1];
        int[] errorLocations = findErrorLocations(sigma);
        int[] errorMagnitudes = findErrorMagnitudes(omega, errorLocations);
        for (int i = 0; i < errorLocations.length; i++) {
            int position = received.length - 1 - field.log(errorLocations[i]);
            if (position < 0) throw new Exception("Bad error location");
            received[position] = GenericGF.addOrSubtract(received[position], errorMagnitudes[i]);
        }
    }

    private GenericGFPoly[] runEuclideanAlgorithm(GenericGFPoly a, GenericGFPoly b, int R) throws Exception {
        if (a.getDegree() < b.getDegree()) {
            GenericGFPoly temp = a; a = b; b = temp;
        }
        GenericGFPoly rLast = a;
        GenericGFPoly r = b;
        GenericGFPoly tLast = field.getZero();
        GenericGFPoly t = field.getOne();
        while (r.getDegree() >= R / 2) {
            GenericGFPoly rLastLast = rLast;
            GenericGFPoly tLastLast = tLast;
            rLast = r;
            tLast = t;
            if (rLast.isZero()) throw new Exception("r_{i-1} was zero");
            r = rLastLast;
            GenericGFPoly q = field.getZero();
            int denominatorLeadingTerm = rLast.getCoefficient(rLast.getDegree());
            int dltInverse = field.inverse(denominatorLeadingTerm);
            while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
                int degreeDiff = r.getDegree() - rLast.getDegree();
                int scale = field.multiply(r.getCoefficient(r.getDegree()), dltInverse);
                q = q.addOrSubtract(field.buildMonomial(degreeDiff, scale));
                r = r.addOrSubtract(rLast.multiplyByMonomial(degreeDiff, scale));
            }
            t = q.multiply(tLast).addOrSubtract(tLastLast);
            if (r.getDegree() >= rLast.getDegree()) throw new Exception("Division algorithm failed");
        }
        int sigmaTildeAtZero = t.getCoefficient(0);
        if (sigmaTildeAtZero == 0) throw new Exception("sigmaTilde(0) was zero");
        int inverse = field.inverse(sigmaTildeAtZero);
        return new GenericGFPoly[]{t.multiplyByMonomial(0, inverse), r.multiplyByMonomial(0, inverse)};
    }

    private int[] findErrorLocations(GenericGFPoly sigma) throws Exception {
        int numErrors = sigma.getDegree();
        if (numErrors == 1) return new int[]{sigma.getCoefficient(1)};
        int[] result = new int[numErrors];
        int e = 0;
        for (int i = 1; i < field.getSize() && e < numErrors; i++) {
            if (sigma.evaluateAt(i) == 0) {
                result[e] = field.inverse(i);
                e++;
            }
        }
        if (e != numErrors) throw new Exception("Error locator degree does not match number of roots");
        return result;
    }

    private int[] findErrorMagnitudes(GenericGFPoly omega, int[] errorLocations) {
        int numErrors = errorLocations.length;
        int[] result = new int[numErrors];
        for (int i = 0; i < numErrors; i++) {
            int xiInverse = field.inverse(errorLocations[i]);
            int denominator = 1;
            for (int j = 0; j < numErrors; j++) {
                if (i != j) {
                    denominator = field.multiply(denominator, GenericGF.addOrSubtract(1, field.multiply(errorLocations[j], xiInverse)));
                }
            }
            result[i] = field.multiply(omega.evaluateAt(xiInverse), field.inverse(denominator));
        }
        return result;
    }
}