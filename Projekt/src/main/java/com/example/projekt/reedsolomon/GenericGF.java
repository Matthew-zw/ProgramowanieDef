package com.example.projekt.reedsolomon;

public final class GenericGF {
    public static final GenericGF QR_CODE_FIELD_256 = new GenericGF(0x011D, 256, 0); // Polinom  x^8 + x^4 + x^3 + x^2 + 1
    private final int[] expTable;
    private final int[] logTable;
    private final GenericGFPoly zero;
    private final GenericGFPoly one;
    private final int size;
    private final int primitive;
    private final int generatorBase;

    public GenericGF(int primitive, int size, int b) {
        this.primitive = primitive;
        this.size = size;
        this.generatorBase = b;
        expTable = new int[size];
        logTable = new int[size];
        int x = 1;
        for (int i = 0; i < size; i++) {
            expTable[i] = x;
            x *= 2;
            if (x >= size) {
                x ^= primitive;
                x &= size - 1;
            }
        }
        for (int i = 0; i < size - 1; i++) {
            logTable[expTable[i]] = i;
        }
        zero = new GenericGFPoly(this, new int[]{0});
        one = new GenericGFPoly(this, new int[]{1});
    }

    GenericGFPoly getZero() { return zero; }
    GenericGFPoly getOne() { return one; }
    int getSize() { return size; }
    int getGeneratorBase() { return generatorBase; }

    static int addOrSubtract(int a, int b) { return a ^ b; }

    int exp(int a) { return expTable[a]; }
    int log(int a) {
        if (a == 0) throw new IllegalArgumentException();
        return logTable[a];
    }
    int inverse(int a) {
        if (a == 0) throw new IllegalArgumentException();
        return expTable[size - logTable[a] - 1];
    }
    int multiply(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return expTable[(logTable[a] + logTable[b]) % (size - 1)];
    }
    GenericGFPoly buildMonomial(int degree, int coefficient) {
        if (degree < 0) {
            throw new IllegalArgumentException();
        }
        if (coefficient == 0) {
            return zero;
        }
        int[] coefficients = new int[degree + 1];
        coefficients[0] = coefficient;
        return new GenericGFPoly(this, coefficients);
    }
}